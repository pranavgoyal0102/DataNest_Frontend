package com.pranavgoyal.datanest.data.local

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

    val version: Long = 0
)

enum class SyncStatus {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    PENDING_UPDATE,
    PENDING_PURGE,
    SYNCED,
    FAILED
}