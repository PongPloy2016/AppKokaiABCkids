package com.pongploydev.priyankvex.paintapp.compose

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.QuizLeaderboardActivity
import com.pongploydev.priyankvex.paintapp.activity.QuizSessionActivity
import com.pongploydev.priyankvex.paintapp.adapter.QuizTopic
import kotlin.math.PI
import kotlin.math.sin

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

// Background components removed, using CommonComposeComponents.kt

// ─────────────────────────────────────────────
//  Main Screen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizComposeScreen() {
    val context = LocalContext.current
    val topics = remember { getQuizTopics() }

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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuizHeader()
                }

                itemsIndexed(topics) { index, topic ->
                    QuizTopicItem(
                        topic = topic,
                        index = index,
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
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                scaleIn(initialScale = 0.95f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    com.pongploydev.priyankvex.paintapp.log.AnalyticsLogger.logClickEvent(context, "หัวข้อแบบทดสอบ_${topic.title}", "หน้าแบบทดสอบ (Quiz)")
                    onClick()
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                        color = Color.White.copy(alpha = 0.6f),
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
                            color = Color(0xFF1A1A2E)
                        )
                        Text(
                            text = topic.description,
                            fontSize = 13.sp,
                            color = Color(0xFF444444),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = colorResource(id = topic.colorRes).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "บทเรียนที่ ${index + 1}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorResource(id = topic.colorRes)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("👉 เริ่มเลย!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KidDeepBlue)
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = KidDeepBlue.copy(alpha = 0.6f),
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
