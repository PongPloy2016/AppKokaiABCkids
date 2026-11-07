package com.pongploydev.videowatchfeaturesupabase.callbacks

import com.google.gson.annotations.SerializedName
import com.pongploydev.videowatchfeaturesupabase.model.Video
import java.io.Serializable
import java.util.ArrayList

class CallbackVideoDetail : Serializable {
    @JvmField var status: String = ""
    @JvmField var post: Video? = null
    @SerializedName("suggested")
    @JvmField var suggested: List<Video> = ArrayList()
}

