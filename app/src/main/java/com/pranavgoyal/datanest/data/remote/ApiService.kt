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

/**
 * Every call is authenticated by [AuthInterceptor]; the server derives the
 * uid from the verified token, so nothing here passes firebaseUid.
 */
interface ApiService {

    /**
     * Wrapped in [Response] so a rejection keeps its status code instead
     * of arriving as a bare HttpException — the one that matters here is
     * 413, which is how the server turns down a file over its
     * max-file-size.
     */
    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(

        @Part file: MultipartBody.Part,

        @Part("isStarred")
        isStarred: RequestBody

    ): Response<ApiResponse<FileResponse>>

    /**
     * One page of rows changed since [cursor], which is the cursor off
     * the previous page. Null on the first sync — Retrofit drops a null
     * query param, and no cursor asks for everything. Enveloped like
     * every other endpoint, so the page itself is under `data`.
     */
    @GET("api/files/sync")
    suspend fun syncDelta(

        @Query("cursor")
        cursor: String?,

        @Query("limit")
        limit: Int

    ): Response<ApiResponse<SyncPageResponse>>

    /**
     * Wrapped in [Response] so a 409 can be read off the error body —
     * the server returns its current [FileResponse] there on a stale
     * version, which is enough to reconcile without a re-fetch.
     */
    @PATCH("api/files/{id}")
    suspend fun updateFile(

        @Path("id")
        id: String,

        @Body request: UpdateFileRequest

    ): Response<ApiResponse<FileResponse>>

    /**
     * Soft delete — moves the file to trash. Wrapped in [Response] so a
     * rejection keeps its status code and body instead of arriving as a
     * bare HttpException.
     *
     * Returns the stored file so the caller can pick up the version the
     * server bumped to; omitting [version] is a 400.
     */
    @POST("api/files/{id}/trash")
    suspend fun trashFile(

        @Path("id")
        id: String,

        @Query("version")
        version: Long

    ): Response<ApiResponse<FileResponse>>

    /**
     * Lifts a file back out of trash. Same version rules as [trashFile].
     */
    @POST("api/files/{id}/restore")
    suspend fun restoreFile(

        @Path("id")
        id: String,

        @Query("version")
        version: Long

    ): Response<ApiResponse<FileResponse>>

    /**
     * Hard delete — destroys the file outright, so this is never the
     * call for an ordinary delete. [trashFile] is the soft one.
     */
    @DELETE("api/files/{id}")
    suspend fun deleteFilePermanently(

        @Path("id")
        id: String,

        @Query("version")
        version: Long

    ): Response<ApiResponse<Unit>>

}
