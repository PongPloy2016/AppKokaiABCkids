package com.pongploydev.videowatchfeaturesupabase.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import java.util.Locale

class Video : Parcelable {
    @JvmField var id: Int = 0

    @SerializedName("cat_id")
    @JvmField var cat_id: Int = 0

    @SerializedName("category_name")
    @JvmField var category_name: String = ""

    @SerializedName("vid")
    @JvmField var vid: String = ""

    @SerializedName("video_title")
    @JvmField var video_title: String = ""

    @SerializedName("video_url")
    @JvmField var video_url: String = ""

    @SerializedName("video_id")
    @JvmField var video_id: String = ""

    @SerializedName("video_thumbnail")
    @JvmField var video_thumbnail: String = ""

    @SerializedName("video_duration")
    @JvmField var video_duration: String = ""

    @SerializedName("video_description")
    @JvmField var video_description: String = ""

    @SerializedName("video_type")
    @JvmField var video_type: String = ""

    @SerializedName("size")
    @JvmField var size: String = ""

    @SerializedName("total_views")
    @JvmField var total_views: Long = 0

    @SerializedName("date_time")
    @JvmField var date_time: String = ""

    @JvmField var isNew: Boolean = false
    @JvmField var tags: String = ""
    @JvmField var age_rating: String = ""
    @JvmField var language: String = ""
    @JvmField var rating: Float = 0.0f
    @JvmField var resolution: String = ""
    @JvmField var isFavorite: Boolean = false
    @JvmField var watch_count: Int = 0
    @JvmField var last_watched: String = ""

