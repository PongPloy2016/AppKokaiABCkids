package com.pongploydev.videowatchfeaturesupabase.domain.repository

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetailV1

interface VideoDetailRepository {
    suspend fun getVideoDetail(id: String, isRefresh: Boolean): Result<CallbackVideoDetailV1>
}
