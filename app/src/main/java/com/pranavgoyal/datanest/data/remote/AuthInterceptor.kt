package com.pranavgoyal.datanest.data.remote

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = idToken(forceRefresh = false)

        if (token == null) {

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
