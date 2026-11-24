package com.pongploydev.profilefeature.compose

import android.content.Context
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.profilefeature.data.UserQuizResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun switchToTab(context: Context, tabIndex: Int) {
    try {
        val activity = context as? androidx.fragment.app.FragmentActivity
        if (activity != null) {
            val viewPager = activity.findViewById<androidx.viewpager.widget.ViewPager>(
                context.resources.getIdentifier("viewpager", "id", context.packageName)
            )
            val viewPagerRtl = activity.findViewById<View>(
                context.resources.getIdentifier("viewpager_rtl", "id", context.packageName)
            )
            if (viewPager != null && viewPager.visibility == View.VISIBLE) {
                viewPager.currentItem = tabIndex
                return
            }
            if (viewPagerRtl is androidx.viewpager.widget.ViewPager && viewPagerRtl.visibility == View.VISIBLE) {
                viewPagerRtl.currentItem = tabIndex
                return
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Helper to format ISO timestamp or date into friendly Thai format with UTC+7 (Asia/Bangkok)
private fun formatThaiDateTime(isoTimestamp: String?): String {
    val thaiZone = TimeZone.getTimeZone("Asia/Bangkok")
    val utcZone = TimeZone.getTimeZone("UTC")

    if (isoTimestamp.isNullOrBlank()) {
        val nowStr = SimpleDateFormat("HH:mm", Locale("th", "TH")).apply {
            timeZone = thaiZone
        }.format(Date())
        return "วันนี้ · $nowStr น."
    }

    return try {
        val cleanIso = isoTimestamp.replace(" ", "T")
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = utcZone
        }
        val date = sdfInput.parse(cleanIso.substringBefore("+").replace("Z", "")) ?: Date()
        val sdfOutput = SimpleDateFormat("dd MMM yyyy · HH:mm 'น.'", Locale("th", "TH")).apply {
            timeZone = thaiZone
        }
        sdfOutput.format(date)
    } catch (e: Exception) {
        try {
            val sdfInput2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                timeZone = utcZone
            }
            val date2 = sdfInput2.parse(isoTimestamp) ?: Date()
            val sdfOutput2 = SimpleDateFormat("dd MMM yyyy · HH:mm 'น.'", Locale("th", "TH")).apply {
                timeZone = thaiZone
            }
            sdfOutput2.format(date2)
        } catch (e2: Exception) {
            isoTimestamp
        }
    }
}

// ==================== QUIZ HISTORY SCREEN ====================

@Composable
fun QuizHistoryListViewScreen(
    quizResults: List<UserQuizResult>,
    onBack: () -> Unit,
    onTakeMoreQuiz: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val totalScore = quizResults.sumOf { it.score }

    val groupedResults = remember(quizResults) {
        quizResults.groupBy { result ->
            result.quizTopicName.ifBlank { "แบบทดสอบวัดความรู้ทั่วไป" }
        }
    }

    val expandedGroupsState = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KidTheme.Background)
    ) {
        // Clean Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "กลับ",
                            tint = KidTheme.TextDark
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ประวัติการทำควิซ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark
                    )
                }
                HorizontalDivider(color = KidTheme.DividerColor)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, KidTheme.CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${quizResults.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = KidTheme.Primary
                            )
                            Text(text = "ครั้งที่ทำ", fontSize = 12.sp, color = KidTheme.TextMuted)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = KidTheme.DividerColor)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$totalScore",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F8A5B)
                            )
                            Text(text = "คะแนนรวม", fontSize = 12.sp, color = KidTheme.TextMuted)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = KidTheme.DividerColor)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${groupedResults.size}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE67E00)
                            )
                            Text(text = "หมวดหมู่", fontSize = 12.sp, color = KidTheme.TextMuted)
                        }
                    }
                }
            }

            if (quizResults.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "แยกตามหมวดหมู่",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidTheme.TextDark
                        )
                        val allExpanded = groupedResults.keys.all { expandedGroupsState[it] ?: true }
                        TextButton(
                            onClick = {
                                groupedResults.keys.forEach { expandedGroupsState[it] = !allExpanded }
                            }
                        ) {
                            Text(
                                text = if (allExpanded) "ย่อทั้งหมด" else "ขยายทั้งหมด",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = KidTheme.Primary
                            )
                        }
                    }
                }
            }

            if (quizResults.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KidTheme.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F3F7),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Quiz,
                                        contentDescription = null,
                                        tint = KidTheme.TextMuted,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ยังไม่มีประวัติการทำแบบทดสอบ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KidTheme.TextDark
                            )
                            Text(
                                text = "เริ่มทำแบบทดสอบเพื่อดูคะแนนและสถิติของคุณที่นี่",
                                fontSize = 12.5.sp,
                                color = KidTheme.TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                groupedResults.forEach { (topicName, topicItems) ->
                    item(key = topicName) {
                        val isExpanded = expandedGroupsState[topicName] ?: true
                        val totalTopicScore = topicItems.sumOf { it.score }
                        val totalTopicQuestions = topicItems.sumOf { it.totalQuestions }

                        QuizTopicGroupCard(
                            topicName = topicName,
                            items = topicItems,
                            isExpanded = isExpanded,
                            totalTopicScore = totalTopicScore,
                            totalTopicQuestions = totalTopicQuestions,
                            onToggleExpand = {
                                expandedGroupsState[topicName] = !isExpanded
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (onTakeMoreQuiz != null) {
                            onTakeMoreQuiz()
                        } else {
                            switchToTab(context, 3)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidTheme.Primary)
                ) {
                    Text(
                        text = "ทำแบบทดสอบเพิ่ม",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==================== EXPANDABLE TOPIC GROUP CARD ====================

@Composable
fun QuizTopicGroupCard(
    topicName: String,
    items: List<UserQuizResult>,
    isExpanded: Boolean,
    totalTopicScore: Int,
    totalTopicQuestions: Int,
    onToggleExpand: () -> Unit
) {
    val topicIcon = when {
        topicName.contains("วิดีโอ") -> Icons.Rounded.PlayCircleOutline
        topicName.contains("ก-ฮ") || topicName.contains("A-Z") -> Icons.AutoMirrored.Rounded.MenuBook
        else -> Icons.Rounded.Quiz
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, KidTheme.CardBorder)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F3F9),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = topicIcon,
                            contentDescription = null,
                            tint = KidTheme.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topicName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KidTheme.TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${items.size} ครั้ง · รวม $totalTopicScore/$totalTopicQuestions คะแนน",
                        fontSize = 12.sp,
                        color = KidTheme.TextMuted
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "ย่อ" else "ขยาย",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = KidTheme.DividerColor)

                    items.forEachIndexed { index, result ->
                        QuizResultCardItem(result = result, itemIndex = index + 1)
                    }
                }
            }
        }
    }
}

