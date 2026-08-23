package com.pranavgoyal.datanest.data.remote

import com.pranavgoyal.datanest.data.remote.dto.ApiResponse
import com.pranavgoyal.datanest.data.remote.dto.FileResponse
import com.pranavgoyal.datanest.data.remote.dto.SyncPageResponse
import com.pranavgoyal.datanest.data.remote.dto.UpdateFileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(

        @Part file: MultipartBody.Part,

        @Part("isStarred")
        isStarred: RequestBody

    ): Response<ApiResponse<FileResponse>>

    @GET("api/files/sync")
    suspend fun syncDelta(

        @Query("cursor")
        cursor: String?,

        @Query("limit")
        limit: Int

    ): Response<ApiResponse<SyncPageResponse>>

    @PATCH("api/files/{id}")
    suspend fun updateFile(

        @Path("id")
        id: String,

        @Body request: UpdateFileRequest

    ): Response<ApiResponse<FileResponse>>

    @POST("api/files/{id}/trash")
    suspend fun trashFile(

        @Path("id")
        id: String,

        @Query("version")
        version: Long

    ): Response<ApiResponse<FileResponse>>

    @POST("api/files/{id}/restore")
    suspend fun restoreFile(

        @Path("id")
        id: String,

        @Query("version")
        version: Long

    ): Response<ApiResponse<FileResponse>>

    @DELETE("api/files/{id}")
    suspend fun deleteFilePermanently(

        @Path("id")
        id: String,

        @Query("version")
        version: Long

    ): Response<ApiResponse<Unit>>

}
