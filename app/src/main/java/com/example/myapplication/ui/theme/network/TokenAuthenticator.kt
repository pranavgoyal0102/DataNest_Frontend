package com.example.myapplication.ui.theme.network

import android.util.Log
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Handles 401 by force-refreshing the ID token and replaying the request
 * once. Matters most for SyncWorker, which can run long enough for a
 * token minted at the start of a sync to expire mid-run.
 */
class TokenAuthenticator : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

        // Only ever retry once — a server that keeps rejecting would
        // otherwise spin here.
        if (response.priorResponse != null) {
            return null
        }

        val fresh =
            AuthInterceptor.idToken(forceRefresh = true)
                ?: return null

        val used =
            response.request
                .header("Authorization")
                ?.removePrefix("Bearer ")

        // Firebase handed back the same cached token, so replaying it
        // would just earn another 401.
        if (fresh == used) {
            return null
        }

        Log.d(
            "AUTH",
            "Refreshed ID token after 401, retrying"
        )

        return response.request
            .newBuilder()
            .header(
                "Authorization",
                "Bearer $fresh"
            )
            .build()
    }
}
