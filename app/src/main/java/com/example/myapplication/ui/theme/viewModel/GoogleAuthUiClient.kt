package com.example.myapplication.ui.theme.viewModel

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val activity: Activity
) {

    private val auth = FirebaseAuth.getInstance()

    private val credentialManager =
        CredentialManager.create(activity)

    suspend fun signIn(): GetCredentialResponse? {

        val googleIdOption =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    activity.getString(
                        activity.resources.getIdentifier(
                            "server_client_id",
                            "string",
                            activity.packageName
                        )
                    )
                )
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(
                    googleIdOption
                )
                .build()

        return try {

            credentialManager.getCredential(
                context = activity,
                request = request
            )

        } catch (e: GetCredentialException) {

            Log.e(
                "GoogleSignIn",
                "Credential error",
                e
            )

            null
        }
    }

    suspend fun firebaseAuthWithGoogle(
        idToken: String
    ): FirebaseUser? {

        return try {

            val credential =
                GoogleAuthProvider.getCredential(
                    idToken,
                    null
                )

            auth.signInWithCredential(
                credential
            ).await()

            auth.currentUser

        } catch (e: Exception) {

            Log.e(
                "GoogleSignIn",
                "Firebase auth failed",
                e
            )

            null
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    fun signOut() {
        auth.signOut()
    }
}