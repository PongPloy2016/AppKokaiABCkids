package com.pongploydev.videowatchfeature.data.repository

import com.pongploydev.videowatchfeature.callbacks.CallbackListVideoV1
import com.pongploydev.videowatchfeature.data.remote.ApiInterfaceV1
import com.pongploydev.videowatchfeature.domain.repository.VideoListRepository

class VideoListRepositoryImpl(
    private val apiInterfaceV1: ApiInterfaceV1
) : VideoListRepository {
    override suspend fun getVideos(
        cacheControl: String?,
        sort: String,
        count: Int,
        page: Int
    ): Result<CallbackListVideoV1> {
        return try {
            val response = apiInterfaceV1.getVideosV1(cacheControl, sort, count, page)
            if (response.status == "ok") {
                Result.success(response)
            } else {
                Result.failure(Exception("API returned status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomVideos(cacheControl: String?): Result<CallbackListVideoV1> {
        return try {
            val response = apiInterfaceV1.getVideosRandomV1(cacheControl)
            if (response.status == "ok") {
                Result.success(response)
            } else {
                Result.failure(Exception("API returned status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
