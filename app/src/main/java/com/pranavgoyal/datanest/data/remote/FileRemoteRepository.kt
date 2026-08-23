package com.pranavgoyal.datanest.data.remote

import android.content.Context
import android.util.Log
import com.pranavgoyal.datanest.data.remote.dto.ApiResponse
import com.pranavgoyal.datanest.data.remote.dto.FileResponse
import com.pranavgoyal.datanest.data.remote.dto.UpdateFileRequest
import com.pranavgoyal.datanest.sync.DeleteResult
import com.pranavgoyal.datanest.sync.DeltaResult
import com.pranavgoyal.datanest.data.local.FileStored
import com.pranavgoyal.datanest.sync.HttpErrorDetail
import com.pranavgoyal.datanest.sync.SyncResult
import com.pranavgoyal.datanest.sync.UpdateResult
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

            SyncResult.Error(
                "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            )
        }
    }

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

    private fun <T> classify(
        response: Response<ApiResponse<T>>
    ): DeleteResult {

        if (response.isSuccessful) {

            val body = response.body()

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

        const val MAX_BODY_CHARS = 1000

        const val BYTES_PER_MB = 1024 * 1024

        const val MAX_UPLOAD_BYTES = 100L * BYTES_PER_MB
    }
}
