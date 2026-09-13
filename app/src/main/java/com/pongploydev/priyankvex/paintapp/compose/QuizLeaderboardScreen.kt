package com.pongploydev.priyankvex.paintapp.compose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.priyankvex.paintapp.model.QuizScore
import java.text.SimpleDateFormat
import java.util.*

data class DisplayQuizScore(
    val userName: String,
    val avatarUrl: String?,
    val topicTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val durationSeconds: Int,
    val dateString: String,
    val rawTimestamp: String
)

private fun formatThaiDateTime(isoTimestamp: String?): String {
    val thaiZone = TimeZone.getTimeZone("Asia/Bangkok")
    val utcZone = TimeZone.getTimeZone("UTC")

    if (isoTimestamp.isNullOrBlank()) {
        val sdfNow = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH")).apply {
            timeZone = thaiZone
        }
        return sdfNow.format(Date())
    }

    return try {
        val cleanIso = isoTimestamp.replace(" ", "T")
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = utcZone
        }
        val date = sdfInput.parse(cleanIso.substringBefore("+").replace("Z", "")) ?: Date()
        val sdfOutput = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH")).apply {
            timeZone = thaiZone
        }
        sdfOutput.format(date)
    } catch (e: Exception) {
        try {
            val sdfInput2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                timeZone = utcZone
            }
            val date2 = sdfInput2.parse(isoTimestamp) ?: Date()
            val sdfOutput2 = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH")).apply {
                timeZone = thaiZone
            }
            sdfOutput2.format(date2)
        } catch (e2: Exception) {
            isoTimestamp
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizLeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var scores by remember { mutableStateOf<List<DisplayQuizScore>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val dataManager = SupabaseDataManager.getInstance(context)
        try {
            // 1. Fetch Supabase Quiz Results
            val supabaseResults = dataManager.fetchAllQuizResults()
            val profilesMap = dataManager.fetchAllUserProfiles().getOrDefault(emptyList()).associateBy { it.id }

            val listFromSupabase = supabaseResults.getOrNull() ?: emptyList()

            if (listFromSupabase.isNotEmpty()) {
                val mapped = listFromSupabase.map { res ->
                    val userProf = profilesMap[res.userId]
                    DisplayQuizScore(
                        userName = userProf?.displayName?.ifBlank { null } ?: "ผู้เล่นนิรนาม",
                        avatarUrl = userProf?.avatarUrl?.ifBlank { null },
                        topicTitle = res.quizTopicName.ifBlank { "แบบทดสอบวัดความรู้" },
                        score = res.score,
                        totalQuestions = if (res.totalQuestions > 0) res.totalQuestions else 10,
                        durationSeconds = res.timeSpentSeconds,
                        dateString = formatThaiDateTime(res.createdAt),
                        rawTimestamp = res.createdAt ?: ""
                    )
                }
                // Sort by date descending - latest date first!
                scores = mapped.sortedByDescending { it.rawTimestamp }
                isLoading = false
                Log.d("Leaderboard", "Loaded ${scores.size} scores from Supabase sorted by date desc")
            } else {
                // Fallback to Firebase Firestore if Supabase database is empty
                fetchFirestoreScores { firestoreScores ->
                    scores = firestoreScores
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            Log.e("Leaderboard", "Error fetching scores from Supabase, fallback to Firestore", e)
            fetchFirestoreScores { firestoreScores ->
                scores = firestoreScores
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🏆 ตารางคะแนน", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF673AB7)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            KidBackground(modifier = Modifier.fillMaxSize())

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.Red
                )
            } else if (scores.isEmpty()) {
                Text(
                    "ยังไม่มีคะแนนในขณะนี้\nมาเป็นคนแรกกันเถอะ! 🌟",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scores) { score ->
                        LeaderboardItem(score)
                    }
                }
            }
        }
    }
}

private fun fetchFirestoreScores(onResult: (List<DisplayQuizScore>) -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH"))
    FirebaseFirestore.getInstance().collection("quiz_scores")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(50)
        .get()
        .addOnSuccessListener { result ->
            try {
                val list = result.toObjects(QuizScore::class.java).map { s ->
                    val dateStr = s.timestamp?.let { dateFormat.format(it) } ?: "เพิ่งเมื่อสักครู่"
                    DisplayQuizScore(
                        userName = s.userName.ifBlank { "ผู้เล่นนิรนาม" },
                        avatarUrl = null,
                        topicTitle = s.topicTitle.ifBlank { "แบบทดสอบทั่วไป" },
                        score = s.score,
                        totalQuestions = if (s.totalQuestions > 0) s.totalQuestions else 10,
                        durationSeconds = s.durationSeconds.toInt(),
                        dateString = dateStr,
                        rawTimestamp = s.timestamp?.time?.toString() ?: ""
                    )
                }
                onResult(list)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}

@Composable
fun LeaderboardItem(score: DisplayQuizScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Icon / Avatar
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = Color(0xFFE1F5FE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!score.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = score.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("👤", fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = score.userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "${score.topicTitle} • ${score.dateString}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "ใช้เวลา: ${score.durationSeconds} วินาที",
                    fontSize = 11.sp,
                    color = Color(0xFF0288D1)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${score.score}/${score.totalQuestions}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5252)
                )
                Text(
                    text = "คะแนน",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

