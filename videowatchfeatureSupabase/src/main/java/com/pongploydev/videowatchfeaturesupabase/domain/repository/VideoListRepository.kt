package com.pongploydev.videowatchfeaturesupabase.domain.repository

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideoV1

interface VideoListRepository {
    suspend fun getVideos(
        cacheControl: String?,
        sort: String,
        count: Int,
        page: Int,
        useSupabase: Boolean = true
    ): Result<CallbackListVideoV1>

    suspend fun getRandomVideos(
        cacheControl: String?,
        useSupabase: Boolean = true
    ): Result<CallbackListVideoV1>
}
