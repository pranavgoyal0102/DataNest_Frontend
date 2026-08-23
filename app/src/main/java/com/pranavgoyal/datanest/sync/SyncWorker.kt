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

            val dirty = fileRepository.getDirtyFileCount()

            if (dirty > 0) {

                Log.d(
                    "SYNC_WORKER",
                    "$dirty row(s) changed mid-sync, re-enqueueing"
                )

                SyncScheduler.startFollowUp(applicationContext)
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Sync failed", e)
            Result.retry()
        }
    }
}