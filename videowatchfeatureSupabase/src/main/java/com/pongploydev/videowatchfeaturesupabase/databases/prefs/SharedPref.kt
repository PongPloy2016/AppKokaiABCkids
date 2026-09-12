package com.pongploydev.videowatchfeaturesupabase.databases.prefs

import android.content.Context
import android.content.SharedPreferences
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig

class SharedPref(val context: Context) {
    var sharedPreferences: SharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }

    var isLogin: Boolean
        @JvmName("getIsLogin") get() = sharedPreferences.getBoolean("login", false)
        @JvmName("setIsLogin") set(value) {
            editor.putBoolean("login", value)
            editor.apply()
        }

    var isDarkTheme: Boolean
        @JvmName("getIsDarkTheme") get() = sharedPreferences.getBoolean("theme", false)
        @JvmName("setIsDarkTheme") set(value) {
            editor.putBoolean("theme", value)
            editor.apply()
        }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor.commit()
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true)
    }

    var baseUrl: String
        get() = sharedPreferences.getString("base_url", "https://php-laravel-app-hslg.onrender.com") ?: "https://php-laravel-app-hslg.onrender.com"
        set(value) {
            editor.putString("base_url", value)
            editor.apply()
        }

    var isUseSupabaseApi: Boolean
        @JvmName("getIsUseSupabaseApi") get() = sharedPreferences.getBoolean("use_supabase_api", true)
        @JvmName("setUseSupabaseApi") set(value) {
            editor.putBoolean("use_supabase_api", value)
            editor.apply()
        }

    fun getUseSupabaseApi(): Boolean = isUseSupabaseApi

    fun saveConfig(
        ytApiKey: String?,
        moreAppUrl: String?,
        privacyPolicy: String?,
        redirectUrl: String?,
        pushNotificationProvider: String?
    ) {
        editor.putString("yt_api_key", ytApiKey)
        editor.putString("more_apps_url", moreAppUrl)
        editor.putString("privacy_policy", privacyPolicy)
        editor.putString("redirect_url", redirectUrl)
        editor.putString("providers", pushNotificationProvider)
        editor.apply()
    }

    val ytApiKey: String
        @JvmName("getYtApiKey") get() = sharedPreferences.getString("yt_api_key", "") ?: ""

    val moreAppsUrl: String
        @JvmName("getMoreAppsUrl") get() = sharedPreferences.getString("more_apps_url", "") ?: ""

    val privacyPolicy: String
        @JvmName("getPrivacyPolicy") get() = sharedPreferences.getString("privacy_policy", "") ?: ""

    val redirectUrl: String
        @JvmName("getRedirectUrl") get() = sharedPreferences.getString("redirect_url", "") ?: ""

    val pushNotificationProvider: String
        @JvmName("getPushNotificationProvider") get() = sharedPreferences.getString("providers", "") ?: ""

    fun setDefaultSortHome() {
        editor.putInt("sort", 0)
        editor.apply()
    }

    val currentSortHome: Int
        @JvmName("getCurrentSortHome") get() = sharedPreferences.getInt("sort", 0)

    fun updateSortHome(position: Int) {
        editor.putInt("sort", position)
        editor.apply()
    }

    fun setDefaultSortVideos() {
        editor.putInt("sort_act", 2)
        editor.apply()
    }

    val currentSortVideos: Int
        @JvmName("getCurrentSortVideos") get() = sharedPreferences.getInt("sort_act", 0)

    fun updateSortVideos(position: Int) {
        editor.putInt("sort_act", position)
        editor.apply()
    }

    val videoViewType: Int
        @JvmName("getVideoViewType") get() = sharedPreferences.getInt("video_list", AppConfig.DEFAULT_VIDEO_VIEW_TYPE)

    fun updateVideoViewType(position: Int) {
        editor.putInt("video_list", position)
        editor.apply()
    }

    fun updateCategoryViewType(position: Int) {
        editor.putInt("category_list", position)
        editor.apply()
    }

    val inAppReviewToken: Int
        @JvmName("getInAppReviewToken") get() = sharedPreferences.getInt("in_app_review_token", 0)

    fun updateInAppReviewToken(value: Int) {
        editor.putInt("in_app_review_token", value)
        editor.apply()
    }

    val loginFeature: String
        @JvmName("getLoginFeature") get() = sharedPreferences.getString("login_feature", "yes") ?: "yes"

    val fontSize: Int
        @JvmName("getFontSize") get() = sharedPreferences.getInt("font_size", 2)

    fun updateFontSize(font_size: Int) {
        editor.putInt("font_size", font_size)
        editor.apply()
    }
}
