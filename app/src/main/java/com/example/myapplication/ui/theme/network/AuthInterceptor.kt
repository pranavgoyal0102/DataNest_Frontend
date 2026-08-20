package com.example.myapplication.ui.theme.network

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Attaches the Firebase ID token to every request. The server reads the
 * uid from the verified token, so no request carries firebaseUid itself.
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = idToken(forceRefresh = false)

        if (token == null) {

            // No signed-in user, or the token fetch failed. Let the call
            // go out unauthenticated and surface as a 401 rather than
            // throwing an IOException the callers would report as a
            // transport failure.
            return chain.proceed(chain.request())
        }

        val authenticated =
            chain.request()
                .newBuilder()
                .header(
                    "Authorization",
                    "Bearer $token"
                )
                .build()

        return chain.proceed(authenticated)
    }

    companion object {

        private const val TOKEN_TIMEOUT_SECONDS = 10L

        /**
         * Blocking token fetch. Safe here — interceptors and
         * authenticators run on OkHttp's own threads, never the main
         * thread. Firebase serves this from memory unless the token is
         * near expiry or [forceRefresh] is set.
         */
        fun idToken(forceRefresh: Boolean): String? {

            val user =
                FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?: return null

            return try {

                Tasks.await(
                    user.getIdToken(forceRefresh),
                    TOKEN_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
                ).token

            } catch (e: Exception) {

                Log.e(
                    "AUTH",
                    "ID token fetch failed",
                    e
                )

                null
            }
        }
    }
}
