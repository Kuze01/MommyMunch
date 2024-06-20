package com.dicoding.mommymunch.data.api

import com.dicoding.mommymunch.data.response.ResponseAnalyze
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("/predict/")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ResponseAnalyze>
}
