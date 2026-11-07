package com.pongploydev.videowatchfeaturesupabase.callbacks

import com.google.gson.annotations.SerializedName
import com.pongploydev.videowatchfeaturesupabase.model.Video
import java.io.Serializable

class CallbackListVideo : Serializable {
    @JvmField var status: String? = null
    @JvmField var count: Int = 0
    @JvmField var count_total: Int = 0
    @JvmField var pages: Int = 0
    @SerializedName("posts")
    @JvmField var posts: List<Video>? = null
}

