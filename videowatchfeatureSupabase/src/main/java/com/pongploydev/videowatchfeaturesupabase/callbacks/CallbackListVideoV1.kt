package com.pongploydev.videowatchfeaturesupabase.callbacks

import com.google.gson.annotations.SerializedName
import com.pongploydev.videowatchfeaturesupabase.model.Video
import java.io.Serializable

data class CallbackListVideoV1(
    val status: String = "",
    val count: Int = 0,
    @SerializedName("count_total")
    val countTotal: Int = 0,
    val pages: Int = 0,
    val posts: List<Video> = emptyList()
) : Serializable
