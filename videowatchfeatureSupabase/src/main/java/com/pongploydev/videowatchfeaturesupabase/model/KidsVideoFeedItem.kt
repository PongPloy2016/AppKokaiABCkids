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
) {
    companion object {
        val DEFAULT_FEEDS = listOf(
            KidsVideoFeedItem(
                id = "thaipbs_kids_youtube",
                name = "Thai PBS Kids",
                icon = "🎬",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg",
                provider = "Thai PBS Kids (YouTube)",
                category = "video",
                description = "วิดีโอสร้างสรรค์ การทดลอง นิทาน และการเรียนรู้แสนสนุกสำหรับเด็กและครอบครัว",
                isKidsFilterRequired = false,
                sortOrder = 5
            ),
            KidsVideoFeedItem(
                id = "thaipbs_youtube_main",
                name = "วิดีโอรวม",
                icon = "📺",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw",
                provider = "Thai PBS (YouTube)",
                category = "video",
                description = "รายการเด็ก นิทาน ละครเด็ก และการเรียนรู้ คัดเฉพาะเนื้อหาที่เหมาะสมสำหรับเด็ก",
                isKidsFilterRequired = true,
                sortOrder = 6
            ),
            KidsVideoFeedItem(
                id = "thaipbs_kids_programs",
                name = "Thai PBS Kids",
                icon = "🌟",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg",
                provider = "Thai PBS Kids",
                category = "video",
                description = "แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์, หม้อข้าวหม้อแกง",
                isKidsFilterRequired = false,
                sortOrder = 7
            ),
            KidsVideoFeedItem(
                id = "thaipbs_talk_about_kids",
                name = "คุยกับลูก",
                icon = "👨‍👩‍👧",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw",
                provider = "Thai PBS",
                category = "video",
                description = "รายการวิดีโอพัฒนาการเด็ก สมอง EF และความรู้สร้างเสริมสายสัมพันธ์ครอบครัว",
                isKidsFilterRequired = false,
                sortOrder = 8
            ),
            KidsVideoFeedItem(
                id = "story_feed",
                name = "นิทาน",
                icon = "📖",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UC103Yv2QS7C2Vf17XIMk0AQ",
                provider = "Indysong Kids",
                category = "video",
                description = "นิทานสอนใจ นิทานอีสป และเพลงเด็กสร้างสรรค์จาก Indysong Kids",
                isKidsFilterRequired = false,
                sortOrder = 9
            )
        )
    }
}

