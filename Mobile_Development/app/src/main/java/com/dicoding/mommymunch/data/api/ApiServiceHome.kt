package com.dicoding.mommymunch.data.api

import com.dicoding.mommymunch.data.response.ResponseHomeFoodItem
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ApiServiceHome {
    @GET("data")
    suspend fun getData(): Response<List<ResponseHomeFoodItem>>
}