package com.example.myapplication.ui.theme.models

import com.example.myapplication.ui.theme.dto.FileResponse

sealed class SyncResult {

    data class Success(
        val remoteId: String,
        val version: Long
    ) : SyncResult()

    data class Error(
        val message: String
    ) : SyncResult()
}

sealed class UpdateResult {

    data class Success(
        val version: Long
    ) : UpdateResult()

    /**
     * 409 from PATCH. The server sends its current state in the
     * response body, so the caller can reconcile without a re-fetch.
     */
    data class Conflict(
        val server: FileResponse
    ) : UpdateResult()

    data class Error(
        val message: String
    ) : UpdateResult()
}

/**
 * Non-2xx detail, kept whole so failures are diagnosable from logcat
 * rather than collapsing into one indistinguishable message.
 */
data class HttpErrorDetail(

    val code: Int,

    /** `message` off the server's ApiResponse, when the body is ours. */
    val serverMessage: String?,

    /** Raw body, truncated — may be HTML from a proxy, or empty. */
    val body: String?
) {

    override fun toString(): String {

        val message =
            serverMessage
                ?.let { " \"$it\"" }
                ?: ""

        return "HTTP $code$message body=${body ?: "<empty>"}"
    }
}

sealed class DeleteResult {

    data object Success : DeleteResult()

    /**
     * 409: the version sent along did not match the server's. Trash,
     * restore and permanent delete all carry `?version=`, so a stale
     * local copy is rejected the same way a PATCH would be.
     *
     * [server] is the current state off the response body, which is
     * enough to retry with the right version. Null when the body was
     * missing or unparseable — a proxy can answer with HTML.
     */
    data class Conflict(
        val detail: HttpErrorDetail,
        val server: FileResponse?
    ) : DeleteResult()

    /**
     * 404: the server has no such file. Distinguished from other
     * failures because retrying will never succeed.
     */
    data class NotFound(
        val detail: HttpErrorDetail
    ) : DeleteResult()

    /** Any other non-2xx, or a 2xx carrying success = false. */
    data class HttpError(
        val detail: HttpErrorDetail
    ) : DeleteResult()

    /** Never reached the server, or the response could not be read. */
    data class Transport(
        val message: String
    ) : DeleteResult()
}
