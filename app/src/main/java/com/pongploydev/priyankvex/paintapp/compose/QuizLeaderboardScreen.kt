package com.pongploydev.priyankvex.paintapp.compose

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pongploydev.priyankvex.paintapp.model.QuizScore
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizLeaderboardScreen(onBack: () -> Unit) {
    var scores by remember { mutableStateOf<List<QuizScore>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("quiz_scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                try {
                    scores = result.toObjects(QuizScore::class.java)
                    isLoading = false
                    Log.d("Leaderboard", "Successfully fetched ${scores.size} scores")
                } catch (e: Exception) {
                    isLoading = false
                    errorMessage = "ข้อมูลไม่ถูกต้อง: ${e.message}"
                    Log.e("Leaderboard", "Error parsing data", e)
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = "ดึงข้อมูลล้มเหลว: ${exception.message}"
                Log.e("Leaderboard", "Error fetching scores", exception)
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

@Composable
fun LeaderboardItem(score: QuizScore) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th"))
    val dateString = score.timestamp?.let { dateFormat.format(it) } ?: "เพิ่งเมื่อสักครู่"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Icon / Avatar placeholder
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = Color(0xFFE1F5FE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 24.sp)
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
                    text = "${score.topicTitle} • $dateString",
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
