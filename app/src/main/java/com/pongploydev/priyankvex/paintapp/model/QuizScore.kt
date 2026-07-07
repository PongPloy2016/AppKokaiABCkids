package com.pongploydev.priyankvex.paintapp.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class QuizScore(
    val userName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val topicTitle: String = "",
    val durationSeconds: Long = 0,
    @ServerTimestamp
    val timestamp: Date? = null
)
