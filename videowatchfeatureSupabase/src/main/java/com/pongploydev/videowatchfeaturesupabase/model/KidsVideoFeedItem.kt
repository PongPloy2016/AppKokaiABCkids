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
                id = "thaipbs_kids_programs",
                name = "Thai PBS Kids",
                icon = "🌟",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?playlist_id=PL78EDk_9jZAH5Nw5Tly6tGP46Mn_qYQtI",
                provider = "Thai PBS Kids",
                category = "video",
                description = "แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์, หม้อข้าวหม้อแกง",
                isKidsFilterRequired = false,
                sortOrder = 7
            ),
            KidsVideoFeedItem(
                id = "kids_cartoon_feed",
                name = "การ์ตูนเด็ก",
                icon = "🎨",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?playlist_id=PL78EDk_9jZAEnJzU6CIacF5pk2R8t8PbO",
                provider = "Thai PBS Kids Animation",
                category = "video",
                description = "การ์ตูนและแอนิเมชันสำหรับเด็ก",
                isKidsFilterRequired = false,
                sortOrder = 10
            ),
            KidsVideoFeedItem(
                id = "kids_song_feed",
                name = "เพลงเด็ก",
                icon = "🎵",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?playlist_id=PLRkCAAfpKzZRTxTyR3zo-IdITnIRnlv3V",
                provider = "KidsMeSong",
                category = "video",
                description = "เพลงเด็ก เพลงท่องศัพท์ เพลงประกอบท่าทาง",
                isKidsFilterRequired = false,
                sortOrder = 11
            ),
            KidsVideoFeedItem(
                id = "kids_science_feed",
                name = "วิทยาศาสตร์น้อย",
                icon = "🔬",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?playlist_id=PL78EDk_9jZAG0CS1_nSeW4qpwmOTXnATy",
                provider = "บ้านนักวิทยาศาสตร์น้อย",
                category = "video",
                description = "การทดลองและความรู้วิทยาศาสตร์สำหรับเด็ก",
                isKidsFilterRequired = false,
                sortOrder = 12
            ),
            KidsVideoFeedItem(
                id = "kids_bedtime_story_feed",
                name = "นิทานก่อนนอน",
                icon = "🌙",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?playlist_id=PLYSzQdnk6q9ZrhdSXFx2Wz9m3gv3L2nOj",
                provider = "นิทานอีสปก่อนนอน",
                category = "video",
                description = "นิทานก่อนนอนสำหรับเด็ก",
                isKidsFilterRequired = false,
                sortOrder = 13
            )
        )
    }
}

