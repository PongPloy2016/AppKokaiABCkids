package com.pongploydev.videowatchfeaturesupabase.domain.usecase

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideoV1
import com.pongploydev.videowatchfeaturesupabase.domain.repository.VideoListRepository

class GetVideoListUseCase(
    private val repository: VideoListRepository
) {
    suspend operator fun invoke(
        cacheControl: String?,
        sort: String,
        count: Int,
        page: Int,
        useSupabase: Boolean = true
    ): Result<CallbackListVideoV1> {
        return repository.getVideos(cacheControl, sort, count, page, useSupabase)
    }
}