    constructor()

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        cat_id = parcel.readInt()
        category_name = parcel.readString() ?: ""
        vid = parcel.readString() ?: ""
        video_title = parcel.readString() ?: ""
        video_url = parcel.readString() ?: ""
        video_id = parcel.readString() ?: ""
        video_thumbnail = parcel.readString() ?: ""
        video_duration = parcel.readString() ?: ""
        video_description = parcel.readString() ?: ""
        video_type = parcel.readString() ?: ""
        size = parcel.readString() ?: ""
        total_views = parcel.readLong()
        date_time = parcel.readString() ?: ""
        isNew = parcel.readByte() != 0.toByte()
        tags = parcel.readString() ?: ""
        age_rating = parcel.readString() ?: ""
        language = parcel.readString() ?: ""
        rating = parcel.readFloat()
        resolution = parcel.readString() ?: ""
        isFavorite = parcel.readByte() != 0.toByte()
        watch_count = parcel.readInt()
        last_watched = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(cat_id)
        parcel.writeString(category_name)
        parcel.writeString(vid)
        parcel.writeString(video_title)
        parcel.writeString(video_url)
        parcel.writeString(video_id)
        parcel.writeString(video_thumbnail)
        parcel.writeString(video_duration)
        parcel.writeString(video_description)
        parcel.writeString(video_type)
        parcel.writeString(size)
        parcel.writeLong(total_views)
        parcel.writeString(date_time)
        parcel.writeByte(if (isNew) 1 else 0)
        parcel.writeString(tags)
        parcel.writeString(age_rating)
        parcel.writeString(language)
        parcel.writeFloat(rating)
        parcel.writeString(resolution)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeInt(watch_count)
        parcel.writeString(last_watched)
    }

    override fun describeContents(): Int = 0

    fun matchesSearch(query: String?): Boolean {
        if (TextUtils.isEmpty(query)) return true
        val lowercaseQuery = query!!.lowercase(Locale.getDefault())
        if (!TextUtils.isEmpty(video_title) && video_title.lowercase(Locale.getDefault()).contains(lowercaseQuery)) return true
        if (!TextUtils.isEmpty(video_description) && video_description.lowercase(Locale.getDefault()).contains(lowercaseQuery)) return true
        if (!TextUtils.isEmpty(category_name) && category_name.lowercase(Locale.getDefault()).contains(lowercaseQuery)) return true
        if (!TextUtils.isEmpty(tags) && tags.lowercase(Locale.getDefault()).contains(lowercaseQuery)) return true
        return false
    }

    fun isInCategory(category: String?): Boolean {
        if (TextUtils.isEmpty(category) || "all".equals(category, ignoreCase = true)) return true
        val lowercaseCategory = category!!.lowercase(Locale.getDefault())
        val textToSearch = "${category_name} ${video_title} ${tags} ${video_description}".lowercase(Locale.getDefault())
        if (textToSearch.contains(lowercaseCategory)) return true

        return when (category.lowercase(Locale.getDefault())) {
            "cartoon" -> textToSearch.contains("การ์ตูน") || textToSearch.contains("cartoon") || textToSearch.contains("animation") || textToSearch.contains("นิทาน")
            "music" -> textToSearch.contains("เพลง") || textToSearch.contains("music") || textToSearch.contains("song")
            "learning", "math" -> textToSearch.contains("เรียนรู้") || textToSearch.contains("learning") || textToSearch.contains("education") || textToSearch.contains("สอน") || textToSearch.contains("math") || textToSearch.contains("คณิต") || textToSearch.contains("บวกเลข") || textToSearch.contains("คูณ") || textToSearch.contains("หาร")
            "abc", "english" -> textToSearch.contains("abc") || textToSearch.contains("english") || textToSearch.contains("ภาษาอังกฤษ") || textToSearch.contains("อักษร") || textToSearch.contains("ก ไก่") || textToSearch.contains("พยัญชนะ") || textToSearch.contains("สระ") || textToSearch.contains("ภาษาไทย") || textToSearch.contains("phonics") || textToSearch.contains("alphabet")
            "story" -> textToSearch.contains("นิทาน") || textToSearch.contains("story") || textToSearch.contains("tale") || textToSearch.contains("เรื่อง")
            else -> false
        }
    }

    fun matchesFilters(query: String?, category: String?, showNewOnly: Boolean, minRating: Float): Boolean {
        if (!matchesSearch(query)) return false
        if (!isInCategory(category)) return false
        if (showNewOnly && !isNew) return false
        if (rating < minRating) return false
        return true
    }

    fun getFormattedViewCount(): String = VideoHelper.formatViewCount(total_views)
    fun getFormattedDuration(): String = VideoHelper.formatDuration(video_duration)

    fun incrementViewCount() {
        total_views++
        watch_count++
    }

    fun toggleFavorite() {
        isFavorite = !isFavorite
    }

    fun getCategoryColor(): String = VideoHelper.getCategoryColor(category_name)
    fun isYouTubeVideo(): Boolean = "youtube".equals(video_type, ignoreCase = true)
    fun isLocalVideo(): Boolean = "local".equals(video_type, ignoreCase = true) || "mp4".equals(video_type, ignoreCase = true) || (!isYouTubeVideo() && video_url.lowercase(Locale.getDefault()).endsWith(".mp4"))

    override fun toString(): String {
        return "Video(id=$id, video_title='$video_title', category_name='$category_name', total_views=$total_views, isNew=$isNew, rating=$rating, isFavorite=$isFavorite)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Video) return false
        if (id != other.id) return false
        return vid == other.vid
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + vid.hashCode()
        return result
    }

    companion object {
        const val CATEGORY_CARTOON = "cartoon"
        const val CATEGORY_MUSIC = "music"
        const val CATEGORY_LEARNING = "learning"
        const val CATEGORY_STORY = "story"

        @JvmField
        val CREATOR: Parcelable.Creator<Video> = object : Parcelable.Creator<Video> {
            override fun createFromParcel(parcel: Parcel): Video = Video(parcel)
            override fun newArray(size: Int): Array<Video?> = arrayOfNulls(size)
        }
    }

    object VideoHelper {
        @JvmStatic
        fun formatViewCount(viewCount: Long): String {
            return when {
                viewCount < 1000 -> viewCount.toString()
                viewCount < 1000000 -> String.format(Locale.getDefault(), "%.1fK", viewCount / 1000.0)
                else -> String.format(Locale.getDefault(), "%.1fM", viewCount / 1000000.0)
            }
        }

        @JvmStatic
        fun formatDuration(duration: String?): String {
            if (TextUtils.isEmpty(duration)) return "00:00"
            return try {
                val totalSeconds = duration!!.toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            } catch (e: NumberFormatException) {
                duration!!
            }
        }

        @JvmStatic
        fun isVideoNew(dateTime: String?): Boolean = Math.random() > 0.7

        @JvmStatic
        fun getCategoryColor(category: String?): String {
            if (TextUtils.isEmpty(category)) return "#4CAF50"
            val lowercaseCategory = category!!.lowercase(Locale.getDefault())
            return when {
                lowercaseCategory.contains("การ์ตูน") || lowercaseCategory.contains("cartoon") -> "#2196F3"
                lowercaseCategory.contains("เพลง") || lowercaseCategory.contains("music") -> "#E91E63"
                lowercaseCategory.contains("เรียนรู้") || lowercaseCategory.contains("learning") -> "#4CAF50"
                lowercaseCategory.contains("นิทาน") || lowercaseCategory.contains("story") -> "#9C27B0"
                else -> "#FF9800"
            }
        }
    }
}
