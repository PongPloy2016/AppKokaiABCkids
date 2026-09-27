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

    private val appContext = context.applicationContext
    private val authManager = SupabaseAuthManager.getInstance(context)
    private val gson = Gson()
    private val statsPrefs = appContext.getSharedPreferences("app_user_stats", Context.MODE_PRIVATE)

    // ==================== LOCAL STATS CACHE (SINGLE SOURCE OF TRUTH) ====================

    fun getUserTotalStars(): Int {
        val cached = statsPrefs.getInt("total_stars", -1)
        if (cached >= 0) return cached
        // Migration from legacy game_hub_scores
        val legacySp = appContext.getSharedPreferences("game_hub_scores", Context.MODE_PRIVATE)
        val legacyStars = legacySp.getInt("user_stars", 0)
        if (legacyStars > 0) {
            saveUserProgress(legacyStars, 1 + (legacyStars / 50))
            return legacyStars
        }
        return 0
    }

    fun getUserCurrentLevel(): Int {
        val cached = statsPrefs.getInt("current_level", -1)
        if (cached > 0) return cached
        val stars = getUserTotalStars()
        return 1 + (stars / 50)
    }

    fun getUserStreakDays(): Int {
        return statsPrefs.getInt("streak_days", 5)
    }

    fun getLocalDisplayName(): String? {
        return statsPrefs.getString("display_name", null)
    }

    fun getLocalAvatarUrl(): String? {
        return statsPrefs.getString("avatar_url", null)
    }

    fun saveUserProgress(
        stars: Int,
        level: Int = 1 + (stars / 50),
        streakDays: Int = 5,
        displayName: String? = null,
        avatarUrl: String? = null
    ) {
        val editor = statsPrefs.edit()
            .putInt("total_stars", stars)
            .putInt("current_level", maxOf(level, 1 + (stars / 50)))
            .putInt("streak_days", streakDays)
        if (!displayName.isNullOrBlank() && displayName != "คนเก่ง" && displayName != "นักอ่านตัวน้อย") {
            editor.putString("display_name", displayName)
        }
        if (!avatarUrl.isNullOrBlank()) {
            editor.putString("avatar_url", avatarUrl)
        }
        editor.apply()

        // Sync legacy game_hub_scores
        try {
            appContext.getSharedPreferences("game_hub_scores", Context.MODE_PRIVATE)
                .edit()
                .putInt("user_stars", stars)
                .putInt("user_level", maxOf(level, 1 + (stars / 50)))
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isVipSubscriber(): Boolean {
        return statsPrefs.getBoolean("is_vip_subscriber", false)
    }

    fun getSubscriptionTier(): String {
        return statsPrefs.getString("subscription_tier", "สมาชิกฟรี (Free)") ?: "สมาชิกฟรี (Free)"
    }

    fun setSubscription(tierName: String, isLifetime: Boolean = false, bonusStars: Int = 0) {
        val editor = statsPrefs.edit()
            .putBoolean("is_vip_subscriber", true)
            .putString("subscription_tier", tierName)
            .putBoolean("is_lifetime_vip", isLifetime)
            .putLong("subscribed_at", System.currentTimeMillis())
        editor.apply()

        if (bonusStars > 0) {
            val stars = getUserTotalStars() + bonusStars
            saveUserProgress(stars = stars, level = 1 + (stars / 50))
        }
    }

    suspend fun addStars(userId: String, starsToAdd: Int): Pair<Int, Int> {
        val currentLocalStars = getUserTotalStars()
        val currentLocalLevel = getUserCurrentLevel()
        val newStars = currentLocalStars + starsToAdd
        val newLevel = 1 + (newStars / 50)

        // 1. Immediately save to persistent cache
        saveUserProgress(newStars, newLevel)

        // 2. Sync to Supabase in background
        if (userId.isNotBlank()) {
            try {
                addStarsAndLevel(userId, currentLocalStars, currentLocalLevel, starsToAdd)
            } catch (e: Exception) {
                Log.w("SupabaseDataManager", "Background addStars cloud sync: ${e.message}")
            }
        }
        return Pair(newStars, newLevel)
    }

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
        val token = authManager.getAccessToken()
        return if (!token.isNullOrBlank() && token.split(".").size == 3) {
            "Bearer $token"
        } else {
            "Bearer ${SupabaseAuthManager.SUPABASE_ANON_KEY}"
        }
    }

    private fun getAnonAuthHeader(): String {
        return "Bearer ${SupabaseAuthManager.SUPABASE_ANON_KEY}"
    }

    // Helper to ensure userId matches PostgreSQL UUID format if custom or guest id string is provided
    private fun ensureValidUuid(id: String): String {
        return try {
            java.util.UUID.fromString(id)
            id
        } catch (e: Exception) {
            java.util.UUID.nameUUIDFromBytes(id.toByteArray()).toString()
        }
    }

    // ==================== PROFILE ====================

    suspend fun fetchUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val validId = ensureValidUuid(userId)
        val localStars = getUserTotalStars()
        val localLevel = getUserCurrentLevel()
        val localName = getLocalDisplayName() ?: "นักอ่านตัวน้อย"
        val localAvatar = getLocalAvatarUrl() ?: ""
        val localStreak = getUserStreakDays()

        try {
            var response = dataApiService.getProfile(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                idFilter = "eq.$validId"
            )
            if (response.code() == 401) {
                Log.w("SupabaseAvatarUpload", "fetchUserProfile 401 JWT expired. Clearing session & retrying with Anon Key...")
                authManager.signOut()
                response = dataApiService.getProfile(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader(),
                    idFilter = "eq.$validId"
                )
            }
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val cloudProfile = response.body()!!.first()
                val resolvedStars = maxOf(localStars, cloudProfile.totalStars)
                val resolvedLevel = maxOf(localLevel, cloudProfile.currentLevel, 1 + (resolvedStars / 50))
                val resolvedName = if (!cloudProfile.displayName.isNullOrBlank() && cloudProfile.displayName != "นักอ่านตัวน้อย") {
                    cloudProfile.displayName
                } else {
                    localName
                }
                val resolvedAvatar = if (!cloudProfile.avatarUrl.isNullOrBlank()) cloudProfile.avatarUrl else localAvatar

                saveUserProgress(
                    stars = resolvedStars,
                    level = resolvedLevel,
                    streakDays = maxOf(localStreak, cloudProfile.streakDays),
                    displayName = resolvedName,
                    avatarUrl = resolvedAvatar
                )

                val mergedProfile = cloudProfile.copy(
                    displayName = resolvedName,
                    avatarUrl = resolvedAvatar,
                    totalStars = resolvedStars,
                    currentLevel = resolvedLevel
                )

                if (resolvedStars > cloudProfile.totalStars || resolvedLevel > cloudProfile.currentLevel) {
                    try {
                        updateUserProfile(validId, UserProfileUpdate(totalStars = resolvedStars, currentLevel = resolvedLevel))
                    } catch (e: Exception) {
                        Log.w("SupabaseDataManager", "Syncing local stars to cloud failed: ${e.message}")
                    }
                }
                Result.success(mergedProfile)
            } else {
                // If profile doesn't exist yet in Supabase, auto-create (upsert) default profile with local stats!
                val defaultProfile = UserProfile(
                    id = validId,
                    displayName = localName,
                    avatarUrl = localAvatar,
                    totalStars = localStars,
                    currentLevel = localLevel,
                    streakDays = localStreak
                )
                upsertUserProfile(defaultProfile).getOrDefault(defaultProfile)
                Result.success(defaultProfile)
            }
        } catch (e: Exception) {
            Log.e("SupabaseDataManager", "fetchUserProfile Exception: ${e.message}", e)
            val fallbackProfile = UserProfile(
                id = validId,
                displayName = localName,
                avatarUrl = localAvatar,
                totalStars = localStars,
                currentLevel = localLevel,
                streakDays = localStreak
            )
            Result.success(fallbackProfile)
        }
    }

    suspend fun updateUserProfile(userId: String, updates: UserProfileUpdate): Result<UserProfile> = withContext(Dispatchers.IO) {
        val tag = "SupabaseAvatarUpload"
        val validId = ensureValidUuid(userId)
        try {
            var response = dataApiService.updateProfile(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                idFilter = "eq.$validId",
                updates = updates
            )
            if (response.code() == 401) {
                Log.w(tag, "updateUserProfile 401 JWT expired. Clearing session & retrying with Anon Key...")
                authManager.signOut()
                response = dataApiService.updateProfile(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader(),
                    idFilter = "eq.$validId",
                    updates = updates
                )
            }
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Log.d(tag, "updateUserProfile SUCCESS: ${response.body()?.firstOrNull()?.avatarUrl}")
                Result.success(response.body()!!.first())
            } else {
                val errStr = response.errorBody()?.string() ?: "Empty body"
                Log.w(tag, "updateUserProfile HTTP ${response.code()} ${response.message()}: $errStr. Attempting upsert...")
                // Row didn't exist in Supabase database; construct full profile and upsert!
                val existing = fetchUserProfile(validId).getOrDefault(UserProfile(id = validId, displayName = "นักอ่านตัวน้อย"))
                val newProfile = UserProfile(
                    id = validId,
                    displayName = updates.displayName ?: existing.displayName,
                    avatarUrl = updates.avatarUrl ?: existing.avatarUrl,
                    totalStars = updates.totalStars ?: existing.totalStars,
                    currentLevel = updates.currentLevel ?: existing.currentLevel
                )
                upsertUserProfile(newProfile)
            }
        } catch (e: Exception) {
            Log.e(tag, "updateUserProfile Exception: ${e.localizedMessage}", e)
            val existing = UserProfile(
                id = validId,
                displayName = updates.displayName ?: "นักอ่านตัวน้อย",
                avatarUrl = updates.avatarUrl ?: "",
                totalStars = updates.totalStars ?: 0,
                currentLevel = updates.currentLevel ?: 1
            )
            upsertUserProfile(existing)
        }
    }

    suspend fun upsertUserProfile(profile: UserProfile): Result<UserProfile> = withContext(Dispatchers.IO) {
        val tag = "SupabaseAvatarUpload"
        val validProfile = profile.copy(id = ensureValidUuid(profile.id))
        try {
            var response = dataApiService.upsertProfile(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                profile = validProfile
            )
            val errBodyInitial = if (!response.isSuccessful) response.errorBody()?.string() else null
            if (response.code() == 401 || errBodyInitial?.contains("JWT expired", ignoreCase = true) == true) {
                Log.w(tag, "upsertUserProfile 401 JWT expired! Clearing session & retrying with Anon Key...")
                authManager.signOut()
                response = dataApiService.upsertProfile(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader(),
                    profile = validProfile
                )
            }
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Log.d(tag, "upsertUserProfile SUCCESS: ${response.body()?.firstOrNull()?.id}")
                Result.success(response.body()!!.first())
            } else {
                val errBody = errBodyInitial ?: response.errorBody()?.string() ?: "Empty response body"
                if (response.code() == 403 || errBody.contains("42501") || errBody.contains("row-level security policy", ignoreCase = true)) {
                    Log.w(tag, "upsertUserProfile RLS WARNING (HTTP ${response.code()}): Supabase Row-Level Security blocked profile insert/update for anon user. Run 'ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;' in Supabase SQL Editor to allow database sync for anonymous users.")
                } else if (response.code() == 409 || errBody.contains("23503") || errBody.contains("violates foreign key constraint", ignoreCase = true)) {
                    Log.w(tag, "upsertUserProfile FOREIGN KEY WARNING (HTTP 409): User ID is not registered in Supabase auth.users table. Run 'ALTER TABLE public.profiles DROP CONSTRAINT IF EXISTS profiles_id_fkey;' in Supabase SQL Editor to allow saving profiles for guest/local users.")
                } else {
                    Log.e(tag, "upsertUserProfile API FAILED (HTTP ${response.code()} ${response.message()}): $errBody")
                }
                Result.failure(Exception("HTTP ${response.code()} ${response.message()}: $errBody"))
            }
        } catch (e: Exception) {
            Log.e(tag, "upsertUserProfile Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun addStarsAndLevel(userId: String, currentStars: Int, currentLevel: Int, starsToAdd: Int): Result<UserProfile> {
        val validId = ensureValidUuid(userId)
        val localStars = getUserTotalStars()
        val localLevel = getUserCurrentLevel()
        val baseStars = maxOf(localStars, currentStars)
        val newStars = baseStars + starsToAdd
        val newLevel = maxOf(localLevel, currentLevel, 1 + (newStars / 50)) // Every 50 stars = +1 level

        saveUserProgress(newStars, newLevel)

        val updates = UserProfileUpdate(totalStars = newStars, currentLevel = newLevel)
        val res = updateUserProfile(validId, updates)
        return if (res.isSuccess) {
            res
        } else {
            val fallback = UserProfile(
                id = validId,
                displayName = getLocalDisplayName() ?: "นักอ่านตัวน้อย",
                avatarUrl = getLocalAvatarUrl() ?: "",
                totalStars = newStars,
                currentLevel = newLevel,
                streakDays = getUserStreakDays()
            )
            Result.success(fallback)
        }
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
                    var storageResp = dataApiService.uploadStorageObject(
                        apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                        auth = getAuthHeader(),
                        contentType = mimeType,
                        upsert = "true",
                        bucket = bucket,
                        path = fileName,
                        body = requestBody
                    )

                    if (storageResp.code() == 401 || storageResp.code() == 403) {
                        Log.w(tag, "Storage upload returned HTTP ${storageResp.code()}. Retrying bucket '$bucket' with Anon Auth header...")
                        storageResp = dataApiService.uploadStorageObject(
                            apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                            auth = getAnonAuthHeader(),
                            contentType = mimeType,
                            upsert = "true",
                            bucket = bucket,
                            path = fileName,
                            body = requestBody
                        )
                    }

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
        val validUserId = ensureValidUuid(userId)
        try {
            val response = dataApiService.getLearningProgress(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                userIdFilter = "eq.$validUserId"
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
        val validUserId = ensureValidUuid(userId)
        try {
            val item = UserLearningProgress(
                userId = validUserId,
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
        val tag = "SupabaseDataManager"
        val validUserId = ensureValidUuid(userId)
        try {
            var response = dataApiService.getQuizResults(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                userIdFilter = "eq.$validUserId"
            )
            if (response.code() == 401) {
                authManager.signOut()
                response = dataApiService.getQuizResults(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader(),
                    userIdFilter = "eq.$validUserId"
                )
            }
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d(tag, "fetchQuizResults SUCCESS: loaded ${list.size} results for user $validUserId")
                Result.success(list)
            } else {
                val err = response.errorBody()?.string() ?: "Empty body"
                Log.e(tag, "fetchQuizResults FAILED HTTP ${response.code()}: $err")
                Result.failure(Exception("HTTP ${response.code()}: $err"))
            }
        } catch (e: Exception) {
            Log.e(tag, "fetchQuizResults Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAllQuizResults(): Result<List<UserQuizResult>> = withContext(Dispatchers.IO) {
        val tag = "SupabaseDataManager"
        try {
            var response = dataApiService.getAllQuizResults(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                order = "created_at.desc"
            )
            if (response.code() == 401) {
                authManager.signOut()
                response = dataApiService.getAllQuizResults(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader(),
                    order = "created_at.desc"
                )
            }
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Result.success(list)
            } else {
                val err = response.errorBody()?.string()
                Log.e(tag, "fetchAllQuizResults HTTP ${response.code()}: $err")
                Result.failure(Exception("Failed to fetch all quiz results"))
            }
        } catch (e: Exception) {
            Log.e(tag, "fetchAllQuizResults Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAllUserProfiles(): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        val tag = "SupabaseDataManager"
        try {
            var response = dataApiService.getAllProfiles(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader()
            )
            if (response.code() == 401) {
                authManager.signOut()
                response = dataApiService.getAllProfiles(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader()
                )
            }
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch all user profiles"))
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
        val tag = "SupabaseDataManager"
        val validUserId = ensureValidUuid(userId)
        try {
            // First ensure profile exists in public.profiles to satisfy Foreign Key constraint
            val profCheck = fetchUserProfile(validUserId)
            if (profCheck.isFailure) {
                Log.d(tag, "Profile for $validUserId not found, creating local profile...")
                upsertUserProfile(
                    UserProfile(
                        id = validUserId,
                        displayName = "นักอ่านตัวน้อย",
                        totalStars = 0,
                        currentLevel = 1
                    )
                )
            }

            val result = UserQuizResult(
                userId = validUserId,
                quizTopicId = topicId,
                quizTopicName = topicName,
                score = score,
                totalQuestions = totalQuestions,
                timeSpentSeconds = timeSpentSeconds
            )
            Log.d(tag, "Saving quiz result for user $validUserId: topic=$topicName, score=$score")
            var response = dataApiService.insertQuizResult(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                result = result
            )
            val errInitial = if (!response.isSuccessful) response.errorBody()?.string() else null
            if (response.code() == 401 || errInitial?.contains("JWT expired", ignoreCase = true) == true) {
                Log.w(tag, "saveQuizResult: 401 JWT expired, clearing session and retrying with Anon Key...")
                authManager.signOut()
                response = dataApiService.insertQuizResult(
                    apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                    auth = getAnonAuthHeader(),
                    result = result
                )
            }
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val saved = response.body()!!.first()
                Log.d(tag, "saveQuizResult SUCCESS: id=${saved.id}")
                Result.success(saved)
            } else {
                val errBody = errInitial ?: response.errorBody()?.string() ?: "Empty body"
                if (response.code() == 403 || errBody.contains("42501") || errBody.contains("row-level security", ignoreCase = true)) {
                    Log.w(tag, "saveQuizResult RLS BLOCKED (HTTP ${response.code()}): Run 'CREATE POLICY \"Allow insert to user_quiz_results\" ON public.user_quiz_results FOR INSERT WITH CHECK (true);' in Supabase SQL Editor.")
                } else if (response.code() == 409 || errBody.contains("23503") || errBody.contains("violates foreign key", ignoreCase = true)) {
                    Log.w(tag, "saveQuizResult FOREIGN KEY BLOCKED (HTTP ${response.code()}): User ID not in public.profiles.")
                } else {
                    Log.e(tag, "saveQuizResult FAILED: HTTP ${response.code()}: $errBody")
                }
                Result.failure(Exception("HTTP ${response.code()}: $errBody"))
            }
        } catch (e: Exception) {
            Log.e(tag, "saveQuizResult Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    // ==================== ACHIEVEMENTS ====================

    suspend fun fetchAchievements(userId: String): Result<List<UserAchievement>> = withContext(Dispatchers.IO) {
        val validUserId = ensureValidUuid(userId)
        try {
            val response = dataApiService.getAchievements(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                userIdFilter = "eq.$validUserId"
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
        val validUserId = ensureValidUuid(userId)
        try {
            val achievement = UserAchievement(
                userId = validUserId,
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
        val tag = "SupabaseDevice"
        try {
            val validUserId = ensureValidUuid(userId)
            val device = UserDevice(
                userId = validUserId,
                deviceId = deviceId,
                deviceName = deviceName,
                deviceModel = deviceModel,
                osVersion = osVersion,
                appVersion = appVersion,
                fcmToken = fcmToken,
                isActive = true
            )
            Log.d(tag, "Registering device for user $validUserId: $deviceId ($deviceName)")
            val response = dataApiService.upsertDevice(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                device = device
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val savedDevice = response.body()!!.first()
                Log.d(tag, "Device registered successfully! Device id: ${savedDevice.id}")
                Result.success(savedDevice)
            } else {
                val errBody = response.errorBody()?.string() ?: "Empty response"
                Log.e(tag, "Failed to register user device: HTTP ${response.code()} $errBody")
                Result.failure(Exception("Failed to register user device: HTTP ${response.code()} $errBody"))
            }
        } catch (e: Exception) {
            Log.e(tag, "registerUserDevice exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch remote app settings from Supabase app_settings table
     */
    suspend fun fetchAppSettings(): Result<AppSettings> = withContext(Dispatchers.IO) {
        val tag = "SupabaseAppSettings"
        try {
            val response = dataApiService.getAppSettings(
                apiKey = SupabaseAuthManager.SUPABASE_ANON_KEY,
                auth = getAuthHeader(),
                idFilter = "eq.1"
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val settings = response.body()!!.first()
                Log.d(tag, "App settings fetched successfully from Supabase: $settings")
                Result.success(settings)
            } else {
                val defaultSettings = AppSettings()
                Log.w(tag, "Failed to fetch app settings (HTTP ${response.code()}), using default config.")
                Result.success(defaultSettings)
            }
        } catch (e: Exception) {
            Log.e(tag, "fetchAppSettings exception: ${e.localizedMessage}, using default config.", e)
            Result.success(AppSettings())
        }
    }
}

