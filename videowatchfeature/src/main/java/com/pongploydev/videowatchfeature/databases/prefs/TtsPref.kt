package com.pongploydev.videowatchfeature.databases.prefs

import android.content.Context
import android.content.SharedPreferences

class TtsPref(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE)

    var speechRate: Float
        get() = prefs.getFloat("speech_rate", 1.0f)
        set(value) = prefs.edit().putFloat("speech_rate", value).apply()

    var pitch: Float
        get() = prefs.getFloat("pitch", 1.0f)
        set(value) = prefs.edit().putFloat("pitch", value).apply()

    var autoSpeak: Boolean
        get() = prefs.getBoolean("auto_speak", true)
        set(value) = prefs.edit().putBoolean("auto_speak", value).apply()

    var languageCode: String
        get() = prefs.getString("language_code", "en") ?: "en"
        set(value) = prefs.edit().putString("language_code", value).apply()

    var countryCode: String
        get() = prefs.getString("country_code", "US") ?: "US"
        set(value) = prefs.edit().putString("country_code", value).apply()

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}