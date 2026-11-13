package com.pongploydev.videowatchfeaturesupabase.model

import com.google.gson.annotations.SerializedName

/**
 * โมเดลตัวแทนข้อมูลฟีดวิดีโอจากตาราง public.kids_video_feeds บน Supabase
 */
data class KidsVideoFeedItem(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("feed_url")
    val feedUrl: String = "",

    @SerializedName("provider")
    val provider: String = "",

    @SerializedName("category")
    val category: String = "video",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("is_kids_filter_required")
    val isKidsFilterRequired: Boolean = false,

    @SerializedName("sort_order")
    val sortOrder: Int = 0,

    @SerializedName("is_active")
    val isActive: Boolean = true
)
