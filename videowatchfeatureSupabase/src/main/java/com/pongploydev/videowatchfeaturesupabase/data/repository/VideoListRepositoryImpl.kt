package com.pongploydev.videowatchfeaturesupabase.data.repository

import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideoV1
import com.pongploydev.videowatchfeaturesupabase.data.remote.ApiInterfaceV1
import com.pongploydev.videowatchfeaturesupabase.data.remote.SupabaseClient
import com.pongploydev.videowatchfeaturesupabase.domain.repository.VideoListRepository
import com.pongploydev.videowatchfeaturesupabase.model.Video

class VideoListRepositoryImpl(private val apiInterfaceV1: ApiInterfaceV1) : VideoListRepository {

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
                val response = SupabaseClient.getService().getVideos(limit = count, offset = offset)

                if (response.isSuccessful) {
                    val supabaseVideos = response.body().orEmpty()
                    val curated =
                            com.pongploydev.videowatchfeaturesupabase.viewmodel.VideoListViewModel
                                    .getCuratedKidVideos()

                    // รวมวิดีโอเด็กคุณภาพสูงและวิดีโอจาก Supabase เข้าด้วยกัน
                    // ให้มีข้อมูลครบทุกหมวดเสมอ
                    val combined = mutableListOf<Video>()
                    combined.addAll(curated)
                    val existingVids = combined.map { it.vid }.toSet()
                    val newSupabase = supabaseVideos.filter { it.vid !in existingVids }
                    combined.addAll(newSupabase)

                    val offset = (page - 1) * count
                    val paged: List<Video> =
                            if (offset < combined.size) {
                                combined.subList(offset, minOf(offset + count, combined.size))
                            } else {
                                emptyList<Video>()
                            }

                    val callback =
                            CallbackListVideoV1(
                                    status = "ok",
                                    count = paged.size,
                                    countTotal = combined.size,
                                    pages =
                                            if (count > 0) (combined.size + count - 1) / count
                                            else 1,
                                    posts = paged
                            )
                    return Result.success(callback)
                }
            } catch (e: Exception) {
                // Supabase error, fall through to curated Thai kid videos
            }

            // ถ้าใช้ Supabase แต่ยังไม่มีข้อมูลหรือเน็ตหลุด
            // ให้ใช้ชุดวิดีโอเด็กไทยคุณภาพสูงแทนการดึงเซิร์ฟเวอร์เก่า
            val curated =
                    com.pongploydev.videowatchfeaturesupabase.viewmodel.VideoListViewModel
                            .getCuratedKidVideos()
            val offset = (page - 1) * count
            val paged =
                    if (offset < curated.size) {
                        curated.subList(offset, minOf(offset + count, curated.size))
                    } else {
                        emptyList()
                    }
            return Result.success(
                    CallbackListVideoV1(
                            status = "ok",
                            count = paged.size,
                            countTotal = curated.size,
                            pages = if (count > 0) (curated.size + count - 1) / count else 1,
                            posts = paged
                    )
            )
        }

        // 2. ถ้าปิด Supabase ให้ดึงจาก ApiInterfaceV1 เดิม
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
                        val callback =
                                CallbackListVideoV1(
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

            val shuffled =
                    com.pongploydev.videowatchfeaturesupabase.viewmodel.VideoListViewModel
                            .getCuratedKidVideos()
                            .shuffled()
            return Result.success(
                    CallbackListVideoV1(
                            status = "ok",
                            count = shuffled.size,
                            countTotal = shuffled.size,
                            pages = 1,
                            posts = shuffled
                    )
            )
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
