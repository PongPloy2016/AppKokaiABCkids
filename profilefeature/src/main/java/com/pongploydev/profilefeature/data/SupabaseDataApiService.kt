package com.pongploydev.profilefeature.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseDataApiService {

    // PROFILES
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") idFilter: String, // e.g. "eq.uuid"
        @Query("select") select: String = "*"
    ): Response<List<UserProfile>>

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Query("id") idFilter: String, // e.g. "eq.uuid"
        @Body updates: UserProfileUpdate
    ): Response<List<UserProfile>>

    @POST("rest/v1/profiles?on_conflict=id")
    suspend fun upsertProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body profile: UserProfile
    ): Response<List<UserProfile>>

    // STORAGE UPLOAD
    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadStorageObject(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Content-Type") contentType: String = "image/jpeg",
        @Header("x-upsert") upsert: String = "true",
        @retrofit2.http.Path("bucket") bucket: String,
        @retrofit2.http.Path(value = "path", encoded = true) path: String,
        @Body body: okhttp3.RequestBody
    ): Response<okhttp3.ResponseBody>

    // LEARNING PROGRESS
    @GET("rest/v1/user_learning_progress")
    suspend fun getLearningProgress(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<UserLearningProgress>>

    @POST("rest/v1/user_learning_progress")
    suspend fun upsertLearningProgress(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body progress: UserLearningProgress
    ): Response<List<UserLearningProgress>>

    // QUIZ RESULTS
    @GET("rest/v1/user_quiz_results")
    suspend fun getQuizResults(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdFilter: String,
        @Query("order") order: String = "created_at.desc",
        @Query("select") select: String = "*"
    ): Response<List<UserQuizResult>>

    @GET("rest/v1/user_quiz_results")
    suspend fun getAllQuizResults(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 100,
        @Query("select") select: String = "*"
    ): Response<List<UserQuizResult>>

    @GET("rest/v1/profiles")
    suspend fun getAllProfiles(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("select") select: String = "*"
    ): Response<List<UserProfile>>

    @POST("rest/v1/user_quiz_results")
    suspend fun insertQuizResult(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body result: UserQuizResult
    ): Response<List<UserQuizResult>>

    // ACHIEVEMENTS
    @GET("rest/v1/user_achievements")
    suspend fun getAchievements(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<UserAchievement>>

    @POST("rest/v1/user_achievements")
    suspend fun unlockAchievement(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body achievement: UserAchievement
    ): Response<List<UserAchievement>>

    // USER DEVICES
    @GET("rest/v1/user_devices")
    suspend fun getDevices(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*"
    ): Response<List<UserDevice>>

    @POST("rest/v1/user_devices?on_conflict=user_id,device_id")
    suspend fun upsertDevice(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
        @Body device: UserDevice
    ): Response<List<UserDevice>>
}
