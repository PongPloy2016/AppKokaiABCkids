package com.pongploydev.videowatchfeature.callbacks

import com.google.gson.annotations.SerializedName
import com.pongploydev.videowatchfeature.model.Video
import java.io.Serializable

data class CallbackVideoDetailV1(
    val status: String = "",
    val post: Video? = null,
    // Keep posts for compatibility if needed, though detail API returns 'post'
    val posts: List<Video> = emptyList()
) : Serializable
