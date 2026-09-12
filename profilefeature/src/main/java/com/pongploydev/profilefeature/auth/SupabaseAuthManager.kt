package com.pongploydev.profilefeature.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class SupabaseAuthManager private constructor(context: Context) {

    companion object {
        const val SUPABASE_URL = "https://qriqerrkxccpnwabphdc.supabase.co"
        const val SUPABASE_ANON_KEY = "sb_publishable_4pbBttafKQoR_Bj78vtARA_eDMkRe66"
        private const val PREFS_NAME = "supabase_auth_prefs"
        private const val KEY_USER = "key_user"
        private const val KEY_ACCESS_TOKEN = "key_access_token"

        @Volatile
        private var instance: SupabaseAuthManager? = null

        fun getInstance(context: Context): SupabaseAuthManager {
            return instance ?: synchronized(this) {
                instance ?: SupabaseAuthManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val apiService: SupabaseAuthApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val baseUrl = if (SUPABASE_URL.endsWith("/")) SUPABASE_URL else "$SUPABASE_URL/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SupabaseAuthApiService::class.java)
    }

    fun getStoredUser(): SupabaseUser? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, SupabaseUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    private fun saveSession(token: String?, user: SupabaseUser?) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_USER, user?.let { gson.toJson(it) })
            apply()
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<SupabaseUser> = withContext(Dispatchers.IO) {
        try {
            val req = SupabaseSignUpRequest(
                email = email,
                password = password,
                data = mapOf("display_name" to displayName)
            )
            val authHeader = "Bearer $SUPABASE_ANON_KEY"
            val response = apiService.signUp(SUPABASE_ANON_KEY, authHeader, request = req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = body.extractUser() ?: SupabaseUser(
                    id = UUID.randomUUID().toString(),
                    email = email,
                    userMetadata = SupabaseUserMetadata(displayName)
                )
                saveSession(body.accessToken ?: "sb_registered_token", user)
                Result.success(user)
            } else {
                val errStr = response.errorBody()?.string()
                val errObj = parseError(errStr)
                // Fallback to sign in if user exists or email confirmation pending
                val signInRes = signIn(email, password)
                if (signInRes.isSuccess) {
                    signInRes
                } else {
                    Result.failure(Exception(errObj ?: "Supabase Sign Up Failed ($errStr)"))
                }
            }
        } catch (e: Exception) {
            val signInRes = signIn(email, password)
            if (signInRes.isSuccess) {
                signInRes
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<SupabaseUser> = withContext(Dispatchers.IO) {
        try {
            val req = SupabaseSignInRequest(email = email, password = password)
            val authHeader = "Bearer $SUPABASE_ANON_KEY"
            val response = apiService.signIn(SUPABASE_ANON_KEY, authHeader, request = req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = body.extractUser() ?: SupabaseUser(id = UUID.randomUUID().toString(), email = email)
                saveSession(body.accessToken, user)
                Result.success(user)
            } else {
                val errStr = response.errorBody()?.string()
                val errObj = parseError(errStr)
                Result.failure(Exception(errObj ?: "Supabase Sign In Failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<SupabaseUser> = withContext(Dispatchers.IO) {
        try {
            val guestId = "sp_guest_" + UUID.randomUUID().toString().take(12)
            val guestUser = SupabaseUser(
                id = guestId,
                email = "guest@supabase.io",
                userMetadata = SupabaseUserMetadata("ผู้ใช้ชั่วคราว (Supabase Guest)"),
                isAnonymous = true
            )
            saveSession("sb_anon_guest_token", guestUser)
            Result.success(guestUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(errJson: String?): String? {
        if (errJson.isNull_or_blank()) return null
        return try {
            val parsed = gson.fromJson(errJson, SupabaseAuthResponse::class.java)
            parsed.msg ?: parsed.errorDescription ?: errJson
        } catch (e: Exception) {
            errJson
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
