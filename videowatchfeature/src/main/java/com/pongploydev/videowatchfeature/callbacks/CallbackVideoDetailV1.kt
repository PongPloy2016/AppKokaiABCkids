package com.pongploydev.videowatchfeature.callbacks

import com.google.gson.annotations.SerializedName
import com.pongploydev.videowatchfeature.model.Video

class CallbackVideoDetailV1 {
    @SerializedName("status")
    var status: Int = 0

    @SerializedName("message")
    var message: String = ""

    @SerializedName("body")
    var body: BodyData? = null

    class BodyData {
        @SerializedName("post")
        var post: Video? = null
    }
}
