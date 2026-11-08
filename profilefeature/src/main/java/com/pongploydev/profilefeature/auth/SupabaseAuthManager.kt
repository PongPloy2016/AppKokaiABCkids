package com.pongploydev.profilefeature.auth

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SupabaseAuthManager private constructor(context: Context) {

    companion object {
        private const val TAG = "SupabaseAuth"
        const val SUPABASE_URL = "https://qriqerrkxccpnwabphdc.supabase.co"
        const val SUPABASE_ANON_KEY = "sb_publishable_4pbBttafKQoR_Bj78vtARA_eDMkRe66"
        private const val PREFS_NAME = "supabase_auth_prefs"
        private const val KEY_USER = "key_user"
        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_GUEST_DISPLAY_NAME = "key_guest_display_name"

        @Volatile
        private var instance: SupabaseAuthManager? = null

        fun getInstance(context: Context): SupabaseAuthManager {
            return instance ?: synchronized(this) {
                instance ?: SupabaseAuthManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    fun getOrCreateGuestDisplayName(): String {
        val existing = prefs.getString(KEY_GUEST_DISPLAY_NAME, null)
        if (!existing.isNullOrBlank() && existing.startsWith("guest", ignoreCase = true)) {
            return existing
        }
        val datePart = SimpleDateFormat("ddyyyyMM", Locale.US).format(Date())
        val rawDeviceId = try {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            null
        }
        val cleanDevice = rawDeviceId
            ?.filter { it.isLetterOrDigit() }
            ?.takeLast(6)
            ?.ifBlank { null }
            ?: (100000..999999).random().toString()
        val generated = "guest${datePart}${cleanDevice}"
        prefs.edit().putString(KEY_GUEST_DISPLAY_NAME, generated).apply()
        return generated
    }

    fun getGuestUserId(): String {
        val rawDeviceId = try {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            null
        }?.takeIf { it.isNotBlank() } ?: getOrCreateGuestDisplayName()
        return UUID.nameUUIDFromBytes("sp_guest_$rawDeviceId".toByteArray()).toString()
    }

    fun getStoredUser(): SupabaseUser? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            val user = gson.fromJson(json, SupabaseUser::class.java)
            if (user != null && user.isAnonymous == true) {
                val curName = user.userMetadata?.displayName
                val validGuestUuid = getGuestUserId()
                val needsNameUpgrade = curName.isNullOrBlank() || curName.startsWith("ผู้ใช้ชั่วคราว")
                val needsIdOrEmailUpgrade = user.id != validGuestUuid || user.email == "guest@supabase.io"
                if (needsNameUpgrade || needsIdOrEmailUpgrade) {
                    val guestName = if (needsNameUpgrade) getOrCreateGuestDisplayName() else curName!!
                    val updated = user.copy(
                        id = validGuestUuid,
                        email = null,
                        userMetadata = SupabaseUserMetadata(displayName = guestName, avatarUrl = user.userMetadata?.avatarUrl)
                    )
                    saveSession(getAccessToken(), updated)
                    return updated
                }
            }
            user
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
        Log.d(TAG, "Attempting Supabase signUp for email: $email, displayName: $displayName")
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
                Log.d(TAG, "Supabase signUp SUCCESS! User id: ${user.id}, token present: ${!body.accessToken.isNullOrBlank()}")
                saveSession(body.accessToken ?: "sb_registered_token", user)
                Result.success(user)
            } else {
                val errCode = response.code()
                val errStr = response.errorBody()?.string()
                Log.w(TAG, "Supabase signUp failed with HTTP $errCode: $errStr")
                val errObj = parseError(errStr)
                // Fallback to sign in if user already exists
                val signInRes = signIn(email, password)
                if (signInRes.isSuccess) {
                    Log.d(TAG, "Fallback signIn after signUp conflict succeeded!")
                    signInRes
                } else {
                    Log.e(TAG, "Both signUp and fallback signIn failed: $errObj")
                    Result.failure(Exception(errObj ?: "ลงทะเบียน Supabase ไม่สำเร็จ ($errStr)"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Supabase signUp: ${e.localizedMessage}", e)
            val signInRes = signIn(email, password)
            if (signInRes.isSuccess) {
                Log.d(TAG, "Fallback signIn succeeded following signUp exception!")
                signInRes
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<SupabaseUser> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Attempting Supabase signIn for email: $email")
        try {
            val req = SupabaseSignInRequest(email = email, password = password)
            val authHeader = "Bearer $SUPABASE_ANON_KEY"
            val response = apiService.signIn(SUPABASE_ANON_KEY, authHeader, request = req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = body.extractUser() ?: SupabaseUser(id = UUID.randomUUID().toString(), email = email)
                Log.d(TAG, "Supabase signIn SUCCESS! User id: ${user.id}")
                saveSession(body.accessToken, user)
                Result.success(user)
            } else {
                val errCode = response.code()
                val errStr = response.errorBody()?.string()
                Log.w(TAG, "Supabase signIn failed with HTTP $errCode: $errStr")
                val errObj = parseError(errStr)
                Result.failure(Exception(errObj ?: "เข้าสู่ระบบ Supabase ไม่สำเร็จ"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Supabase signIn: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(
        email: String,
        displayName: String,
        googleId: String,
        avatarUrl: String? = null,
        idToken: String? = null
    ): Result<SupabaseUser> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Attempting Supabase Google auth for email: $email, googleId: $googleId, hasIdToken: ${!idToken.isNullOrBlank()}")

        val authHeader = "Bearer $SUPABASE_ANON_KEY"

        // 1. If idToken is available, try Supabase native OAuth id_token exchange
        if (!idToken.isNullOrBlank()) {
            try {
                val tokenReq = SupabaseIdTokenRequest(provider = "google", idToken = idToken)
                val tokenResp = apiService.signInWithIdToken(SUPABASE_ANON_KEY, authHeader, request = tokenReq)
                if (tokenResp.isSuccessful && tokenResp.body() != null) {
                    val body = tokenResp.body()!!
                    val user = body.extractUser() ?: SupabaseUser(
                        id = UUID.randomUUID().toString(),
                        email = email,
                        userMetadata = SupabaseUserMetadata(displayName = displayName, avatarUrl = avatarUrl)
                    )
                    Log.d(TAG, "Supabase signInWithIdToken SUCCESS! User id: ${user.id}")
                    saveSession(body.accessToken, user)
                    return@withContext Result.success(user)
                } else {
                    Log.w(TAG, "Supabase signInWithIdToken HTTP ${tokenResp.code()}: ${tokenResp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Supabase signInWithIdToken exception: ${e.message}")
            }
        }

        // 2. Fallback: Authenticate via deterministic password
        val deterministicPassword = "Ggl_${googleId.take(16)}_Sec#99"
        try {
            val signInRes = signIn(email, deterministicPassword)
            if (signInRes.isSuccess) {
                Log.d(TAG, "Supabase signInWithGoogle (existing account) SUCCESS!")
                return@withContext signInRes
            }

            val signUpRes = signUp(email, deterministicPassword, displayName)
            if (signUpRes.isSuccess) {
                Log.d(TAG, "Supabase signUpWithGoogle (new account) SUCCESS!")
                return@withContext signUpRes
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase password fallback exception: ${e.message}")
        }

        // 3. Fallback: Generate dedicated local session for Google user
        val fallbackUserId = "sp_ggl_" + UUID.nameUUIDFromBytes(googleId.toByteArray()).toString().take(20)
        val fallbackUser = SupabaseUser(
            id = fallbackUserId,
            email = email,
            userMetadata = SupabaseUserMetadata(displayName = displayName, avatarUrl = avatarUrl),
            isAnonymous = false
        )
        saveSession("sb_google_token_${googleId.take(8)}", fallbackUser)
        Log.d(TAG, "Supabase signInWithGoogle: saved fallback session for user $fallbackUserId")
        Result.success(fallbackUser)
    }

    suspend fun signInAnonymously(): Result<SupabaseUser> = withContext(Dispatchers.IO) {
        try {
            val guestName = getOrCreateGuestDisplayName()
            val guestId = getGuestUserId()
            val guestUser = SupabaseUser(
                id = guestId,
                email = null,
                userMetadata = SupabaseUserMetadata(guestName),
                isAnonymous = true
            )
            Log.d(TAG, "Supabase signInAnonymously: generated guest uuid $guestId with name $guestName")
            saveSession("sb_anon_guest_token", guestUser)
            Result.success(guestUser)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in signInAnonymously: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    private fun parseError(errJson: String?): String? {
        if (errJson.isNull_or_blank()) return null
        return try {
            val parsed = gson.fromJson(errJson, SupabaseAuthResponse::class.java)
            when (parsed.errorCode) {
                "user_already_exists" -> "อีเมลนี้มีผู้ลงทะเบียนใน Supabase แล้ว หากเคยลงทะเบียนแล้วกรุณาเลือก 'เข้าสู่ระบบ'"
                "invalid_credentials" -> "อีเมลหรือรหัสผ่านไม่ถูกต้อง"
                else -> {
                    val rawMsg = parsed.msg ?: parsed.errorDescription
                    if (rawMsg?.contains("User already registered", ignoreCase = true) == true) {
                        "อีเมลนี้มีผู้ลงทะเบียนใน Supabase แล้ว กรุณาเลือก 'เข้าสู่ระบบ'"
                    } else if (rawMsg?.contains("Password should be at least", ignoreCase = true) == true) {
                        "รหัสผ่านต้องมีความยาวอย่างน้อย 6 ตัวอักษร"
                    } else {
                        rawMsg ?: errJson
                    }
                }
            }
        } catch (e: Exception) {
            errJson
        }
    }

    fun signOut() {
        prefs.edit()
            .remove(KEY_USER)
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
