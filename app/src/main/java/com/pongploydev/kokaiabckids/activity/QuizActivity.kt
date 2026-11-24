package com.pongploydev.kokaiabckids.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import com.pongploydev.kokaiabckids.compose.QuizComposeScreen

class QuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        window.statusBarColor = "#DDF3FF".toColorInt()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            MaterialTheme {
                QuizComposeScreen(
                    isStandalone = true,
                    onBack = { finish() }
                )
            }
        }
    }
}
