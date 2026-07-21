package com.example.myapplication.ui.theme.room

import android.content.Context
import android.util.Log
import com.example.myapplication.ui.theme.models.FileStored
import com.example.myapplication.ui.theme.models.SyncResult
import com.example.myapplication.ui.theme.models.SyncStatus
import com.example.myapplication.ui.theme.network.FileDownloader
import com.example.myapplication.ui.theme.network.FileRemoteRepository

class SyncRepo(

    private val context: Context,

    private val localRepo: FileRepository,

    private val remoteRepo: FileRemoteRepository
){

    suspend fun downloadCloudFiles(
        firebaseUid: String
    ) {

        val remoteFiles =
            remoteRepo.downloadFiles(
                firebaseUid
            )

        remoteFiles.forEach { remote ->

            val existing =
                localRepo.getFileByRemoteId(
                    remote.id
                )

            if (existing == null) {
                val downloader =
                    FileDownloader(
                        context
                    )

                val localPath =
                    downloader.downloadFile(

                        remote.cloudUrl,

                        remote.title
                    )

                localRepo.saveFile(

                    FileStored(
                        remoteId = remote.id,
                        title = remote.title,
                        uri = localPath ?: "",
                        mimeType = remote.mimeType,
                        size = remote.size,
                        isDeleted = remote.isDeleted,
                        isStarred = remote.isStarred,
                        createdAt = remote.createdAt,
                        updatedAt = remote.updatedAt,
                        syncStatus = SyncStatus.SYNCED
                    )
                )

                Log.d(
                    "SYNC",
                    "Downloaded ${remote.title}"
                )

            } else {

                if (
                    existing.syncStatus == SyncStatus.SYNCED &&
                    remote.updatedAt > existing.updatedAt
                ) {

                    localRepo.updateFile(

                        existing.copy(

                            title = remote.title,

                            mimeType = remote.mimeType,

                            size = remote.size,

                            isDeleted = remote.isDeleted,

                            isStarred = remote.isStarred,

                            updatedAt = remote.updatedAt,

                            syncStatus =
                            SyncStatus.SYNCED
                        )
                    )

                    Log.d(
                        "SYNC",
                        "Updated from cloud ${remote.title}"
                    )

                } else {

                    Log.d(
                        "SYNC",
                        "Skipped ${remote.title}"
                    )
                }
            }
        }
    }

    suspend fun syncAllFiles(
        firebaseUid: String
    ) {

        uploadNewFiles(
            firebaseUid
        )

        syncUpdatedFiles()

        syncDeletedFiles(
            firebaseUid
        )
    }

    private suspend fun uploadNewFiles(
        firebaseUid: String
    ) {

        val pendingUploads =
            localRepo.getPendingUploadFiles()

        Log.d(
            "SYNC",
            "Pending uploads = ${pendingUploads.size}"
        )

        pendingUploads.forEach { file ->

            try {

                localRepo.updateSyncStatus(
                    file.id,
                    SyncStatus.PENDING_UPLOAD.name
                )

                when (
                    val result =
                        remoteRepo.uploadFile(
                            context,
                            file,
                            firebaseUid
                        )
                ) {

                    is SyncResult.Success -> {

                        localRepo.updateRemoteId(
                            file.id,
                            result.remoteId
                        )

                        localRepo.updateSyncStatus(
                            file.id,
                            SyncStatus.SYNCED.name
                        )

                        Log.d(
                            "SYNC",
                            "Uploaded ${file.title}"
                        )
                    }

                    is SyncResult.Error -> {

                        localRepo.updateSyncStatus(
                            file.id,
                            SyncStatus.FAILED.name
                        )

                        Log.e(
                            "SYNC",
                            "Upload failed ${file.title}"
                        )
                    }
                }

            } catch (e: Exception) {

                localRepo.updateSyncStatus(
                    file.id,
                    SyncStatus.FAILED.name
                )

                Log.e(
                    "SYNC",
                    "Exception uploading ${file.title}",
                    e
                )
            }
        }
    }

    private suspend fun syncUpdatedFiles() {

        val pendingUpdates =
            localRepo.getPendingUpdateFiles()

        Log.d(
            "SYNC",
            "Updates = ${pendingUpdates.size}"
        )

        pendingUpdates.forEach { file ->

            try {

                val success =
                    remoteRepo.updateFile(
                        file
                    )

                if (success) {

                    localRepo.updateSyncStatus(
                        file.id,
                        SyncStatus.SYNCED.name
                    )

                    Log.d(
                        "SYNC",
                        "Updated ${file.title}"
                    )

                } else {

                    localRepo.updateSyncStatus(
                        file.id,
                        SyncStatus.FAILED.name
                    )
                }

            } catch (e: Exception) {

                localRepo.updateSyncStatus(
                    file.id,
                    SyncStatus.FAILED.name
                )

                Log.e(
                    "SYNC",
                    "Update failed ${file.title}",
                    e
                )
            }
        }
    }

    private suspend fun syncDeletedFiles(
        firebaseUid: String
    ) {

        val pendingDeletes =
            localRepo.getPendingDeleteFiles()

        Log.d(
            "SYNC",
            "Deletes = ${pendingDeletes.size}"
        )

        pendingDeletes.forEach { file ->

            try {

                val remoteId =
                    file.remoteId

                if (remoteId == null) {

                    localRepo.deleteFile(
                        file
                    )

                    return@forEach
                }

                val success =
                    remoteRepo.deleteFile(
                        firebaseUid,
                        remoteId
                    )

                if (success) {

                    localRepo.deleteFile(
                        file
                    )

                    Log.d(
                        "SYNC",
                        "Deleted ${file.title}"
                    )

                } else {

                    localRepo.updateSyncStatus(
                        file.id,
                        SyncStatus.FAILED.name
                    )
                }

            } catch (e: Exception) {

                localRepo.updateSyncStatus(
                    file.id,
                    SyncStatus.FAILED.name
                )

                Log.e(
                    "SYNC",
                    "Delete failed ${file.title}",
                    e
                )
            }
        }
    }
}