package com.pongploydev.profilefeature.data

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("display_name") val displayName: String? = "นักอ่านตัวน้อย",
    @SerializedName("avatar_url") val avatarUrl: String? = "",
    @SerializedName("role") val role: String? = "student",
    @SerializedName("total_stars") val totalStars: Int = 0,
    @SerializedName("total_coins") val totalCoins: Int = 0,
    @SerializedName("current_level") val currentLevel: Int = 1,
    @SerializedName("streak_days") val streakDays: Int = 1,
    @SerializedName("last_active_at") val lastActiveAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class UserProfileUpdate(
    @SerializedName("email") val email: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("total_stars") val totalStars: Int? = null,
    @SerializedName("total_coins") val totalCoins: Int? = null,
    @SerializedName("current_level") val currentLevel: Int? = null,
    @SerializedName("streak_days") val streakDays: Int? = null,
    @SerializedName("last_active_at") val lastActiveAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class UserLearningProgress(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("category_key") val categoryKey: String,
    @SerializedName("item_key") val itemKey: String,
    @SerializedName("score") val score: Int = 0,
    @SerializedName("is_completed") val isCompleted: Boolean = false,
    @SerializedName("times_practiced") val timesPracticed: Int = 1,
    @SerializedName("last_practiced_at") val lastPracticedAt: String? = null
)

data class UserQuizResult(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("quiz_topic_id") val quizTopicId: Int,
    @SerializedName("quiz_topic_name") val quizTopicName: String,
    @SerializedName("score") val score: Int,
    @SerializedName("total_questions") val totalQuestions: Int = 10,
    @SerializedName("time_spent_seconds") val timeSpentSeconds: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

data class UserAchievement(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("badge_key") val badgeKey: String,
    @SerializedName("badge_name") val badgeName: String,
    @SerializedName("badge_icon") val badgeIcon: String = "🏆",
    @SerializedName("unlocked_at") val unlockedAt: String? = null
)

data class UserDevice(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("device_name") val deviceName: String? = null,
    @SerializedName("device_model") val deviceModel: String? = null,
    @SerializedName("os_version") val osVersion: String? = null,
    @SerializedName("app_version") val appVersion: String? = null,
    @SerializedName("fcm_token") val fcmToken: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("last_active_at") val lastActiveAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class AppSettings(
    @SerializedName("id") val id: Int = 1,
    @SerializedName("app_version_name") val appVersionName: String = "3.0.3",
    @SerializedName("app_version_code") val appVersionCode: Int = 47,
    @SerializedName("min_supported_version_code") val minSupportedVersionCode: Int = 45,
    @SerializedName("force_update") val forceUpdate: Boolean = false,
    @SerializedName("update_title") val updateTitle: String = "มีเวอร์ชันใหม่พร้อมใช้งาน 🎉",
    @SerializedName("update_message") val updateMessage: String = "กรุณาอัปเดตแอปเพื่อรับบทเรียนและเกมใหม่ๆ",
    @SerializedName("play_store_url") val playStoreUrl: String = "",

    @SerializedName("is_subscription_enabled") val isSubscriptionEnabled: Boolean = true,
    @SerializedName("sub_monthly_id") val subMonthlyId: String = "sub_monthly",
    @SerializedName("sub_monthly_price") val subMonthlyPrice: Double = 29.0,
    @SerializedName("sub_monthly_bonus_stars") val subMonthlyBonusStars: Int = 20,

    @SerializedName("sub_yearly_id") val subYearlyId: String = "sub_yearly",
    @SerializedName("sub_yearly_price") val subYearlyPrice: Double = 199.0,
    @SerializedName("sub_yearly_bonus_stars") val subYearlyBonusStars: Int = 100,

    @SerializedName("sub_lifetime_id") val subLifetimeId: String = "sub_lifetime",
    @SerializedName("sub_lifetime_price") val subLifetimePrice: Double = 399.0,
    @SerializedName("sub_lifetime_bonus_stars") val subLifetimeBonusStars: Int = 300,

    @SerializedName("vip_stars_multiplier") val vipStarsMultiplier: Double = 2.0,

    @SerializedName("is_ads_enabled") val isAdsEnabled: Boolean = true,
    @SerializedName("is_banner_ad_enabled") val isBannerAdEnabled: Boolean = true,
    @SerializedName("is_interstitial_ad_enabled") val isInterstitialAdEnabled: Boolean = true,
    @SerializedName("ad_interstitial_interval") val adInterstitialInterval: Int = 3,

    @SerializedName("is_game_hub_enabled") val isGameHubEnabled: Boolean = true,
    @SerializedName("is_video_hub_enabled") val isVideoHubEnabled: Boolean = true,
    @SerializedName("is_sound_effects_enabled") val isSoundEffectsEnabled: Boolean = true,
    @SerializedName("is_ai_feature_enabled") val isAiFeatureEnabled: Boolean = false,

    @SerializedName("is_maintenance_mode") val isMaintenanceMode: Boolean = false,
    @SerializedName("maintenance_message") val maintenanceMessage: String = "",
    @SerializedName("is_announcement_active") val isAnnouncementActive: Boolean = false,
    @SerializedName("announcement_text") val announcementText: String = "",

    @SerializedName("contact_email") val contactEmail: String = "",
    @SerializedName("privacy_policy_url") val privacyPolicyUrl: String = "",
    @SerializedName("terms_of_service_url") val termsOfServiceUrl: String = ""
)

