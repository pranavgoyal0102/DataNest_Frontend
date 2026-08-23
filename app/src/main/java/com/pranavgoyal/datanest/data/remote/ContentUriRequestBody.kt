package com.pranavgoyal.datanest.data.remote

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

/**
 * Streams a content:// document straight from the ContentResolver into
 * the request body, so the file never fully materialises on the heap.
 *
 * Reading the URI into a ByteArray first is what OOMed: a 225MB file
 * wants 225MB of contiguous heap up front, which no ordinary device is
 * going to hand over. Okio copies segment by segment instead, so peak
 * usage is a segment rather than the whole file.
 */
class ContentUriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val mediaType: MediaType?,
    private val declaredSize: Long
) : RequestBody() {

    override fun contentType(): MediaType? = mediaType

    /**
     * The size recorded when the file was picked, or -1 when the picker
     * could not report one — that makes OkHttp fall back to chunked
     * encoding. Sending a known length is worth it: the server can then
     * reject an oversized upload off the headers rather than after the
     * whole body has arrived.
     */
    override fun contentLength(): Long =
        if (declaredSize > 0) declaredSize else -1L

    /**
     * Called once per attempt, and OkHttp can attempt more than once —
     * [TokenAuthenticator] replays the request after refreshing a 401.
     * Opening a fresh stream each time keeps the body repeatable, so a
     * replay is not left writing an already-drained one.
     */
    override fun writeTo(sink: BufferedSink) {

        val stream =
            context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open $uri")

        stream.use { input ->
            sink.writeAll(input.source())
        }
    }
}
