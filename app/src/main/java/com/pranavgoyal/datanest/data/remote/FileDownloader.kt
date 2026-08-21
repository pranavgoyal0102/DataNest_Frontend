package com.pranavgoyal.datanest.data.remote

import android.content.Context
import android.util.Log
import java.io.File
import java.net.URL

class FileDownloader(
    private val context: Context
) {

    suspend fun downloadFile(
        url: String,
        fileName: String
    ): String? {

        return try {

            val directory =
                File(
                    context.filesDir,
                    "downloads"
                )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val destination =
                File(
                    directory,
                    fileName
                )

            URL(url)
                .openStream()
                .use { input ->

                    destination.outputStream().use {

                            output ->

                        input.copyTo(output)
                    }
                }

            destination.absolutePath

        } catch (e: Exception) {

            Log.e(
                "DOWNLOAD",
                "Download failed",
                e
            )

            null
        }
    }
}