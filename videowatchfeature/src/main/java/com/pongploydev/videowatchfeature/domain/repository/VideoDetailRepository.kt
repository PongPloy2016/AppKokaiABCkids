package com.pongploydev.videowatchfeature.domain.repository

import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1

interface VideoDetailRepository {
    suspend fun getVideoDetail(id: String, isRefresh: Boolean): Result<CallbackVideoDetailV1>
}
