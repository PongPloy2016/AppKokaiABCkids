package com.pongploydev.kokaiabckids.compose

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.activity.QuizLeaderboardActivity
import com.pongploydev.kokaiabckids.activity.QuizSessionActivity
import com.pongploydev.kokaiabckids.adapter.QuizTopic
import com.pongploydev.kokaiabckids.utils.QuizProgressManager

// ─────────────────────────────────────────────
//  Color Palette
// ─────────────────────────────────────────────
private val KidDeepBlue   = Color(0xFF0288D1)
private val KidPurple     = Color(0xFFAB47BC)

private val topicGradients = listOf(
    listOf(Color(0xFFE1F5FE), Color(0xFFF3E5F5)),
    listOf(Color(0xFFFFF9C4), Color(0xFFFFE0B2)),
    listOf(Color(0xFFE8F5E9), Color(0xFFE0F7FA)),
    listOf(Color(0xFFFCE4EC), Color(0xFFEDE7F6)),
    listOf(Color(0xFFF3E5F5), Color(0xFFE3F2FD)),
)

// ─────────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizComposeScreen() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val topics = remember { getQuizTopics() }

    // Read current profile & progress
    val supabaseUser = remember { SupabaseAuthManager.getInstance(context).getStoredUser() }
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val userId = supabaseUser?.id ?: firebaseUser?.uid

    var refreshTrigger by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(userId, refreshTrigger) {
        if (!userId.isNullOrEmpty()) {
            val dataManager = SupabaseDataManager.getInstance(context)
            dataManager.fetchUserProfile(userId).onSuccess { profile ->
                if (profile.currentLevel > 1) {
                    QuizProgressManager.setMaxUnlockedLesson(context, profile.currentLevel + 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🎯 คลังแบบทดสอบ",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, QuizLeaderboardActivity::class.java))
                    }) {
                        Text("🏆", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KidDeepBlue
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            QuizBackground(modifier = Modifier.fillMaxSize())

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuizHeader()
                }

                itemsIndexed(topics) { index, topic ->
                    val isUnlocked = remember(refreshTrigger, topic.id) {
                        QuizProgressManager.isTopicUnlocked(context, topic.id)
                    }
                    val highScore = remember(refreshTrigger, topic.id) {
                        QuizProgressManager.getTopicHighScore(context, topic.id)
                    }

                    QuizTopicItem(
                        topic = topic,
                        index = index,
                        isUnlocked = isUnlocked,
                        highScore = highScore,
                        gradientColors = topicGradients[index % topicGradients.size]
                    ) {
                        val intent = Intent(context, QuizSessionActivity::class.java).apply {
                            putExtra("EXTRA_TOPIC_ID", topic.id)
                            putExtra("EXTRA_TOPIC_TITLE", topic.title)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(KidDeepBlue, KidPurple)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingTargetDot()
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "เลือกหัวข้อที่ชอบ",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ฝึกฝนและท้าทายตัวเองกันเถอะ! 🌟",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PulsingTargetDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "targetDot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dotScale"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(60.dp).graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.25f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🎯", fontSize = 34.sp)
            }
        }
    }
}

@Composable
fun QuizTopicItem(
    topic: QuizTopic,
    index: Int,
    isUnlocked: Boolean,
    highScore: Int,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(Unit) { visible = true }

    val cardAlpha = if (isUnlocked) 1.0f else 0.55f

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                scaleIn(initialScale = 0.95f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
                .clickable {
                    if (isUnlocked) {
                        com.pongploydev.kokaiabckids.log.AnalyticsLogger.logClickEvent(context, "หัวข้อแบบทดสอบ_${topic.title}", "หน้าแบบทดสอบ (Quiz)")
                        onClick()
                    } else {
                        Toast.makeText(context, "🔒 ต้องสอบผ่านบทเรียนก่อนหน้าก่อนนะ!", Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 6.dp else 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(gradientColors))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = if (isUnlocked) 0.6f else 0.4f),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = topic.iconRes),
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color(0xFF1A1A2E) else Color(0xFF555555)
                        )
                        Text(
                            text = topic.description,
                            fontSize = 13.sp,
                            color = Color(0xFF555555),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isUnlocked) colorResource(id = topic.colorRes).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "บทเรียนที่ ${index + 1}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isUnlocked) colorResource(id = topic.colorRes) else Color.DarkGray
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isUnlocked) {
                                if (highScore >= 0) {
                                    Text("✅ ผ่านแล้ว (${highScore}/20)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                } else {
                                    Text("👉 เริ่มเลย!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KidDeepBlue)
                                }
                            } else {
                                Text("🔒 ล็อคอยู่", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isUnlocked) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) KidDeepBlue.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun getQuizTopics(): List<QuizTopic> {
    return listOf(
        QuizTopic(1, "ภาษาไทย ก-ฮ", "ทดสอบความรู้พยัญชนะไทย 44 ตัว", R.drawable.card_one, R.color.color_red),
        QuizTopic(2, "English A-Z", "Test your English Alphabet knowledge", R.drawable.card_two, R.color.color_blue),
        QuizTopic(3, "ตัวเลข 0-9", "ทดสอบการนับเลขและจำตัวเลข", R.drawable.card_three, R.color.color_green),
        QuizTopic(4, "English Words", "คำศัพท์ภาษาอังกฤษ A for Apple, B for Ball", R.drawable.card_one, R.color.color_pink),
        QuizTopic(5, "คำศัพท์อังกฤษ-ไทย", "ทายคำศัพท์จากรูปเป็นภาษาไทย", R.drawable.card_two, R.color.color_orange),
        QuizTopic(6, "ตัวเลขเป็นคำ", "Zero, One, Two... ทดสอบตัวเลขภาษาอังกฤษ", R.drawable.card_three, R.color.color_purple),
        QuizTopic(7, "สัตว์ Animals 🐾", "ทายชื่อสัตว์จากรูปภาพ", R.drawable.home_animal, R.color.color_blue),
        QuizTopic(8, "ผลไม้ Fruits 🍎", "ทายชื่อผลไม้ภาษาอังกฤษ", R.drawable.home_fruits, R.color.color_red),
        QuizTopic(9, "สี Colors 🎨", "ทดสอบชื่อสีภาษาอังกฤษ", R.drawable.home_color, R.color.color_purple),
        QuizTopic(10, "ผัก Vegetables 🥦", "ทายชื่อผักภาษาอังกฤษ", R.drawable.home_vegetable, R.color.color_green),
        QuizTopic(11, "กีฬา Sports ⚽", "ทายชื่อกีฬาจากรูปภาพ", R.drawable.home_sports, R.color.color_pink),
        QuizTopic(12, "ยานพาหนะ Vehicles 🚗", "ทายชื่อยานพาหนะภาษาอังกฤษ", R.drawable.home_vehicle, R.color.color_orange)
    )
}
