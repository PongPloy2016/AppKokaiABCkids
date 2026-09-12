package com.pongploydev.videowatchfeaturesupabase.data.repository

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideoV1
import com.pongploydev.videowatchfeaturesupabase.data.remote.ApiInterfaceV1
import com.pongploydev.videowatchfeaturesupabase.data.remote.SupabaseClient
import com.pongploydev.videowatchfeaturesupabase.domain.repository.VideoListRepository

class VideoListRepositoryImpl(
    private val apiInterfaceV1: ApiInterfaceV1
) : VideoListRepository {

    override suspend fun getVideos(
        cacheControl: String?,
        sort: String,
        count: Int,
        page: Int,
        useSupabase: Boolean
    ): Result<CallbackListVideoV1> {
        if (useSupabase) {
            try {
                val offset = (page - 1) * count
                val response = SupabaseClient.getService().getVideos(
                    limit = count,
                    offset = offset
                )

                if (response.isSuccessful) {
                    val supabaseVideos = response.body() ?: emptyList()
                    val contentRange = response.headers()["content-range"] // e.g. "0-9/75"
                    val totalFromHeader = contentRange?.split("/")?.lastOrNull()?.toIntOrNull()

                    val estimatedTotal = totalFromHeader ?: if (supabaseVideos.size >= count) {
                        offset + supabaseVideos.size + 1
                    } else {
                        offset + supabaseVideos.size
                    }

                    val callback = CallbackListVideoV1(
                        status = "ok",
                        count = supabaseVideos.size,
                        countTotal = estimatedTotal,
                        pages = if (count > 0) (estimatedTotal + count - 1) / count else 1,
                        posts = supabaseVideos
                    )
                    return Result.success(callback)
                }
            } catch (e: Exception) {
                // Log หรือดำเนินการต่อเพื่อลอง Legacy API
            }
        }

        // 2. ถ้าปิด Supabase หรือ Supabase เกิดข้อผิดพลาด ให้ดึงจาก ApiInterfaceV1 เดิม
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

    override suspend fun getRandomVideos(
        cacheControl: String?,
        useSupabase: Boolean
    ): Result<CallbackListVideoV1> {
        if (useSupabase) {
            try {
                val response = SupabaseClient.getService().getVideos(limit = 50)
                if (response.isSuccessful) {
                    val supabaseVideos = response.body() ?: emptyList()
                    if (supabaseVideos.isNotEmpty()) {
                        val shuffled = supabaseVideos.shuffled()
                        val callback = CallbackListVideoV1(
                            status = "ok",
                            count = shuffled.size,
                            countTotal = shuffled.size,
                            pages = 1,
                            posts = shuffled
                        )
                        return Result.success(callback)
                    }
                }
            } catch (e: Exception) {
                // fallback
            }
        }

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

