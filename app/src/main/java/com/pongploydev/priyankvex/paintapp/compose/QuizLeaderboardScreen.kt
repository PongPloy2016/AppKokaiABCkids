package com.pongploydev.priyankvex.paintapp.compose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val rawTimestamp: String,
    val dateKey: String = ""
)

private fun extractDateKey(isoTimestamp: String?): String {
    if (isoTimestamp.isNullOrBlank()) return ""
    return try {
        val cleanIso = isoTimestamp.replace(" ", "T")
        if (cleanIso.contains("T")) {
            cleanIso.substringBefore("T")
        } else if (cleanIso.contains("-")) {
            cleanIso.take(10)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}

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
    var selectedDateKey by remember { mutableStateOf<String?>(null) }

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
                        rawTimestamp = res.createdAt ?: "",
                        dateKey = extractDateKey(res.createdAt)
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

    val filteredScores = remember(scores, selectedDateKey) {
        if (selectedDateKey.isNullOrBlank()) {
            scores
        } else {
            scores.filter { it.dateKey == selectedDateKey }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Table Calendar Demo", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E88E5) // Blue theme matching the screenshot
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Table Calendar Component (Matching screenshot exactly!)
            TableCalendarView(
                scores = scores,
                selectedDateKey = selectedDateKey,
                onSelectDate = { key -> selectedDateKey = key }
            )

            // Reset Filter Button bar if a date is filtered
            if (selectedDateKey != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "กรองวันที่: $selectedDateKey (${filteredScores.size} รายการ)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1565C0)
                    )
                    TextButton(onClick = { selectedDateKey = null }) {
                        Text("แสดงทั้งหมด ✕", fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                } else if (filteredScores.isEmpty()) {
                    Text(
                        if (selectedDateKey != null) "ไม่มีรายการคะแนนในวันที่เลือก 📅" else "ยังไม่มีคะแนนในขณะนี้\nมาเป็นคนแรกกันเถอะ! 🌟",
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
                        items(filteredScores) { score ->
                            LeaderboardItem(score)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCalendarView(
    scores: List<DisplayQuizScore>,
    selectedDateKey: String?,
    onSelectDate: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok")))
    }
    var isFullMonthMode by remember { mutableStateOf(false) }

    // Count events per dateKey
    val eventCountsMap = remember(scores) {
        scores.groupingBy { it.dateKey }.eachCount()
    }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayNumFormat = SimpleDateFormat("d", Locale.US)

    val currentMonthTitle = remember(currentMonthCalendar.time) {
        monthFormat.format(currentMonthCalendar.time)
    }

    // Build the grid of days
    val daysInGrid = remember(currentMonthCalendar.time, isFullMonthMode) {
        val cal = (currentMonthCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysList = mutableListOf<Date?>()

        if (isFullMonthMode) {
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val offset = (firstDayOfWeek + 5) % 7
            cal.add(Calendar.DAY_OF_MONTH, -offset)

            for (i in 0 until 35) {
                daysList.add(cal.time)
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            val targetCal = (currentMonthCalendar.clone() as Calendar)
            val dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK)
            val offset = (dayOfWeek + 5) % 7
            targetCal.add(Calendar.DAY_OF_MONTH, -offset)

            for (i in 0 until 7) {
                daysList.add(targetCal.time)
                targetCal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        daysList
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            // 1. Navigation Header (<  March 2019  [Full/Week]  >)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    val newCal = (currentMonthCalendar.clone() as Calendar).apply {
                        if (isFullMonthMode) add(Calendar.MONTH, -1) else add(Calendar.WEEK_OF_YEAR, -1)
                    }
                    currentMonthCalendar = newCal
                }) {
                    Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentMonthTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    // View Mode Toggle Button (Full / Week)
                    Surface(
                        onClick = { isFullMonthMode = !isFullMonthMode },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFF5722),
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (isFullMonthMode) "Full" else "Week",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(onClick = {
                    val newCal = (currentMonthCalendar.clone() as Calendar).apply {
                        if (isFullMonthMode) add(Calendar.MONTH, 1) else add(Calendar.WEEK_OF_YEAR, 1)
                    }
                    currentMonthCalendar = newCal
                }) {
                    Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Day of Week Header Row (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
            val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEachIndexed { index, day ->
                    val isWeekend = index >= 5 // Sat & Sun
                    Text(
                        text = day,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isWeekend) Color(0xFFE53935) else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Days Grid (Rows of 7 days)
            val rows = daysInGrid.chunked(7)
            rows.forEach { weekRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekRow.forEachIndexed { index, date ->
                        if (date != null) {
                            val dateKey = keyFormat.format(date)
                            val dayNumStr = dayNumFormat.format(date)
                            val isSelected = dateKey == selectedDateKey
                            val eventCount = eventCountsMap[dateKey] ?: 0
                            val isWeekend = index >= 5

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (isSelected) onSelectDate(null) else onSelectDate(dateKey)
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Date Number Indicator (Circular if selected)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFFF5722) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumStr,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isWeekend -> Color(0xFFE53935)
                                            else -> Color(0xFF212121)
                                        }
                                    )
                                }

                                // Event Dots (• • •) underneath date
                                if (eventCount > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        val dotsToShow = eventCount.coerceAtMost(4)
                                        repeat(dotsToShow) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF5D4037))
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(7.dp))
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}


private fun fetchFirestoreScores(onResult: (List<DisplayQuizScore>) -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH"))
    val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    FirebaseFirestore.getInstance().collection("quiz_scores")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(50)
        .get()
        .addOnSuccessListener { result ->
            try {
                val list = result.toObjects(QuizScore::class.java).map { s ->
                    val dateStr = s.timestamp?.let { dateFormat.format(it) } ?: "เพิ่งเมื่อสักครู่"
                    val key = s.timestamp?.let { dateKeyFormat.format(it) } ?: ""
                    DisplayQuizScore(
                        userName = s.userName.ifBlank { "ผู้เล่นนิรนาม" },
                        avatarUrl = null,
                        topicTitle = s.topicTitle.ifBlank { "แบบทดสอบทั่วไป" },
                        score = s.score,
                        totalQuestions = if (s.totalQuestions > 0) s.totalQuestions else 10,
                        durationSeconds = s.durationSeconds.toInt(),
                        dateString = dateStr,
                        rawTimestamp = s.timestamp?.time?.toString() ?: "",
                        dateKey = key
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


