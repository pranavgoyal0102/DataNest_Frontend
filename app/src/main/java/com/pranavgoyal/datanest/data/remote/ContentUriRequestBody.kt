package com.pranavgoyal.datanest.data.remote

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

class ContentUriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val mediaType: MediaType?,
    private val declaredSize: Long
) : RequestBody() {

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long =
        if (declaredSize > 0) declaredSize else -1L

    override fun writeTo(sink: BufferedSink) {

        val stream =
            context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open $uri")

        stream.use { input ->
            sink.writeAll(input.source())
        }
    }
}
