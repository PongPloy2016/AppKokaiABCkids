package com.pongploydev.videowatchfeaturesupabase.data.remote

import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem
import com.pongploydev.videowatchfeaturesupabase.model.Video
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SupabaseApiService {

    @GET("rest/v1/kids_video_feeds")
    suspend fun getKidsVideoFeeds(
        @Header("apikey") apiKey: String = AppConfig.SUPABASE_ANON_KEY,
        @Header("Authorization") auth: String = "Bearer " + AppConfig.SUPABASE_ANON_KEY,
        @Query("select") select: String = "*",
        @Query("is_active") isActive: String = "eq.true",
        @Query("order") order: String = "sort_order.asc"
    ): Response<List<KidsVideoFeedItem>>

    @GET("rest/v1/videos")
    suspend fun getVideos(
        @Header("apikey") apiKey: String = AppConfig.SUPABASE_ANON_KEY,
        @Header("Authorization") auth: String = "Bearer " + AppConfig.SUPABASE_ANON_KEY,
        @Header("Prefer") prefer: String = "count=exact",
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc",
        @Query("or") orFilter: String? = null,
        @Query("category_name") categoryFilter: String? = null,
        @Query("cat_id") catIdFilter: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<List<Video>>

    @GET("rest/v1/videos")
    suspend fun getVideoDetail(
        @Header("apikey") apiKey: String = AppConfig.SUPABASE_ANON_KEY,
        @Header("Authorization") auth: String = "Bearer " + AppConfig.SUPABASE_ANON_KEY,
        @Query("id") idFilter: String? = null, // e.g. "eq.1"
        @Query("vid") vidFilter: String? = null, // e.g. "eq.VID001"
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 1
    ): Response<List<Video>>

    @GET("rest/v1/videos")
    suspend fun getVideosByCategory(
        @Header("apikey") apiKey: String = AppConfig.SUPABASE_ANON_KEY,
        @Header("Authorization") auth: String = "Bearer " + AppConfig.SUPABASE_ANON_KEY,
        @Query("category_name") categoryFilter: String, // e.g. "ilike.*Spark*"
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Response<List<Video>>
}
