package com.pranavgoyal.datanest.sync

import com.pranavgoyal.datanest.data.remote.dto.FileResponse
import com.pranavgoyal.datanest.data.remote.dto.SyncPageResponse

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

    data class Conflict(
        val server: FileResponse
    ) : UpdateResult()

    data class Error(
        val message: String
    ) : UpdateResult()
}

data class HttpErrorDetail(

    val code: Int,

    val serverMessage: String?,

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

sealed class DeltaResult {

    data class Page(
        val page: SyncPageResponse
    ) : DeltaResult()

    data class HttpError(
        val detail: HttpErrorDetail
    ) : DeltaResult()

    data class Transport(
        val message: String
    ) : DeltaResult()
}

sealed class DeleteResult {

    data object Success : DeleteResult()

    data class Conflict(
        val detail: HttpErrorDetail,
        val server: FileResponse?
    ) : DeleteResult()

    data class NotFound(
        val detail: HttpErrorDetail
    ) : DeleteResult()

    data class HttpError(
        val detail: HttpErrorDetail
    ) : DeleteResult()

    data class Transport(
        val message: String
    ) : DeleteResult()
}
