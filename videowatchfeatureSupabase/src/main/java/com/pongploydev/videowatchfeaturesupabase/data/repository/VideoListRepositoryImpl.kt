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
            // 1. ลองดึงข้อมูลจาก Supabase ก่อน
            try {
                val offset = (page - 1) * count
                val supabaseVideos = SupabaseClient.getService().getVideos(
                    categoryFilter = "ilike.*Education*",
                    limit = count,
                    offset = offset
                )

                if (supabaseVideos.isNotEmpty()) {
                    val callback = CallbackListVideoV1(
                        status = "ok",
                        count = supabaseVideos.size,
                        countTotal = supabaseVideos.size,
                        pages = 1,
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
                val supabaseVideos = SupabaseClient.getService().getVideos(
                    categoryFilter = "ilike.*Education*"
                )
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

