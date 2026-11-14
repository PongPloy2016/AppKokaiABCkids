package com.pongploydev.kokaiabckids.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class QuizScore(
    val userName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val topicTitle: String = "",
    val durationSeconds: Long = 0,
    val avatarUrl: String? = null,
    val email: String? = null,
    val user_id_supabase: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)

