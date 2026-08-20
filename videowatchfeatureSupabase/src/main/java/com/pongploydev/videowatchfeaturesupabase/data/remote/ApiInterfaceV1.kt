package com.pongploydev.videowatchfeaturesupabase.data.remote

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideoV1
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterfaceV1 {

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    @GET("api/videolist/{id}")
    suspend fun getVideoDetailV1(
        @Header("Cache-Control") cacheControl: String?,
        @Path("id") id: String
    ): CallbackVideoDetailV1

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    @GET("api/videolist")
    suspend fun getVideosV1(
        @Header("Cache-Control") cacheControl: String?,
        @Query("sort") sort: String,
        @Query("count") count: Int,
        @Query("page") page: Int
    ): CallbackListVideoV1

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    @GET("api/videoListRandom")
    suspend fun getVideosRandomV1(
        @Header("Cache-Control") cacheControl: String?
    ): CallbackListVideoV1
}
