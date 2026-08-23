package com.pranavgoyal.datanest

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.pranavgoyal.datanest.sync.SyncScheduler

class DataNestApp : Application() {

    override fun onCreate() {

        super.onCreate()

        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(ForegroundSyncObserver())
    }

    /**
     * Syncs when the app comes to the foreground.
     *
     * Observing the process rather than an Activity is what makes this
     * once per foreground: onStart here fires when the first Activity
     * starts and not again until the whole app has been backgrounded, so
     * a rotation or a second Activity does not re-trigger it. It also
     * covers cold start, which is why MainActivity no longer schedules
     * anything of its own.
     */
    private inner class ForegroundSyncObserver : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {

            Log.d(
                "SYNC",
                "App foregrounded, scheduling sync"
            )

            SyncScheduler.start(this@DataNestApp)
        }
    }
}
