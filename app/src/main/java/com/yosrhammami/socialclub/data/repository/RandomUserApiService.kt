package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.remote.RandomUserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApiService {

    @GET("api/")
    suspend fun getPeople(@Query("results") count: Int = 20): RandomUserResponse
}