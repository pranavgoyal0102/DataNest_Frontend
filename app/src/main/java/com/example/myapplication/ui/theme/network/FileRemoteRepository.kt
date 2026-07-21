package com.example.myapplication.ui.theme.network

import android.content.Context
import android.util.Log
import com.example.myapplication.ui.theme.dto.FileResponse
import com.example.myapplication.ui.theme.dto.UpdateFileRequest
import com.example.myapplication.ui.theme.models.FileStored
import com.example.myapplication.ui.theme.models.SyncResult
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class FileRemoteRepository {

    suspend fun uploadFile(
        context: Context,
        file: FileStored,
        firebaseUid: String
    ): SyncResult {

        return try {

            val uri = Uri.parse(file.uri)

            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: return SyncResult.Error("Cannot open file")

            val bytes =
                inputStream.readBytes()

            inputStream.close()

            val requestBody =
                bytes.toRequestBody(
                    file.mimeType.toMediaTypeOrNull()
                )

            val multipart =
                MultipartBody.Part.createFormData(
                    "file",
                    file.title,
                    requestBody
                )

            val firebaseBody =
                firebaseUid.toRequestBody(
                    "text/plain".toMediaTypeOrNull()
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
                    firebaseBody,
                    starredBody
                )

            if (response.success) {

                val remoteId =
                    response.data?.id
                        ?: return SyncResult.Error("No remoteId")

                SyncResult.Success(remoteId)

            } else {

                SyncResult.Error(response.message)

            }

        } catch (e: Exception) {

            Log.e("SYNC", "Upload failed", e)

            SyncResult.Error(
                e.message ?: "Unknown error"
            )
        }
    }



    suspend fun downloadFiles(
        firebaseUid: String
    ): List<FileResponse> {

        return try {

            val response =
                RetrofitInstance.api.getFiles(
                    firebaseUid
                )

            response.data ?: emptyList()

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Download failed",
                e
            )

            emptyList()
        }
    }

    suspend fun updateFile(
        file: FileStored
    ): Boolean {

        val remoteId =
            file.remoteId
                ?: return false

        return try {

            val response =
                RetrofitInstance.api.updateFile(
                    remoteId,
                    UpdateFileRequest(
                        title = file.title,
                        isDeleted = file.isDeleted,
                        isStarred = file.isStarred
                    )
                )

            response.success

        } catch (e: Exception) {

            Log.e(
                "SYNC",
                "Update failed",
                e
            )

            false
        }
    }

    suspend fun deleteFile(
        firebaseUid: String,
        remoteId: String
    ): Boolean {

        return try {

            RetrofitInstance.api.deleteFile(
                firebaseUid,
                remoteId
            )

            true

        } catch (e: Exception) {

            false
        }
    }
}