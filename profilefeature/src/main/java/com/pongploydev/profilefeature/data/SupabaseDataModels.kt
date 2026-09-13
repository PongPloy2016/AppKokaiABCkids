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
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class UserProfileUpdate(
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("total_stars") val totalStars: Int? = null,
    @SerializedName("total_coins") val totalCoins: Int? = null,
    @SerializedName("current_level") val currentLevel: Int? = null,
    @SerializedName("streak_days") val streakDays: Int? = null
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
