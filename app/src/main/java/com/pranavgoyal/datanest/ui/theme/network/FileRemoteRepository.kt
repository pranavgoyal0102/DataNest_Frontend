package com.pranavgoyal.datanest.ui.theme.network

import android.content.Context
import android.util.Log
import com.pranavgoyal.datanest.ui.theme.dto.ApiResponse
import com.pranavgoyal.datanest.ui.theme.dto.FileResponse
import com.pranavgoyal.datanest.ui.theme.dto.UpdateFileRequest
import com.pranavgoyal.datanest.ui.theme.models.DeleteResult
import com.pranavgoyal.datanest.ui.theme.models.DeltaResult
import com.pranavgoyal.datanest.ui.theme.models.FileStored
import com.pranavgoyal.datanest.ui.theme.models.HttpErrorDetail
import com.pranavgoyal.datanest.ui.theme.models.SyncResult
import com.pranavgoyal.datanest.ui.theme.models.UpdateResult
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class FileRemoteRepository {

    private val gson = Gson()

    suspend fun uploadFile(
        context: Context,
        file: FileStored
    ): SyncResult {

        // Cheap pre-flight, so an oversized file fails at once instead of
        // after pushing the whole body up only to be turned down. The
        // server stays the authority — size is 0 when the picker could
        // not report one, and the multipart envelope adds a little on top
        // — so the 413 branch below is still the one that decides.
        if (file.size > MAX_UPLOAD_BYTES) {

            return SyncResult.Error(
                "${file.title} is ${file.size / BYTES_PER_MB}MB, over the " +
                        "${MAX_UPLOAD_BYTES / BYTES_PER_MB}MB upload limit"
            )
        }

        return try {

            // Streamed off the ContentResolver rather than read into a
            // ByteArray, which OOMed on large files.
            val requestBody =
                ContentUriRequestBody(
                    context = context,
                    uri = Uri.parse(file.uri),
                    mediaType = file.mimeType.toMediaTypeOrNull(),
                    declaredSize = file.size
                )

            val multipart =
                MultipartBody.Part.createFormData(
                    "file",
                    file.title,
                    requestBody
                )

            val starredBody =
                file.isStarred
                    .toString()
                    .toRequestBody(
                        "text/plain".toMediaTypeOrNull()
                    )

            val response =
                RetrofitInstance.api.uploadFile(
                    multipart,
                    starredBody
                )

            val body = response.body()

            if (response.isSuccessful && body?.success == true) {

                val uploaded =
                    body.data
                        ?: return SyncResult.Error("No remoteId")

                SyncResult.Success(
                    remoteId = uploaded.id,
                    version = uploaded.version
                )

            } else if (response.isSuccessful) {

                // 2xx carrying success = false, or an envelope with no
                // data. Reported with the code so it is not mistaken for
                // a transport fault.
                SyncResult.Error(
                    body?.message
                        ?: "HTTP ${response.code()} with no body"
                )

            } else {

                val detail =
                    errorDetail(
                        response.code(),
                        readErrorBody(response)
                    )

                if (response.code() == 413) {

                    SyncResult.Error(
                        "${file.title} was rejected as too large — $detail"
                    )

                } else {

                    SyncResult.Error(detail.toString())
                }
            }

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Upload failed for ${file.title}",
                e
            )

            // Keeps the class name: a body the server cuts off mid-write
            // throws an IOException whose message is often null, which
            // used to surface as a bare "Unknown error".
            SyncResult.Error(
                "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            )
        }
    }



    /**
     * Fetches one page of the delta feed. [cursor] is the previous
     * page's cursor, or null on a first sync, which asks for everything.
     */
    suspend fun syncDelta(
        cursor: String?,
        limit: Int
    ): DeltaResult {

        return try {

            val response =
                RetrofitInstance.api.syncDelta(
                    cursor,
                    limit
                )

            val body = response.body()

            val page = body?.data

            if (
                response.isSuccessful &&
                body?.success == true &&
                page != null
            ) {

                DeltaResult.Page(page)

            } else if (response.isSuccessful) {

                // 2xx carrying success = false, or an envelope with no
                // data. Reported with the code so it is not mistaken
                // for a transport fault.
                DeltaResult.HttpError(
                    HttpErrorDetail(
                        code = response.code(),
                        serverMessage = body?.message,
                        body = null
                    )
                )

            } else {

                DeltaResult.HttpError(
                    errorDetail(
                        response.code(),
                        readErrorBody(response)
                    )
                )
            }

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Delta request failed at cursor=$cursor",
                e
            )

            DeltaResult.Transport(
                "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            )
        }
    }

    suspend fun updateFile(
        file: FileStored
    ): UpdateResult {

        val remoteId =
            file.remoteId
                ?: return UpdateResult.Error("No remoteId")

        return try {

            val response =
                RetrofitInstance.api.updateFile(
                    remoteId,
                    UpdateFileRequest(
                        title = file.title,
                        isDeleted = file.isDeleted,
                        isStarred = file.isStarred,
                        version = file.version
                    )
                )

            if (response.code() == 409) {

                val server =
                    parseConflict(
                        response.errorBody()?.string()
                    )

                return if (server == null) {

                    UpdateResult.Error(
                        "Conflict without server state"
                    )

                } else {

                    UpdateResult.Conflict(server)
                }
            }

            val body = response.body()

            if (response.isSuccessful && body?.success == true) {

                UpdateResult.Success(
                    version =
                    body.data?.version
                        ?: file.version
                )

            } else {

                UpdateResult.Error(
                    body?.message
                        ?: "HTTP ${response.code()}"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Update failed",
                e
            )

            UpdateResult.Error(
                e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Soft delete — moves the file to trash server-side.
     */
    suspend fun trashFile(
        remoteId: String,
        version: Long
    ): DeleteResult {

        return try {

            classify(
                RetrofitInstance.api.trashFile(
                    remoteId,
                    version
                )
            )

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Trash request failed for $remoteId",
                e
            )

            DeleteResult.Transport(
                "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            )
        }
    }

    /**
     * Lifts a file back out of trash server-side.
     */
    suspend fun restoreFile(
        remoteId: String,
        version: Long
    ): DeleteResult {

        return try {

            classify(
                RetrofitInstance.api.restoreFile(
                    remoteId,
                    version
                )
            )

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Restore request failed for $remoteId",
                e
            )

            DeleteResult.Transport(
                "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            )
        }
    }

    /**
     * Hard delete — destroys the file outright. A stale [version] is a
     * 409, and omitting it is a 400.
     */
    suspend fun deleteFilePermanently(
        remoteId: String,
        version: Long
    ): DeleteResult {

        return try {

            classify(
                RetrofitInstance.api.deleteFilePermanently(
                    remoteId,
                    version
                )
            )

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Permanent delete request failed for $remoteId",
                e
            )

            DeleteResult.Transport(
                "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            )
        }
    }

    /**
     * Maps one delete response onto [DeleteResult], keeping the status
     * code and body on every failure path.
     */
    private fun <T> classify(
        response: Response<ApiResponse<T>>
    ): DeleteResult {

        if (response.isSuccessful) {

            val body = response.body()

            // A 2xx with success = false is still a rejection; report it
            // with the code so it is not mistaken for a transport fault.
            return if (body == null || body.success) {

                DeleteResult.Success

            } else {

                DeleteResult.HttpError(
                    HttpErrorDetail(
                        code = response.code(),
                        serverMessage = body.message,
                        body = null
                    )
                )
            }
        }

        // Read once and reuse — the error body is a one-shot stream, so
        // parsing it after building the detail would come back empty.
        val raw = readErrorBody(response)

        val detail =
            errorDetail(
                response.code(),
                raw
            )

        return when (response.code()) {

            409 -> DeleteResult.Conflict(
                detail,
                parseConflict(raw)
            )

            404 -> DeleteResult.NotFound(detail)

            else -> DeleteResult.HttpError(detail)
        }
    }

    /** One-shot: the stream is spent once read, so callers must reuse. */
    private fun readErrorBody(
        response: Response<*>
    ): String? {

        return try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            Log.e(
                "SYNC",
                "Could not read error body",
                e
            )
            null
        }
    }

    private fun errorDetail(
        code: Int,
        raw: String?
    ): HttpErrorDetail {

        return HttpErrorDetail(
            code = code,
            serverMessage = serverMessage(raw),
            body =
            raw
                ?.take(MAX_BODY_CHARS)
                ?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Pulls `message` off the body when it is one of ours. Parsed
     * leniently — a proxy or container can answer with HTML instead.
     */
    private fun serverMessage(
        raw: String?
    ): String? {

        if (raw.isNullOrBlank()) {
            return null
        }

        return try {

            gson.fromJson(raw, JsonObject::class.java)
                ?.get("message")
                ?.takeIf { it.isJsonPrimitive }
                ?.asString

        } catch (e: Exception) {
            null
        }
    }

    private fun parseConflict(
        body: String?
    ): FileResponse? {

        if (body.isNullOrBlank()) {
            return null
        }

        return try {

            val type =
                object : TypeToken<ApiResponse<FileResponse>>() {}.type

            gson.fromJson<ApiResponse<FileResponse>>(
                body,
                type
            ).data

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Could not parse 409 body",
                e
            )

            null
        }
    }

    private companion object {

        /** Enough for a JSON error; keeps an HTML page out of logcat. */
        const val MAX_BODY_CHARS = 1000

        const val BYTES_PER_MB = 1024 * 1024

        /**
         * Mirrors the server's spring.servlet.multipart.max-file-size of
         * 100MB. Only a pre-flight — if the two ever drift apart, the
         * server's 413 is what the caller ends up reporting.
         */
        const val MAX_UPLOAD_BYTES = 100L * BYTES_PER_MB
    }
}
