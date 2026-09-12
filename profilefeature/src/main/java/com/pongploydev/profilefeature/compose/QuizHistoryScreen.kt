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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
        return "ล่าสุดวันนี้ • $nowStr น. (UTC+7)"
    }

    return try {
        val cleanIso = isoTimestamp.replace(" ", "T")
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = utcZone
        }
        val date = sdfInput.parse(cleanIso.substringBefore("+").replace("Z", "")) ?: Date()
        val sdfOutput = SimpleDateFormat("dd MMM yyyy • HH:mm 'น.'", Locale("th", "TH")).apply {
            timeZone = thaiZone
        }
        "${sdfOutput.format(date)} (UTC+7)"
    } catch (e: Exception) {
        try {
            val sdfInput2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                timeZone = utcZone
            }
            val date2 = sdfInput2.parse(isoTimestamp) ?: Date()
            val sdfOutput2 = SimpleDateFormat("dd MMM yyyy • HH:mm 'น.'", Locale("th", "TH")).apply {
                timeZone = thaiZone
            }
            "${sdfOutput2.format(date2)} (UTC+7)"
        } catch (e2: Exception) {
            "$isoTimestamp (UTC+7)"
        }
    }
}

// ==================== QUIZ HISTORY CARD LISTVIEW SCREEN ====================

@Composable
fun QuizHistoryListViewScreen(
    quizResults: List<UserQuizResult>,
    onBack: () -> Unit,
    onTakeMoreQuiz: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val totalScore = quizResults.sumOf { it.score }
    val totalQuestions = quizResults.sumOf { it.totalQuestions }
    val avgScore = if (totalQuestions > 0) (totalScore.toFloat() / totalQuestions * 100).toInt() else 0

    // Group results by quiz topic name (card ซ้ำ จะถูกรวมเป็น group ย่อยๆ)
    val groupedResults = remember(quizResults) {
        quizResults.groupBy { result ->
            result.quizTopicName.ifBlank { "แบบทดสอบวัดความรู้ทั่วไป" }
        }
    }

    // Map to keep track of expanded/collapsed state for each topic group
    val expandedGroupsState = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = KidTheme.Primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "กลับ",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ประวัติและรายละเอียดควิซ 📚",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${quizResults.size} ครั้ง", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KidTheme.Primary)
                            Text(text = "ควิซที่ทำแล้ว", fontSize = 12.sp, color = KidTheme.TextMuted)
                        }
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.LightGray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalScore คะแนน", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00B894))
                            Text(text = "คะแนนรวมที่ได้", fontSize = 12.sp, color = KidTheme.TextMuted)
                        }
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.LightGray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${groupedResults.size} หมวด", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                            Text(text = "หมวดหมู่ทั้งหมด", fontSize = 12.sp, color = KidTheme.TextMuted)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ประวัติแยกตามหมวดหมู่ 🔽",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.clickable {
                            val allExpanded = groupedResults.keys.all { expandedGroupsState[it] == true }
                            groupedResults.keys.forEach { expandedGroupsState[it] = !allExpanded }
                        }
                    ) {
                        Text(
                            text = "ย่อ/ขยาย ทั้งหมด",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (quizResults.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📚", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "ยังไม่มีประวัติการทำแบบทดสอบ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KidTheme.TextDark)
                            Text(
                                text = "ลองกด 'ทำแบบทดสอบเพิ่ม' ด้านล่างเพื่อวัดความรู้และสะสมดาวคะแนน!",
                                fontSize = 13.sp,
                                color = KidTheme.TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
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
                Spacer(modifier = Modifier.height(12.dp))
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
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidTheme.Primary)
                ) {
                    Text(text = "ทำแบบทดสอบเพิ่ม 🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(20.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column {
            // Group Header (Clickable to Expand / Collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = when {
                                    topicName.contains("ก-ฮ") -> "🎯"
                                    topicName.contains("วิดีโอ") -> "🎬"
                                    topicName.contains("A-Z") -> "🔤"
                                    else -> "📚"
                                },
                                fontSize = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = topicName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidTheme.TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "${items.size} รายการ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "รวม $totalTopicScore/$totalTopicQuestions คะแนน",
                                fontSize = 11.sp,
                                color = KidTheme.TextMuted
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "ย่อ" else "ขยาย",
                            tint = KidTheme.Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Expandable Items Container
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF1F5F9))
                    )

                    items.forEachIndexed { index, result ->
                        QuizResultCardItem(result = result, itemIndex = index + 1)
                    }
                }
            }
        }
    }
}

// ==================== QUIZ RESULT CARD ITEM WITH DATE & TIME ====================

@Composable
fun QuizResultCardItem(result: UserQuizResult, itemIndex: Int = 1) {
    val percentage = if (result.totalQuestions > 0) (result.score.toFloat() / result.totalQuestions * 100).toInt() else 0
    val isHighScore = percentage >= 80
    val formattedDateTime = remember(result.createdAt) { formatThaiDateTime(result.createdAt) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, if (isHighScore) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Item Number + Date & Time + Score Badge
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
                        contentDescription = "วันที่และเวลา",
                        tint = KidTheme.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDateTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = KidTheme.TextDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isHighScore) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, if (isHighScore) Color(0xFF86EFAC) else Color(0xFFFDE68A))
                ) {
                    Text(
                        text = "${result.score}/${result.totalQuestions} คะแนน (+${result.score} ⭐)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighScore) Color(0xFF166534) else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Score Progress Bar
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
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighScore) Color(0xFF10B981) else Color(0xFFF59E0B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Details (Time spent + Status badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "เวลาที่ใช้",
                        tint = KidTheme.TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ใช้เวลา: ${if (result.timeSpentSeconds > 0) "${result.timeSpentSeconds} วินาที" else "ไม่ระบุ"}",
                        fontSize = 11.sp,
                        color = KidTheme.TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isHighScore) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                ) {
                    Text(
                        text = if (isHighScore) "🎉 ผ่านเกณฑ์ระดับดีเยี่ยม" else "👍 ผ่านการทำแบบทดสอบ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighScore) Color(0xFF15803D) else KidTheme.TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
