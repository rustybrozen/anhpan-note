package com.example.anhpannt.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteNote(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("user_id")
    val userId: String? = null
) {
    fun toLocal(isSynced: Boolean = true): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            updatedAt = updatedAt,
            isSynced = isSynced,
            isDeleted = isDeleted
        )
    }
}




fun Note.toRemote(userId: String? = null): RemoteNote {
    return RemoteNote(
        id = id,
        title = title,
        content = content,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        userId = userId
    )
}
