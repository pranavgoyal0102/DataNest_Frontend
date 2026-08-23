package com.pranavgoyal.datanest.sync

import android.content.Context
import android.util.Log
import com.pranavgoyal.datanest.data.remote.dto.FileResponse
import com.pranavgoyal.datanest.data.local.FileRepository
import com.pranavgoyal.datanest.data.local.FileStored
import com.pranavgoyal.datanest.data.local.SyncStatus
import com.pranavgoyal.datanest.data.remote.FileDownloader
import com.pranavgoyal.datanest.data.remote.FileRemoteRepository

class SyncRepo(

    private val context: Context,

    private val localRepo: FileRepository,

    private val remoteRepo: FileRemoteRepository
){

    suspend fun deltaSync() {

        val store = SyncCursorStore(context)

        var cursor = store.cursor

        var pages = 0

        Log.d(
            "SYNC",
            "Delta sync from cursor=${cursor ?: "<none>"}"
        )

        while (pages < MAX_PAGES) {

            val page =
                when (
                    val result =
                        remoteRepo.syncDelta(
                            cursor,
                            PAGE_SIZE
                        )
                ) {

                    is DeltaResult.Page -> result.page

                    is DeltaResult.HttpError -> {

                        Log.e(
                            "SYNC",
                            "Delta page rejected at cursor=$cursor: " +
                                    "${result.detail}"
                        )

                        return
                    }

                    is DeltaResult.Transport -> {

                        Log.e(
                            "SYNC",
                            "Delta page did not reach the server at " +
                                    "cursor=$cursor: ${result.message}"
                        )

                        return
                    }
                }

            val rows =
                page.changes
                    ?: run {

                        Log.e(
                            "SYNC",
                            "Delta page at cursor=$cursor had no " +
                                    "changes list, stopping"
                        )

                        return
                    }

            rows.forEach { remote ->
                applyRemote(remote)
            }

            pages++

            val next = page.cursor

            if (next != null && next != cursor) {

                store.cursor = next

                cursor = next

            } else if (page.hasMore) {

                Log.e(
                    "SYNC",
                    "Delta cursor did not change at $cursor " +
                            "(${rows.size} rows, hasMore), stopping"
                )

                return
            }

            Log.d(
                "SYNC",
                "Applied ${rows.size} rows, " +
                        "cursor now $cursor, hasMore=${page.hasMore}"
            )

            if (!page.hasMore) {
                return
            }
        }

        Log.e(
            "SYNC",
            "Delta sync stopped at $MAX_PAGES pages, " +
                    "cursor $cursor — more remains"
        )
    }

    private suspend fun applyRemote(
        remote: FileResponse
    ) {

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
                    version = remote.version,
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

                        version = remote.version,

                        syncStatus =
                        SyncStatus.SYNCED
                    )
                )

                Log.d(
                    "SYNC",
                    "Updated from cloud ${remote.title}"
                )

            } else {

                if (remote.version != existing.version) {

                    localRepo.updateVersion(
                        existing.id,
                        remote.version
                    )
                }

                Log.d(
                    "SYNC",
                    "Skipped ${remote.title}"
                )
            }
        }
    }

    suspend fun syncAllFiles() {

        uploadNewFiles()

        syncUpdatedFiles()

        syncPurgedFiles()
    }

    private companion object {

        const val PAGE_SIZE = 200

        const val MAX_PAGES = 50
    }

    private suspend fun uploadNewFiles() {

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
                            file
                        )
                ) {

                    is SyncResult.Success -> {

                        localRepo.updateRemoteId(
                            file.id,
                            result.remoteId,
                            result.version
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
                            "Upload failed ${file.title}: ${result.message}"
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

                pushUpdate(file)

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

    private suspend fun pushUpdate(
        file: FileStored,
        retryOnConflict: Boolean = true
    ) {

        when (
            val result =
                remoteRepo.updateFile(file)
        ) {

            is UpdateResult.Success -> {

                localRepo.updateVersion(
                    file.id,
                    result.version
                )

                localRepo.updateSyncStatus(
                    file.id,
                    SyncStatus.SYNCED.name
                )

                Log.d(
                    "SYNC",
                    "Updated ${file.title}"
                )
            }

            is UpdateResult.Conflict -> {

                val server = result.server

                val serverWins =
                    server.updatedAt > file.updatedAt ||
                            !retryOnConflict

                if (serverWins) {

                    localRepo.updateFile(

                        file.copy(

                            title = server.title,

                            mimeType = server.mimeType,

                            size = server.size,

                            isDeleted = server.isDeleted,

                            isStarred = server.isStarred,

                            updatedAt = server.updatedAt,

                            version = server.version,

                            syncStatus = SyncStatus.SYNCED
                        )
                    )

                    Log.d(
                        "SYNC",
                        "Conflict on ${file.title}, took server state"
                    )

                } else {

                    Log.d(
                        "SYNC",
                        "Conflict on ${file.title}, retrying local edit"
                    )

                    pushUpdate(
                        file.copy(version = server.version),
                        retryOnConflict = false
                    )
                }
            }

            is UpdateResult.Error -> {

                localRepo.updateSyncStatus(
                    file.id,
                    SyncStatus.FAILED.name
                )

                Log.e(
                    "SYNC",
                    "Update failed ${file.title}: ${result.message}"
                )
            }
        }
    }

    private suspend fun syncPurgedFiles() {

        val pendingPurges =
            localRepo.getPendingPurgeFiles()

        Log.d(
            "SYNC",
            "Purges = ${pendingPurges.size}"
        )

        pendingPurges.forEach { file ->

            try {

                val remoteId =
                    file.remoteId

                if (remoteId == null) {

                    localRepo.deleteFile(
                        file
                    )

                    return@forEach
                }

                var stage =
                    "DELETE api/files/$remoteId" +
                            "?version=${file.version}"

                var result =
                    remoteRepo.deleteFilePermanently(
                        remoteId,
                        file.version
                    )

                val conflict = result as? DeleteResult.Conflict

                val server = conflict?.server

                if (server != null) {

                    Log.d(
                        "SYNC",
                        "Purge conflict on ${file.title}, " +
                                "retrying at version ${server.version}"
                    )

                    localRepo.updateVersion(
                        file.id,
                        server.version
                    )

                    stage =
                        "DELETE api/files/$remoteId" +
                                "?version=${server.version} (retry)"

                    result =
                        remoteRepo.deleteFilePermanently(
                            remoteId,
                            server.version
                        )
                }

                val label = "${file.title} [$remoteId] $stage"

                when (val outcome = result) {

                    is DeleteResult.Success -> {

                        localRepo.deleteFile(
                            file
                        )

                        Log.d(
                            "SYNC",
                            "Purged ${file.title}"
                        )
                    }

                    is DeleteResult.NotFound -> {

                        localRepo.updateSyncStatus(
                            file.id,
                            SyncStatus.FAILED.name
                        )

                        Log.e(
                            "SYNC",
                            "Purge rejected, no such file on server: " +
                                    "$label -> ${outcome.detail}"
                        )
                    }

                    is DeleteResult.Conflict -> {

                        localRepo.updateSyncStatus(
                            file.id,
                            SyncStatus.FAILED.name
                        )

                        Log.e(
                            "SYNC",
                            "Purge rejected as stale" +
                                    (if (outcome.server == null)
                                        " (no server state in body)"
                                    else "") +
                                    ": $label -> ${outcome.detail}"
                        )
                    }

                    is DeleteResult.HttpError -> {

                        localRepo.updateSyncStatus(
                            file.id,
                            SyncStatus.FAILED.name
                        )

                        Log.e(
                            "SYNC",
                            "Purge rejected: $label -> ${outcome.detail}"
                        )
                    }

                    is DeleteResult.Transport -> {

                        localRepo.updateSyncStatus(
                            file.id,
                            SyncStatus.FAILED.name
                        )

                        Log.e(
                            "SYNC",
                            "Purge did not reach the server: " +
                                    "$label -> ${outcome.message}"
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
                    "Purge threw for ${file.title} " +
                            "[${file.remoteId}]",
                    e
                )
            }
        }
    }
}
