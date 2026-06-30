package com.pongploydev.videowatchfeature.domain.repository

import com.pongploydev.videowatchfeature.callbacks.CallbackListVideoV1

interface VideoListRepository {
    suspend fun getVideos(
        cacheControl: String?,
        sort: String,
        count: Int,
        page: Int
    ): Result<CallbackListVideoV1>
}
