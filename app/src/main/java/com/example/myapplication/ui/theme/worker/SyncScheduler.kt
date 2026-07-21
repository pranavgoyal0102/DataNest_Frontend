package com.example.myapplication.ui.theme.worker

import android.content.Context
import android.util.Log
import androidx.work.*

object SyncScheduler {

    private const val PREF_NAME = "sync_prefs"
    private const val KEY_INITIAL_SYNC_DONE = "initial_sync_done"

    fun start(context: Context) {

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val initialSyncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)

        val syncMode = if (initialSyncDone) SyncWorker.MODE_INCREMENTAL
        else SyncWorker.MODE_FULL

        val inputData = Data.Builder()
            .putString(SyncWorker.KEY_SYNC_MODE, syncMode)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "file_sync",
            ExistingWorkPolicy.KEEP,
            request
        )

        if (!initialSyncDone) {
            prefs.edit().putBoolean(KEY_INITIAL_SYNC_DONE, true).apply()
        }

        Log.d("SYNC", "Sync scheduled in $syncMode mode")
    }
}