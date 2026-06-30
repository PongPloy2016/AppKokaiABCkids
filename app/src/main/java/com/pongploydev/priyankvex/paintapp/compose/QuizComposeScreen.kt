package com.pongploydev.priyankvex.paintapp.compose

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.QuizSessionActivity
import com.pongploydev.priyankvex.paintapp.adapter.QuizTopic

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
                        "คลังแบบทดสอบ",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A69FF)
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuizHeader()
            }

            itemsIndexed(topics) { index, topic ->
                QuizTopicItem(topic, index) {
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

@Composable
fun QuizHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A69FF))
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎯", fontSize = 32.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "เลือกหัวข้อที่ชอบ",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "ฝึกฝนและท้าทายตัวเองกันเถอะ! 🌟",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuizTopicItem(topic: QuizTopic, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color(0xFFECEEF2))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Left Color Stripe
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(8.dp)
                        .background(colorResource(id = topic.colorRes))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color(0xFFF8F9FA)
                    ) {
                        Image(
                            painter = painterResource(id = topic.iconRes),
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3142)
                        )
                        Text(
                            text = topic.description,
                            fontSize = 13.sp,
                            color = Color(0xFF9094A6),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = colorResource(id = topic.colorRes).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "บทเรียนที่ ${index + 1}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = topic.colorRes)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(24.dp)
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
