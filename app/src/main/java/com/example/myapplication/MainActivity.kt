package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.uii.MainNavigation
import com.example.myapplication.ui.theme.viewModel.RoomViewModel

class MainActivity : ComponentActivity() {

    private var sharedUri: Uri? = null
    private var sharedUris: List<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle the shared file intent (if app opened via Share)
        handleShareIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val roomViewModel: RoomViewModel = ViewModelProvider(this)[RoomViewModel::class.java]

                MainNavigation(
                    roomViewModel = roomViewModel,
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

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                sharedUri = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                sharedUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }
        }
    }
}
