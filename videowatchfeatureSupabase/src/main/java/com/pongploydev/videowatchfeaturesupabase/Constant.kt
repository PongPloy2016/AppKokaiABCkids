package com.pongploydev.videowatchfeaturesupabase

import android.graphics.Bitmap
import java.io.Serializable

class Constant : Serializable {
    companion object {
        const val AD_TYPE_ADMOB = "admob"
        const val AD_TYPE_FACEBOOK = "facebook"
        const val AD_TYPE_STARTAPP = "startapp"
        const val AD_TYPE_APPLOVIN = "applovin"

        @JvmField var profileBitmap: Bitmap? = null
        @JvmField var newCommentCount = 0
        @JvmField var isCommentMadeFinal = false
        const val defaultTabs = 2
        const val HIDE_THRESHOLD = 20

        // global variables
        const val IMMEDIATE_APP_UPDATE_REQ_CODE = 124
        const val POSITION = "POSITION_ID"
        const val EXTRA_OBJC = "key.EXTRA_OBJC"
        const val EXTRA_OBJC_CAT = "key.EXTRA_OBJC_CAT"
        const val KEY_VIDEO_CATEGORY_ID = "category_id"
        const val KEY_VIDEO_CATEGORY_NAME = "category_name"
        const val KEY_VID = "vid"
        const val KEY_VIDEO_TITLE = "video_title"
        const val KEY_VIDEO_URL = "video_url"
        const val KEY_VIDEO_ID = "video_id"
        const val KEY_VIDEO_THUMBNAIL = "video_thumbnail"
        const val KEY_VIDEO_DURATION = "video_duration"
        const val KEY_VIDEO_DESCRIPTION = "video_description"
        const val KEY_VIDEO_TYPE = "video_type"
        const val KEY_VIDEO_SIZE = "size"
        const val KEY_TOTAL_VIEWS = "total_views"
        const val KEY_DATE_TIME = "date_time"
        const val YOUTUBE_IMAGE_FRONT = "http://img.youtube.com/vi/"
        const val YOUTUBE_IMAGE_BACK_MQ = "/mqdefault.jpg"
        const val YOUTUBE_IMAGE_BACK_HQ = "/hqdefault.jpg"
        const val TOPIC_GLOBAL = "global"
        const val REGISTRATION_COMPLETE = "registrationComplete"
        const val PUSH_NOTIFICATION = "pushNotification"
        const val SHARED_PREF = "ah_firebase"
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_ID_BIG_IMAGE = 101
        const val DELAY_TIME = 250L
        const val MAX_SEARCH_RESULT = 100
        const val NOTIFICATION_CHANNEL_NAME = "videos_channel_01"

        const val MOST_POPULAR = "n.total_views DESC"
        const val ADDED_NEWEST = "n.id DESC"
        const val ADDED_OLDEST = "n.id ASC"

        const val VIDEO_LIST_DEFAULT = 0
        const val VIDEO_LIST_COMPACT = 1

        const val CATEGORY_LIST = 0
        const val CATEGORY_GRID_2_COLUMN = 1
        const val CATEGORY_GRID_3_COLUMN = 2
        const val APP_OPEN_AD_ON_START = true
        const val APP_OPEN_AD_ON_RESUME = true

        const val LOCALHOST_ADDRESS = "10.0.2.2"

        @JvmField var APP_OPEN_AD_TIMEOUT = 100
        const val DELAY_ACTION_CLICK = 250

        const val NATIVE_AD_STYLE_SMALL = "small"
        const val NATIVE_AD_STYLE_MEDIUM = "medium"
        const val NATIVE_AD_STYLE_LARGE = "large"

        const val FONT_SIZE_XSMALL = 12
        const val FONT_SIZE_SMALL = 14
        const val FONT_SIZE_MEDIUM = 16
        const val FONT_SIZE_LARGE = 18
        const val FONT_SIZE_XLARGE = 20

        @JvmField var isAppOpen = false
        const val NATIVE_AD_STYLE_POST_DETAILS = "large"
        const val NATIVE_AD_STYLE_EXIT_DIALOG = "medium"

        const val YOUTUBE_IMG_FRONT = "https://img.youtube.com/vi/"
        const val YOUTUBE_IMG_BACK = "/mqdefault.jpg"
    }
}
