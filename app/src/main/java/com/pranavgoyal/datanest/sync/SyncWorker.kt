package com.pranavgoyal.datanest.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.pranavgoyal.datanest.data.remote.FileRemoteRepository
import com.pranavgoyal.datanest.data.local.AppDatabase
import com.pranavgoyal.datanest.data.local.FileRepository
import com.google.firebase.auth.FirebaseAuth

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SYNC_MODE = "sync_mode"
        const val MODE_FULL = "full"
        const val MODE_INCREMENTAL = "incremental"
    }

    override suspend fun doWork(): Result {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d("SYNC_WORKER", "No user logged in, skipping")
            return Result.success()
        }

        val syncMode = inputData.getString(KEY_SYNC_MODE) ?: MODE_INCREMENTAL

        Log.d("SYNC_WORKER", "Worker started in $syncMode mode for user=${user.uid}")

        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val fileRepository = FileRepository(database.fileDao())
            val remoteRepository = FileRemoteRepository()
            val syncRepository =
                SyncRepo(
                    applicationContext,
                    fileRepository,
                    remoteRepository
                )

            syncRepository.syncAllFiles()

            // Runs every time now. The delta is cheap when nothing has
            // changed — one request that comes back empty — and gating
            // it the way the full-list pull was gated would mean an
            // incremental sync never saw remote edits at all.
            //
            // MODE_FULL still means something: it drops the cursor, so
            // the next pull starts from scratch.
            if (syncMode == MODE_FULL) {

                Log.d(
                    "SYNC_WORKER",
                    "Full mode, resetting delta cursor"
                )

                SyncCursorStore(applicationContext)
                    .clear()
            }

            syncRepository.deltaSync()

            Log.d("SYNC_WORKER", "Sync completed in $syncMode mode")
            Result.success()

        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Sync failed", e)
            Result.retry()
        }
    }
}