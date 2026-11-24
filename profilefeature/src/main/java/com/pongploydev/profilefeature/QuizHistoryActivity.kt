package com.pongploydev.profilefeature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.compose.KidTheme
import com.pongploydev.profilefeature.compose.QuizHistoryListViewScreen
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.profilefeature.data.UserQuizResult
import kotlinx.coroutines.launch

class QuizHistoryActivity : ComponentActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, QuizHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val firebaseAuth = remember { FirebaseAuth.getInstance() }
            val supabaseAuthManager = remember { SupabaseAuthManager.getInstance(this@QuizHistoryActivity) }
            val dataManager = remember { SupabaseDataManager.getInstance(this@QuizHistoryActivity) }

            var quizResults by remember { mutableStateOf<List<UserQuizResult>>(emptyList()) }
            val userId = supabaseAuthManager.getStoredUser()?.id ?: firebaseAuth.currentUser?.uid ?: supabaseAuthManager.getGuestUserId()

            LaunchedEffect(userId) {
                if (userId.isNotBlank()) {
                    scope.launch {
                        dataManager.fetchQuizResults(userId).onSuccess { list ->
                            quizResults = list
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = KidTheme.Primary
            ) {
                QuizHistoryListViewScreen(
                    quizResults = quizResults,
                    onBack = { finish() },
                    onTakeMoreQuiz = { finish() }
                )
            }
        }
    }
}
