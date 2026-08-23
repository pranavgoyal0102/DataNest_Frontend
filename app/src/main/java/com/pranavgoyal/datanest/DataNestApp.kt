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
