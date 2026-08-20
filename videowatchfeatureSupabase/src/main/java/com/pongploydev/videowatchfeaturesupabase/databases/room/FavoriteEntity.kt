package com.pongploydev.videowatchfeaturesupabase.databases.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pongploydev.videowatchfeaturesupabase.model.Video

@Entity(tableName = "tbl_favorite")
data class FavoriteEntity(
        @PrimaryKey val vid: String = "",
        val category_name: String = "",
        val video_title: String = "",
        val video_url: String = "",
        val video_id: String = "",
        val video_thumbnail: String = "",
        val video_duration: String = "",
        val video_description: String = "",
        val video_type: String = "",
        val total_views: Long = 0,
        val date_time: String = ""
) {
    fun toVideo(): Video {
        val video = Video()
        video.vid = this.vid
        video.category_name = this.category_name
        video.video_title = this.video_title
        video.video_url = this.video_url
        video.video_id = this.video_id
        video.video_thumbnail = this.video_thumbnail
        video.video_duration = this.video_duration
        video.video_description = this.video_description
        video.video_type = this.video_type
        video.total_views = this.total_views
        video.date_time = this.date_time
        video.isFavorite = true
        return video
    }

    companion object {
        fun fromVideo(video: Video): FavoriteEntity {
            return FavoriteEntity(
                    vid = video.vid,
                    category_name = video.category_name,
                    video_title = video.video_title,
                    video_url = video.video_url,
                    video_id = video.video_id,
                    video_thumbnail = video.video_thumbnail,
                    video_duration = video.video_duration,
                    video_description = video.video_description,
                    video_type = video.video_type,
                    total_views = video.total_views,
                    date_time = video.date_time
            )
        }
    }
}
