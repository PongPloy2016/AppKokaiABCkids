package com.pongploydev.videowatchfeature.data.remote

import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeature.callbacks.CallbackListVideoV1
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterfaceV1 {

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    @GET("api/v1/videos")
    suspend fun getVideoDetailV1(
        @Header("Cache-Control") cacheControl: String?,
        @Query("id") id: String
    ): CallbackVideoDetailV1

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    @GET("api/v1/videos")
    suspend fun getVideosV1(
        @Header("Cache-Control") cacheControl: String?,
        @Query("sort") sort: String,
        @Query("count") count: Int,
        @Query("page") page: Int
    ): CallbackListVideoV1
}
