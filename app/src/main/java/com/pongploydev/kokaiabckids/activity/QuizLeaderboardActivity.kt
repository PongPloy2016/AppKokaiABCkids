package com.pongploydev.kokaiabckids.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pongploydev.kokaiabckids.compose.QuizLeaderboardScreen

class QuizLeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizLeaderboardScreen(onBack = { finish() })
        }
    }
}
