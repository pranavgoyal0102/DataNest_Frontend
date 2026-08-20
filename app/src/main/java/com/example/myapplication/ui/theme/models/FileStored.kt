package com.example.myapplication.ui.theme.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "files",
    indices = [
        Index(
            value = ["remoteId"],
            unique = true
        ),
        Index("syncStatus"),
        Index("isDeleted"),
        Index("title"),
        Index("updatedAt")
    ]
)
data class FileStored(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val remoteId: String? = null,

    val title: String,

    val uri: String,

    val mimeType: String,

    val size: Long,

    val createdAt: Long,

    val updatedAt: Long,

    val folderId: Long? = null,

    val isStarred: Boolean = false,

    val isDeleted: Boolean = false,

    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,

    val cloudUrl: String? = null,

    val fileHash: String? = null,

    // Server-assigned JPA @Version. 0 until the file has been
    // uploaded; PATCH rejects a request that omits or staleness-fails it.
    val version: Long = 0
)

enum class SyncStatus {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    PENDING_UPDATE,

    /**
     * Explicit "delete permanently" from the trash screen. Trashing a
     * file is a PENDING_UPDATE with isDeleted = true, not this.
     */
    PENDING_PURGE,
    SYNCED,
    FAILED
}