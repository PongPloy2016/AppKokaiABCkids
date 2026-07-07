package com.pongploydev.priyankvex.paintapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pongploydev.priyankvex.paintapp.compose.QuizLeaderboardScreen

class QuizLeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizLeaderboardScreen(onBack = { finish() })
        }
    }
}
