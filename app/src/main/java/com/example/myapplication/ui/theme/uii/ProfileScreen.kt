package com.example.myapplication.ui.theme.uii

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.viewModel.GoogleAuthUiClient
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.CustomCredential
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.viewModel.SyncViewModel
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val syncViewModel : SyncViewModel = viewModel()
    val activity = context.findActivity()
    val scope = rememberCoroutineScope()

    val googleClient = remember {
        GoogleAuthUiClient(activity)
    }

    val auth = FirebaseAuth.getInstance()
    val user = remember { mutableStateOf(auth.currentUser) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            user.value = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        if (user.value != null) {
            AsyncImage(
                model = user.value!!.photoUrl,
                contentDescription = "Profile Image",
                modifier = Modifier.size(150.dp).clip(RoundedCornerShape(100))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = user.value!!.displayName ?: "User",
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = {
                googleClient.signOut()
            }) {
                Text("Logout", fontSize = 20.sp, color = Color.White)
            }

        } else {

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account",
                modifier = Modifier.size(200.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = {
                scope.launch {
                    try {
                        val result = googleClient.signIn() ?: return@launch
                        val credential = result.credential

                        if (
                            credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCredential =
                                GoogleIdTokenCredential.createFrom(credential.data)

                            googleClient.firebaseAuthWithGoogle(
                                googleCredential.idToken
                            )

                            Toast.makeText(
                                context,
                                "Login successful",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Login failed", e)
                        Toast.makeText(
                            context,
                            "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }) {
                Text("Login", fontSize = 32.sp, color = Color.White)
            }
        }

        TextButton(onClick = {
            syncViewModel.syncNow()
        }) {
            Text(text = "Sync Now")
        }
    }
}

/**
 * 🔥 REQUIRED helper for Credential Manager
 */
fun Context.findActivity(): Activity {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    throw IllegalStateException("Activity not found")
}
