package com.example.myapplication.ui.theme.network

import com.example.myapplication.ui.theme.dto.ApiResponse
import com.example.myapplication.ui.theme.dto.CreateFileRequest
import com.example.myapplication.ui.theme.dto.FileResponse
import com.example.myapplication.ui.theme.dto.UpdateFileRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(

        @Part file: MultipartBody.Part,

        @Part("firebaseUid")
        firebaseUid: RequestBody,

        @Part("isStarred")
        isStarred: RequestBody

    ): ApiResponse<FileResponse>

    @GET("api/files/{uid}")
    suspend fun getFiles(

        @Path("uid")
        uid: String

    ): ApiResponse<List<FileResponse>>

    @PUT("api/files/{id}")
    suspend fun updateFile(

        @Path("id")
        id: String,

        @Body request: UpdateFileRequest

    ): ApiResponse<Unit>

    @DELETE("api/files/{uid}/{id}")
    suspend fun deleteFile(

        @Path("uid")
        uid: String,

        @Path("id")
        remoteId: String

    ): ApiResponse<Unit>


}