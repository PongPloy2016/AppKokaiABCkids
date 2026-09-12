package com.pongploydev.profilefeature.auth

import com.google.gson.annotations.SerializedName

data class SupabaseSignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("data") val data: Map<String, String>? = null
)

data class SupabaseSignInRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SupabaseUserMetadata(
    @SerializedName("display_name") val displayName: String? = null
)

data class SupabaseUser(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("user_metadata") val userMetadata: SupabaseUserMetadata? = null,
    @SerializedName("is_anonymous") val isAnonymous: Boolean? = false
)

data class SupabaseAuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: SupabaseUser? = null,
    // Root level fields if User object is returned directly at root level
    @SerializedName("id") val rootId: String? = null,
    @SerializedName("email") val rootEmail: String? = null,
    @SerializedName("created_at") val rootCreatedAt: String? = null,
    @SerializedName("user_metadata") val rootUserMetadata: SupabaseUserMetadata? = null,
    @SerializedName("is_anonymous") val rootIsAnonymous: Boolean? = false,
    // Error fields
    @SerializedName("error_description") val errorDescription: String? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("error_code") val errorCode: String? = null
) {
    fun extractUser(): SupabaseUser? {
        if (user != null) return user
        if (rootId != null) {
            return SupabaseUser(
                id = rootId,
                email = rootEmail,
                createdAt = rootCreatedAt,
                userMetadata = rootUserMetadata,
                isAnonymous = rootIsAnonymous
            )
        }
        return null
    }
}
