package com.pongploydev.videowatchfeature.domain.usecase

import com.pongploydev.videowatchfeature.callbacks.CallbackListVideoV1
import com.pongploydev.videowatchfeature.domain.repository.VideoListRepository

class GetVideoListUseCase(
    private val repository: VideoListRepository
) {
    suspend operator fun invoke(
        cacheControl: String?,
        sort: String,
        count: Int,
        page: Int
    ): Result<CallbackListVideoV1> {
        return repository.getVideos(cacheControl, sort, count, page)
    }
}