// ==================== QUIZ RESULT CARD ITEM ====================

@Composable
fun QuizResultCardItem(result: UserQuizResult, itemIndex: Int = 1) {
    val percentage = if (result.totalQuestions > 0) (result.score.toFloat() / result.totalQuestions * 100).toInt() else 0
    val isHighScore = percentage >= 80
    val formattedDateTime = remember(result.createdAt) { formatThaiDateTime(result.createdAt) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, KidTheme.DividerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = KidTheme.TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDateTime,
                        fontSize = 12.sp,
                        color = KidTheme.TextMuted
                    )
                }

                Text(
                    text = "${result.score}/${result.totalQuestions} คะแนน",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighScore) Color(0xFF1F8A5B) else Color(0xFFD97706)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isHighScore) Color(0xFF10B981) else Color(0xFFF59E0B),
                    trackColor = Color(0xFFE5E7EB)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KidTheme.TextDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = KidTheme.TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (result.timeSpentSeconds > 0) "ใช้เวลา ${result.timeSpentSeconds} วินาที" else "ไม่ระบุเวลา",
                        fontSize = 11.5.sp,
                        color = KidTheme.TextMuted
                    )
                }

                Text(
                    text = if (isHighScore) "ผ่านเกณฑ์ดีเยี่ยม" else "เสร็จสิ้น",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isHighScore) Color(0xFF1F8A5B) else KidTheme.TextMuted
                )
            }
        }
    }
}
