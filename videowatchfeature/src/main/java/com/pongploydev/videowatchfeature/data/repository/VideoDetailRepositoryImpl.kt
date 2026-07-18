package com.pongploydev.videowatchfeature.data.repository

import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeature.data.remote.ApiInterfaceV1
import com.pongploydev.videowatchfeature.domain.repository.VideoDetailRepository

class VideoDetailRepositoryImpl(
    private val apiInterfaceV1: ApiInterfaceV1
) : VideoDetailRepository {

    override suspend fun getVideoDetail(id: String, isRefresh: Boolean): Result<CallbackVideoDetailV1> {
        return try {
            val cacheControl = if (isRefresh) "no-cache" else null
            val detail = apiInterfaceV1.getVideoDetailV1(cacheControl, id)
            if (detail.status == "ok" && (detail.post != null || detail.posts.isNotEmpty())) {
                Result.success(detail)
            } else {
                Result.failure(Exception("API returned invalid status or empty data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
