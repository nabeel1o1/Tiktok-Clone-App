package com.example.tiktok.interfaces

import com.example.tiktok.model.HomeFeed
import com.example.tiktok.utilies.Constants.TIKTOK_HOME_FEED
import retrofit2.http.GET
import retrofit2.http.Query

interface TiktokApiService {

    @GET(TIKTOK_HOME_FEED)
    suspend fun getHomeFeed(@Query("page") page: Int): HomeFeed
}