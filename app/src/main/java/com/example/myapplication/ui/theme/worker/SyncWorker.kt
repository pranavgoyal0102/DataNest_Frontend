package com.example.myapplication.ui.theme.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.myapplication.ui.theme.network.FileRemoteRepository
import com.example.myapplication.ui.theme.room.AppDatabase
import com.example.myapplication.ui.theme.room.FileRepository
import com.example.myapplication.ui.theme.room.SyncRepo
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

            val localCount =
                fileRepository.getFilesCount()

            if (
                syncMode == MODE_FULL ||
                localCount == 0
            ) {

                Log.d(
                    "SYNC_WORKER",
                    "Downloading cloud files"
                )

                syncRepository.downloadCloudFiles()
            }

            Log.d("SYNC_WORKER", "Sync completed in $syncMode mode")
            Result.success()

        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Sync failed", e)
            Result.retry()
        }
    }
}