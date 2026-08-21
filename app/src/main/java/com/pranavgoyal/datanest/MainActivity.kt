package com.pranavgoyal.datanest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pranavgoyal.datanest.ui.theme.MyApplicationTheme
import com.pranavgoyal.datanest.ui.screens.MainNavigation
import com.pranavgoyal.datanest.ui.viewmodel.RoomViewModel
import com.pranavgoyal.datanest.sync.SyncScheduler
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private var sharedUri: Uri? = null
    private var sharedUris: List<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleShareIntent(intent)
        Log.e(
            "AUTH_CHECK",
            "User = ${
                FirebaseAuth
                    .getInstance()
                    .currentUser?.uid
            }"
        )
        SyncScheduler.start(this)
        Log.d("SYNC", "Worker scheduled")

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainNavigation(
                    sharedUri = sharedUri,
                    sharedUris = sharedUris
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(
        intent: Intent?
    ) {

        Log.e(
            "SHARE_DEBUG",
            "action = ${intent?.action}"
        )

        if (intent == null) return

        when (intent.action) {

            Intent.ACTION_SEND -> {

                sharedUri =
                    intent.getParcelableExtra(
                        Intent.EXTRA_STREAM
                    )

                Log.e(
                    "SHARE_DEBUG",
                    "sharedUri = $sharedUri"
                )
            }

            Intent.ACTION_SEND_MULTIPLE -> {

                sharedUris =
                    intent.getParcelableArrayListExtra(
                        Intent.EXTRA_STREAM
                    )

                Log.e(
                    "SHARE_DEBUG",
                    "sharedUris = ${sharedUris?.size}"
                )
            }
        }
    }
}
