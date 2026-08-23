package com.pranavgoyal.datanest.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val PREF_NAME = "sync_prefs"
    private const val KEY_INITIAL_SYNC_DONE = "initial_sync_done"

    private const val UNIQUE_WORK_NAME = "file_sync"

    const val COALESCE_DELAY_SECONDS = 5L

    fun start(
        context: Context,
        delaySeconds: Long = 0
    ) {

        enqueue(
            context,
            delaySeconds,
            ExistingWorkPolicy.KEEP
        )
    }

    fun startFollowUp(context: Context) {

        enqueue(
            context,
            0,
            ExistingWorkPolicy.APPEND_OR_REPLACE
        )
    }

    private fun enqueue(
        context: Context,
        delaySeconds: Long,
        policy: ExistingWorkPolicy
    ) {

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
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            policy,
            request
        )

        if (!initialSyncDone) {
            prefs.edit().putBoolean(KEY_INITIAL_SYNC_DONE, true).apply()
        }

        Log.d(
            "SYNC",
            "Sync scheduled in $syncMode mode, " +
                    "delay=${delaySeconds}s, policy=$policy"
        )
    }
}
