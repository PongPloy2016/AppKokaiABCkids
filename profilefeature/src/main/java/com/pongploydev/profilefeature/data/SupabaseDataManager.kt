package com.pongploydev.profilefeature.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SupabaseDataManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: SupabaseDataManager? = null

        fun getInstance(context: Context): SupabaseDataManager {
            return instance ?: synchronized(this) {
                instance ?: SupabaseDataManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val authManager = SupabaseAuthManager.getInstance(context)
    private val gson = Gson()

    private val dataApiService: SupabaseDataApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val baseUrl = if (SupabaseAuthManager.SUPABASE_URL.endsWith("/")) {
            SupabaseAuthManager.SUPABASE_URL
        } else {
            "${SupabaseAuthManager.SUPABASE_URL}/"
        }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SupabaseDataApiService::class.java)
    }

    private fun getAuthHeader(): String {
        val token = authManager.getAccessToken() ?: SupabaseAuthManager.SUPABASE_ANON_KEY
        return "Bearer $token"
    }

    // ==================== PROFILE ====================

    suspend fun fetchUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getProfile(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                idFilter = "eq.$userId"
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                // If profile doesn't exist yet in Supabase, auto-create (upsert) default profile!
                val defaultProfile = UserProfile(
                    id = userId,
                    displayName = "นักอ่านตัวน้อย",
                    totalStars = 0,
                    currentLevel = 1
                )
                upsertUserProfile(defaultProfile).getOrDefault(defaultProfile)
                Result.success(defaultProfile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(userId: String, updates: UserProfileUpdate): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.updateProfile(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                idFilter = "eq.$userId",
                updates = updates
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                // Row didn't exist in Supabase database; construct full profile and upsert!
                val existing = fetchUserProfile(userId).getOrDefault(UserProfile(id = userId, displayName = "นักอ่านตัวน้อย"))
                val newProfile = UserProfile(
                    id = userId,
                    displayName = updates.displayName ?: existing.displayName,
                    avatarUrl = existing.avatarUrl,
                    totalStars = updates.totalStars ?: existing.totalStars,
                    currentLevel = updates.currentLevel ?: existing.currentLevel
                )
                upsertUserProfile(newProfile)
            }
        } catch (e: Exception) {
            val existing = UserProfile(
                id = userId,
                displayName = updates.displayName ?: "นักอ่านตัวน้อย",
                avatarUrl = "",
                totalStars = updates.totalStars ?: 0,
                currentLevel = updates.currentLevel ?: 1
            )
            upsertUserProfile(existing)
        }
    }

    suspend fun upsertUserProfile(profile: UserProfile): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.upsertProfile(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                profile = profile
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                Result.failure(Exception("Failed to upsert profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStarsAndLevel(userId: String, currentStars: Int, currentLevel: Int, starsToAdd: Int): Result<UserProfile> {
        val fetchedProf = fetchUserProfile(userId).getOrNull()
        val baseStars = fetchedProf?.totalStars ?: currentStars
        val newStars = baseStars + starsToAdd
        val newLevel = 1 + (newStars / 50) // Every 50 stars = +1 level
        val updates = UserProfileUpdate(totalStars = newStars, currentLevel = newLevel)
        return updateUserProfile(userId, updates)
    }

    suspend fun uploadAvatarImage(userId: String, imageBytes: ByteArray, mimeType: String = "image/jpeg"): Result<String> = withContext(Dispatchers.IO) {
        val tag = "SupabaseAvatarUpload"
        Log.d(tag, "=== [START AVATAR UPLOAD] ===")
        Log.d(tag, "UserId: $userId, Image size: ${imageBytes.size} bytes, MimeType: $mimeType")

        try {
            val cleanId = userId.replace(Regex("[^a-zA-Z0-9]"), "")
            val fileName = "avatar_${cleanId}_${System.currentTimeMillis()}.jpg"
            val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())

            // 1. Try uploading to candidate Supabase Storage buckets ("avatars", "files", "profiles")
            val candidateBuckets = listOf("avatars", "files", "profiles")
            var publicUrl: String? = null

            for (bucket in candidateBuckets) {
                Log.d(tag, "Attempting upload to Supabase Storage bucket: '$bucket' with fileName: '$fileName'...")
                try {
                    val storageResp = dataApiService.uploadStorageObject(
                        apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                        auth = getAuthHeader(),
                        contentType = mimeType,
                        upsert = "true",
                        bucket = bucket,
                        path = fileName,
                        body = requestBody
                    )

                    val code = storageResp.code()
                    val msg = storageResp.message()

                    if (storageResp.isSuccessful) {
                        publicUrl = "${SupabaseAuthManager.SUPABASE_URL}/storage/v1/object/public/$bucket/$fileName"
                        Log.d(tag, "SUCCESS! Uploaded to Storage bucket '$bucket' ($code $msg). Public URL: $publicUrl")
                        break
                    } else {
                        val errorBody = storageResp.errorBody()?.string() ?: "No error body"
                        Log.w(tag, "Failed uploading to Storage bucket '$bucket' ($code $msg): $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Exception during upload to bucket '$bucket': ${e.localizedMessage}", e)
                }
            }

            // 2. Fallback to compact Base64 data URL if storage bucket fails or doesn't exist
            val finalAvatarUrl = publicUrl ?: run {
                Log.w(tag, "Storage bucket upload returned null. Fallback to Base64 data URL encoding...")
                val base64Str = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                val dataUrl = "data:$mimeType;base64,$base64Str"
                Log.d(tag, "Base64 data URL created successfully (length: ${dataUrl.length} chars)")
                dataUrl
            }

            // 3. Update user_profiles record in Supabase PostgreSQL (qriqerrkxccpnwabphdc)
            Log.d(tag, "Updating user_profiles table in Supabase PostgreSQL for userId: '$userId'...")
            val updateResult = updateUserProfile(userId, UserProfileUpdate(avatarUrl = finalAvatarUrl))
            if (updateResult.isSuccess) {
                Log.d(tag, "=== [SUCCESS AVATAR UPLOAD] Avatar URL saved to user_profiles table ===")
                Result.success(finalAvatarUrl)
            } else {
                Log.w(tag, "updateUserProfile failed, executing upsertUserProfile...")
                val existing = fetchUserProfile(userId).getOrDefault(UserProfile(id = userId))
                val newProfile = existing.copy(avatarUrl = finalAvatarUrl)
                val upsertRes = upsertUserProfile(newProfile)
                if (upsertRes.isSuccess) {
                    Log.d(tag, "=== [SUCCESS AVATAR UPLOAD] Upsert profile completed ===")
                } else {
                    Log.e(tag, "Upsert profile failed: ${upsertRes.exceptionOrNull()?.localizedMessage}")
                }
                Result.success(finalAvatarUrl)
            }
        } catch (e: Exception) {
            Log.e(tag, "=== [ERROR AVATAR UPLOAD] Exception: ${e.localizedMessage} ===", e)
            Result.failure(e)
        }
    }

    // ==================== LEARNING PROGRESS ====================

    suspend fun fetchLearningProgress(userId: String): Result<List<UserLearningProgress>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getLearningProgress(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch learning progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveLearningProgress(
        userId: String,
        categoryKey: String,
        itemKey: String,
        score: Int
    ): Result<UserLearningProgress> = withContext(Dispatchers.IO) {
        try {
            val item = UserLearningProgress(
                userId = userId,
                categoryKey = categoryKey,
                itemKey = itemKey,
                score = score,
                isCompleted = score >= 80,
                timesPracticed = 1
            )
            val response = dataApiService.upsertLearningProgress(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                progress = item
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                Result.failure(Exception("Failed to save learning progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== QUIZ RESULTS ====================

    suspend fun fetchQuizResults(userId: String): Result<List<UserQuizResult>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getQuizResults(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch quiz results"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveQuizResult(
        userId: String,
        topicId: Int,
        topicName: String,
        score: Int,
        totalQuestions: Int = 10,
        timeSpentSeconds: Int = 60
    ): Result<UserQuizResult> = withContext(Dispatchers.IO) {
        try {
            val result = UserQuizResult(
                userId = userId,
                quizTopicId = topicId,
                quizTopicName = topicName,
                score = score,
                totalQuestions = totalQuestions,
                timeSpentSeconds = timeSpentSeconds
            )
            val response = dataApiService.insertQuizResult(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                result = result
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                Result.failure(Exception("Failed to save quiz result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ACHIEVEMENTS ====================

    suspend fun fetchAchievements(userId: String): Result<List<UserAchievement>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getAchievements(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch achievements"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockAchievement(
        userId: String,
        badgeKey: String,
        badgeName: String,
        badgeIcon: String = "🏆"
    ): Result<UserAchievement> = withContext(Dispatchers.IO) {
        try {
            val achievement = UserAchievement(
                userId = userId,
                badgeKey = badgeKey,
                badgeName = badgeName,
                badgeIcon = badgeIcon
            )
            val response = dataApiService.unlockAchievement(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                achievement = achievement
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                Result.failure(Exception("Failed to unlock achievement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== USER DEVICES ====================

    suspend fun registerUserDevice(
        userId: String,
        deviceId: String,
        deviceName: String = android.os.Build.MODEL,
        deviceModel: String = android.os.Build.PRODUCT,
        osVersion: String = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
        appVersion: String = "1.0.0",
        fcmToken: String? = null
    ): Result<UserDevice> = withContext(Dispatchers.IO) {
        try {
            fetchUserProfile(userId)
            val device = UserDevice(
                userId = userId,
                deviceId = deviceId,
                deviceName = deviceName,
                deviceModel = deviceModel,
                osVersion = osVersion,
                appVersion = appVersion,
                fcmToken = fcmToken,
                isActive = true
            )
            val response = dataApiService.upsertDevice(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                device = device
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(response.body()!!.first())
            } else {
                Result.failure(Exception("Failed to register user device"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
