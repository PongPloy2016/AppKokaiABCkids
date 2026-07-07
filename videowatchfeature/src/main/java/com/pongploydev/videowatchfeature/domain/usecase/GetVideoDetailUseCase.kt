package com.pongploydev.videowatchfeature.domain.usecase

import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeature.domain.repository.VideoDetailRepository

class GetVideoDetailUseCase(private val repository: VideoDetailRepository) {

    suspend operator fun invoke(id: String, isRefresh: Boolean = false): Result<CallbackVideoDetailV1> {
        return repository.getVideoDetail(id, isRefresh)
    }
}
