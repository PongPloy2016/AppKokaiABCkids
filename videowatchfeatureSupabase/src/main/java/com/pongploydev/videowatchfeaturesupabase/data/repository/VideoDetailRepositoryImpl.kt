package com.pongploydev.videowatchfeaturesupabase.data.repository

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeaturesupabase.data.remote.ApiInterfaceV1
import com.pongploydev.videowatchfeaturesupabase.data.remote.SupabaseClient
import com.pongploydev.videowatchfeaturesupabase.domain.repository.VideoDetailRepository

class VideoDetailRepositoryImpl(
    private val apiInterfaceV1: ApiInterfaceV1
) : VideoDetailRepository {

    override suspend fun getVideoDetail(id: String, isRefresh: Boolean): Result<CallbackVideoDetailV1> {
        // 1. ลองดึงข้อมูลจาก Supabase ก่อน
        try {
            val response = when {
                id.startsWith("VID", ignoreCase = true) -> {
                    val byVidResp = SupabaseClient.getService().getVideoDetail(vidFilter = "eq.$id")
                    val listByVid = byVidResp.body()
                    if (byVidResp.isSuccessful && !listByVid.isNullOrEmpty()) {
                        byVidResp
                    } else {
                        val numPart = id.substring(3).trimStart('0').ifEmpty { "0" }
                        val numId = numPart.toIntOrNull()
                        if (numId != null) {
                            SupabaseClient.getService().getVideoDetail(idFilter = "eq.$numId")
                        } else null
                    }
                }
                id.toIntOrNull() != null -> {
                    SupabaseClient.getService().getVideoDetail(idFilter = "eq.$id")
                }
                else -> {
                    SupabaseClient.getService().getVideoDetail(vidFilter = "eq.$id")
                }
            }

            val list = response?.body() ?: emptyList()
            if (list.isNotEmpty()) {
                val video = list[0]
                return Result.success(
                    CallbackVideoDetailV1(
                        status = "ok",
                        post = video,
                        posts = list
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. ถ้าไม่ได้ ให้ลอง ApiInterfaceV1 เดิม
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

