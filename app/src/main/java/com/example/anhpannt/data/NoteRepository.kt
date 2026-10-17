package com.example.anhpannt.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(
    private val noteDao: NoteDao
) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()




    fun getNoteById(id: String): Flow<Note?> {
        return noteDao.getNoteById(id)
    }




    suspend fun saveNote(id: String?, title: String, content: String): String {
        val noteId = id ?: UUID.randomUUID().toString()
        val note = Note(
            id = noteId,
            title = title.trim(),
            content = content.trim(),
            updatedAt = System.currentTimeMillis(),
            isSynced = false,
            isDeleted = false
        )
        noteDao.upsertNote(note)
        return noteId
    }




    suspend fun deleteNotes(ids: List<String>) {
        if (ids.isNotEmpty()) {
            noteDao.markAsDeleted(ids, System.currentTimeMillis())
        }
    }




    suspend fun signIn(email: String, password: String): String = withContext(Dispatchers.IO) {
        SupabaseProvider.client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val user = SupabaseProvider.client.auth.currentUserOrNull()
        user?.id ?: ""
    }




    suspend fun signUp(email: String, password: String): String = withContext(Dispatchers.IO) {
        SupabaseProvider.client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        var user = SupabaseProvider.client.auth.currentUserOrNull()
        if (user == null) {
            SupabaseProvider.client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            user = SupabaseProvider.client.auth.currentUserOrNull()
        }
        user?.id ?: ""
    }




    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            SupabaseProvider.client.auth.signOut()
        } catch (e: Exception) {
            // Ignore offline signOut errors
        }
    }




    suspend fun syncAllNotes(userId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext

        val postgrest = SupabaseProvider.client.from("notes")

        val remoteNotes = postgrest.select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<RemoteNote>()
        val remoteMap = remoteNotes.associateBy { it.id }

        val localNotes = noteDao.getAllNotesSnapshot()
        val localMap = localNotes.associateBy { it.id }

        val localToUpsert = mutableListOf<Note>()
        val remoteToUpsert = mutableListOf<RemoteNote>()

        for (remote in remoteNotes) {
            val local = localMap[remote.id]
            if (local == null) {
                if (!remote.isDeleted) {
                    localToUpsert.add(remote.toLocal(isSynced = true))
                }
            } else {
                if (remote.updatedAt > local.updatedAt) {
                    localToUpsert.add(remote.toLocal(isSynced = true))
                } else if (local.updatedAt > remote.updatedAt || !local.isSynced) {
                    remoteToUpsert.add(local.toRemote(userId = userId))
                }
            }
        }

        for (local in localNotes) {
            if (!remoteMap.containsKey(local.id)) {
                remoteToUpsert.add(local.toRemote(userId = userId))
            }
        }

        if (remoteToUpsert.isNotEmpty()) {
            postgrest.upsert(remoteToUpsert)
            noteDao.markAsSynced(remoteToUpsert.map { it.id })
        }

        if (localToUpsert.isNotEmpty()) {
            noteDao.upsertNotes(localToUpsert)
        }
    }
}
