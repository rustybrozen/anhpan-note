package com.example.anhpannt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: String): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotes(notes: List<Note>)

    @Query("UPDATE notes SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun markAsDeleted(ids: List<String>, updatedAt: Long)

    @Query("UPDATE notes SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<Note>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSnapshot(): List<Note>

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int
}
