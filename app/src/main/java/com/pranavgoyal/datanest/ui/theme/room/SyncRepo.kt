package com.pranavgoyal.datanest.ui.theme.room

import android.content.Context
import android.util.Log
import com.pranavgoyal.datanest.ui.theme.dto.FileResponse
import com.pranavgoyal.datanest.ui.theme.models.DeleteResult
import com.pranavgoyal.datanest.ui.theme.models.DeltaResult
import com.pranavgoyal.datanest.ui.theme.models.FileStored
import com.pranavgoyal.datanest.ui.theme.models.SyncResult
import com.pranavgoyal.datanest.ui.theme.models.SyncStatus
import com.pranavgoyal.datanest.ui.theme.models.UpdateResult
import com.pranavgoyal.datanest.ui.theme.network.FileDownloader
import com.pranavgoyal.datanest.ui.theme.network.FileRemoteRepository
import com.pranavgoyal.datanest.ui.theme.worker.SyncCursorStore

class SyncRepo(

    private val context: Context,

    private val localRepo: FileRepository,

    private val remoteRepo: FileRemoteRepository
){

    /**
     * Pulls everything changed since the stored cursor, a page at a time.
     *
     * Nothing is ever removed here. A row with isDeleted = true is
     * trashed, not purged, and has to stay so the trash screen can show
     * it; a file purged server-side just stops appearing in the feed,
     * which is indistinguishable from it being unchanged. A device
     * offline across a purge therefore keeps its copy — known gap, and
     * closing it needs the server to emit tombstones.
     */
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

                        // Leave the cursor alone: advancing past rows
                        // that were never applied would skip them for
                        // good. The next run retries from here.
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

            // Null means the payload did not have the shape we expect —
            // a changed envelope, a proxy answering in HTML. Surface it
            // and leave the cursor be; treating it as an empty page
            // would advance past rows that were never applied.
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

                // Persisted only after the page is applied. A crash in
                // between re-fetches a page that is already in, and
                // applying it twice lands on the same state.
                store.cursor = next

                cursor = next

            } else if (page.hasMore) {

                // Same cursor handed back with more promised, or none at
                // all: following it would re-request this page forever.
                // Compared for equality only — the value stays opaque.
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

    /**
     * Folds one remote row into the local table. Unchanged in substance
     * from the full-list pull this replaced: a local edit still in
     * flight keeps its fields and takes only the server's version.
     */
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

                // Local edit is pending, so its fields stay put — but
                // take the server's version anyway, otherwise the
                // eventual PATCH goes out stale and 409s.
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

        /** Bounds a server that never stops saying hasMore. */
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

    /**
     * Pushes one pending edit, resolving a stale-version 409 with the
     * server state carried in that response.
     *
     * The tie-break matches [downloadCloudFiles]: a strictly newer server
     * edit wins, otherwise the local edit is re-sent with the server's
     * version. [retryOnConflict] bounds that to a single retry so a
     * repeatedly-conflicting file can't spin.
     */
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

    /**
     * Hard-deletes files the user explicitly destroyed from the trash
     * screen, via DELETE api/files/{id}?version=. Trashing is handled by
     * [syncUpdatedFiles] as an ordinary PATCH with isDeleted = true.
     */
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

                // Purge is only reachable from the trash screen, so the
                // file is already trashed server-side by the time this
                // runs — there is no trash-first step to do here.
                var result =
                    remoteRepo.deleteFilePermanently(
                        remoteId,
                        file.version
                    )

                // A stale version comes back with the server's current
                // state attached, so the right version is already in
                // hand. Retried once — a second conflict means something
                // else is writing, and spinning would not help.
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

                        // Already gone server-side. Retrying will never
                        // succeed, so flag it loudly rather than leaving
                        // the row cycling through FAILED forever.
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

                        // Either the retry conflicted too, or the body
                        // carried no state to retry with. The row keeps
                        // PENDING_PURGE intent via FAILED and the next
                        // downloadCloudFiles refreshes its version.
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
