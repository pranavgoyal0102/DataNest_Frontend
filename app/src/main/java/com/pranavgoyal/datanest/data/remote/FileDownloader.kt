package com.pranavgoyal.datanest.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class FileDownloader(
    private val context: Context
) {

    /**
     * Pinned to Dispatchers.IO because the body blocks for the whole
     * transfer. Its caller is SyncRepo, which runs on CoroutineWorker's
     * Dispatchers.Default — a pool sized to the CPU count — so a couple of
     * concurrent downloads there would tie up threads meant for compute
     * and stall everything else scheduled on it.
     */
    suspend fun downloadFile(
        url: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {

        return@withContext try {

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