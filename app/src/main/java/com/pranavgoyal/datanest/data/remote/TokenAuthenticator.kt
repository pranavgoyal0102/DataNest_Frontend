package com.pranavgoyal.datanest.data.remote

import android.util.Log
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {

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
