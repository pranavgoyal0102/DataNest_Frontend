package com.pranavgoyal.datanest.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val PREF_NAME = "sync_prefs"
    private const val KEY_INITIAL_SYNC_DONE = "initial_sync_done"

    private const val UNIQUE_WORK_NAME = "file_sync"

    /**
     * How long a local edit waits before its sync runs. Rapid edits —
     * starring three files in a row — all land inside this window, and
     * ExistingWorkPolicy.KEEP drops the later ones against the request
     * already sitting in ENQUEUED. One run then covers all of them.
     *
     * Dropping a trigger loses nothing because [SyncWorker] syncs
     * whatever is pending in the database rather than a row handed to it
     * in inputData.
     */
    const val COALESCE_DELAY_SECONDS = 5L

    /**
     * Enqueues a sync, coalescing against any already-pending run.
     *
     * [delaySeconds] is the batching window. Pass 0 for a trigger the
     * user is waiting on — the manual button, or coming back to the app
     * — and [COALESCE_DELAY_SECONDS] for a local edit.
     */
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

    /**
     * Queues one more pass to run after the pass currently in flight.
     *
     * Only for [SyncWorker] to call on itself. KEEP would be wrong here:
     * the running worker still counts as pending work under its own
     * unique name, so a KEEP enqueue from inside it is silently dropped
     * and the follow-up never happens. APPEND_OR_REPLACE instead hangs
     * the new request off the running one, and falls back to replacing
     * if that run ends up cancelled or failed.
     */
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
