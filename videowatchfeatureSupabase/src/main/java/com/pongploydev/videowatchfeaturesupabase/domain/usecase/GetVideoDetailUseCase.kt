package com.pongploydev.videowatchfeaturesupabase.domain.usecase

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeaturesupabase.domain.repository.VideoDetailRepository

class GetVideoDetailUseCase(private val repository: VideoDetailRepository) {

    suspend operator fun invoke(id: String, isRefresh: Boolean = false): Result<CallbackVideoDetailV1> {
        return repository.getVideoDetail(id, isRefresh)
    }
}
