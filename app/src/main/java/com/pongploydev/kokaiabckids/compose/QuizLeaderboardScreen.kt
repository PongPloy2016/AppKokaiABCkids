package com.pongploydev.kokaiabckids.compose

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.kokaiabckids.model.QuizScore
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
    val dateKey: String = "",
    val email: String? = null
)

data class UserLeaderboardSummary(
    val userId: String,
    val userName: String,
    val userEmail: String?,
    val avatarUrl: String?,
    val totalScore: Int,
    val totalQuestions: Int,
    val totalGamesPlayed: Int,
    val totalStars: Int,
    val latestDateString: String,
    val rawLatestTimestamp: String,
    val latestTopicTitle: String = "",
    val latestDurationSeconds: Int = 0,
    val latestScore: Int = 0,
    val latestTotalQuestions: Int = 10
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

private fun aggregateUserSummaries(
    scores: List<DisplayQuizScore>,
    profilesList: List<com.pongploydev.profilefeature.data.UserProfile> = emptyList(),
    currentProfile: com.pongploydev.profilefeature.data.UserProfile? = null
): List<UserLeaderboardSummary> {
    val nameToProfileMap = profilesList.filter { !it.displayName.isNullOrBlank() }.associateBy { it.displayName }
    val grouped = scores.groupBy { it.userName }
    return grouped.map { (userName, userScores) ->
        val firstScore = userScores.first()
        val totalScore = userScores.sumOf { it.score }
        val totalQuestions = userScores.sumOf { it.totalQuestions }
        val totalGames = userScores.size
        val totalStars = totalScore
        val latest = userScores.maxByOrNull { it.rawTimestamp } ?: firstScore

        val matchingProfile = nameToProfileMap[userName]
        val avatarUrl = firstScore.avatarUrl?.ifBlank { null }
            ?: matchingProfile?.avatarUrl?.ifBlank { null }
            ?: if (userName == currentProfile?.displayName) currentProfile?.avatarUrl?.ifBlank { null } else null

        val userEmail = firstScore.email?.ifBlank { null }
            ?: userScores.mapNotNull { it.email?.ifBlank { null } }.firstOrNull()
            ?: matchingProfile?.email?.ifBlank { null }
            ?: if (userName == currentProfile?.displayName) currentProfile?.email?.ifBlank { null } else null

        UserLeaderboardSummary(
            userId = firstScore.userName,
            userName = userName,
            userEmail = userEmail,
            avatarUrl = avatarUrl,
            totalScore = totalScore,
            totalQuestions = totalQuestions,
            totalGamesPlayed = totalGames,
            totalStars = totalStars,
            latestDateString = latest.dateString,
            rawLatestTimestamp = latest.rawTimestamp,
            latestTopicTitle = latest.topicTitle,
            latestDurationSeconds = latest.durationSeconds,
            latestScore = latest.score,
            latestTotalQuestions = latest.totalQuestions
        )
    }.sortedByDescending { it.totalScore }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizLeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var scores by remember { mutableStateOf<List<DisplayQuizScore>>(emptyList()) }
    var profilesList by remember { mutableStateOf<List<com.pongploydev.profilefeature.data.UserProfile>>(emptyList()) }
    var currentProfile by remember { mutableStateOf<com.pongploydev.profilefeature.data.UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf(0) } // 0 = 🏆 คะแนนรวมรายคน, 1 = 📅 ประวัติการเล่น

    LaunchedEffect(Unit) {
        val dataManager = SupabaseDataManager.getInstance(context)
        val authManager = SupabaseAuthManager.getInstance(context)
        val currentUserId = authManager.getStoredUser()?.id ?: authManager.getGuestUserId()

        try {
            // 1. Fetch Supabase Profiles & Quiz Results
            val profilesResult = dataManager.fetchAllUserProfiles().getOrDefault(emptyList())
            profilesList = profilesResult
            currentProfile = dataManager.fetchUserProfile(currentUserId).getOrNull()

            val profilesMap = profilesResult.associateBy { it.id }
            val nameToProfileMap = profilesResult.filter { !it.displayName.isNullOrBlank() }.associateBy { it.displayName }

            val supabaseResults = dataManager.fetchAllQuizResults()
            val listFromSupabase = supabaseResults.getOrNull() ?: emptyList()

            val supabaseMapped = listFromSupabase.map { res ->
                val userProf = profilesMap[res.userId] ?: nameToProfileMap[res.userId]
                val userProfAvatar = userProf?.avatarUrl?.ifBlank { null }
                val currentProfAvatar = if (res.userId == currentProfile?.id) currentProfile?.avatarUrl?.ifBlank { null } else null
                val avatar = userProfAvatar ?: currentProfAvatar

                val userProfName = userProf?.displayName?.ifBlank { null }
                val currentProfName = if (res.userId == currentProfile?.id) currentProfile?.displayName?.ifBlank { null } else null
                val name = userProfName ?: currentProfName ?: if (res.userId.isNotBlank()) res.userId else "ผู้เล่นนิรนาม"

                DisplayQuizScore(
                    userName = name,
                    avatarUrl = avatar,
                    topicTitle = res.quizTopicName.ifBlank { "แบบทดสอบวัดความรู้" },
                    score = res.score,
                    totalQuestions = if (res.totalQuestions > 0) res.totalQuestions else 10,
                    durationSeconds = res.timeSpentSeconds,
                    dateString = formatThaiDateTime(res.createdAt),
                    rawTimestamp = res.createdAt ?: "",
                    dateKey = extractDateKey(res.createdAt)
                )
            }

            // Combine Supabase scores with Firestore scores so no scores/users are lost
            fetchFirestoreScores(profilesResult, currentProfile) { firestoreScores ->
                scores = (supabaseMapped + firestoreScores).sortedByDescending { it.rawTimestamp }
                isLoading = false
                Log.d("Leaderboard", "Loaded total ${scores.size} scores (Supabase + Firestore)")
            }
        } catch (e: Exception) {
            Log.e("Leaderboard", "Error fetching scores from Supabase/Firestore", e)
            fetchFirestoreScores(profilesList, currentProfile) { firestoreScores ->
                scores = firestoreScores
                isLoading = false
            }
        }
    }

    val userSummaries = remember(scores, profilesList, currentProfile) {
        aggregateUserSummaries(scores, profilesList, currentProfile)
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
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("🏆 ตารางอันดับคะแนน", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "กลับ", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E88E5)
                    )
                )

                // Tab Row Switcher (🏆 ตารางอันดับ vs 📅 ประวัติการเล่น)
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = Color(0xFF5E35B1),
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = {
                            Text(
                                "🏆 ตารางอันดับ",
                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = {
                            Text(
                                "📅 ประวัติการเล่น",
                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (currentTab == 0) {
                // TAB 0: 🏆 ตารางอันดับ (Podium Top 3 + Purple List Rows)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF4F5F9))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF673AB7)
                        )
                    } else if (userSummaries.isEmpty()) {
                        Text(
                            "ยังไม่มีอันดับคะแนนในขณะนี้ 🌟",
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    } else {
                        val top3 = userSummaries.take(3)
                        val rank4AndBelow = userSummaries.drop(3)
                        val currentUserName = currentProfile?.displayName

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 1. Podium Section for Top 3
                            item {
                                Top3PodiumSection(
                                    top3List = top3,
                                    currentProfileName = currentUserName
                                )
                            }

                            // 2. Table Header Bar (อันดับ | ผู้เล่น | คะแนนรวม)
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 28.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "อันดับ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.width(44.dp)
                                    )
                                    Text(
                                        "ผู้เล่น",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "คะแนนรวม",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // 3. List Rows for Rank 4 and below
                            itemsIndexed(rank4AndBelow) { index, summary ->
                                val rankNumber = index + 4
                                LeaderboardRowCard(
                                    rank = rankNumber,
                                    summary = summary,
                                    currentProfileName = currentUserName
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 1: 📅 ประวัติการเล่น (Table Calendar Filter View)
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
}

@Composable
fun Top3PodiumSection(
    top3List: List<UserLeaderboardSummary>,
    currentProfileName: String?
) {
    if (top3List.isEmpty()) return

    val rank1 = top3List.getOrNull(0)
    val rank2 = top3List.getOrNull(1)
    val rank3 = top3List.getOrNull(2)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Rank 2 (Left)
            if (rank2 != null) {
                PodiumUserItem(
                    summary = rank2,
                    rank = 2,
                    avatarSize = 64.dp,
                    isCenter = false,
                    currentProfileName = currentProfileName
                )
            } else {
                Spacer(modifier = Modifier.width(84.dp))
            }

            // Rank 1 (Center - Taller with Crown)
            if (rank1 != null) {
                PodiumUserItem(
                    summary = rank1,
                    rank = 1,
                    avatarSize = 82.dp,
                    isCenter = true,
                    currentProfileName = currentProfileName
                )
            }

            // Rank 3 (Right)
            if (rank3 != null) {
                PodiumUserItem(
                    summary = rank3,
                    rank = 3,
                    avatarSize = 64.dp,
                    isCenter = false,
                    currentProfileName = currentProfileName
                )
            } else {
                Spacer(modifier = Modifier.width(84.dp))
            }
        }
    }
}

@Composable
fun PodiumUserItem(
    summary: UserLeaderboardSummary,
    rank: Int,
    avatarSize: androidx.compose.ui.unit.Dp,
    isCenter: Boolean,
    currentProfileName: String?
) {
    val isCurrentUser = !currentProfileName.isNullOrBlank() && summary.userName == currentProfileName
    var showDetailDialog by remember { mutableStateOf(false) }

    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Text(
                    text = "🏆 รายละเอียดคะแนน (${summary.userName})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• ชื่อผู้เล่น: \"${summary.userName}\"", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("• หมวดหมู่แบบทดสอบ: \"${if (summary.latestTopicTitle.isNotBlank()) summary.latestTopicTitle else "แบบทดสอบทั่วไป"}\"", fontSize = 14.sp)
                    Text("• คะแนนที่ได้: ${summary.latestScore} / ${summary.latestTotalQuestions} ข้อ (คะแนนรวม: ${summary.totalScore})", fontSize = 14.sp)
                    Text("• เวลาที่ใช้: ${summary.latestDurationSeconds} วินาที", fontSize = 14.sp)
                    Text("• วันเวลาที่เล่น: ${summary.latestDateString}", fontSize = 14.sp)
                    Text("• อีเมลสมาชิก: ${maskEmail(summary.userEmail)}", fontSize = 14.sp, color = Color(0xFF1E88E5))
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text("ปิด")
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(96.dp)
            .clickable { showDetailDialog = true }
    ) {
        // Crown for Rank 1
        if (rank == 1) {
            Text(
                text = "👑",
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            // Avatar Image with Colored Border
            Surface(
                modifier = Modifier
                    .size(avatarSize)
                    .padding(bottom = 6.dp),
                shape = CircleShape,
                color = Color(0xFFEDE7F6),
                border = BorderStroke(
                    width = if (rank == 1) 3.dp else 2.dp,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        else -> Color(0xFFCD7F32) // Bronze
                    }
                )
            ) {
                if (!summary.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = summary.avatarUrl,
                        contentDescription = summary.userName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = (avatarSize.value * 0.45).sp)
                    }
                }
            }

            // Rank Number Badge (Purple Circle)
            Surface(
                shape = CircleShape,
                color = Color(0xFF6C5CE7),
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = 2.dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Player Name (with YOU badge if current user)
        Text(
            text = if (isCurrentUser) "${summary.userName}\n(YOU)" else summary.userName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748),
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Points
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${summary.totalScore} คะแนน",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7C4DFF)
            )
        }
    }
}

@Composable
fun LeaderboardRowCard(
    rank: Int,
    summary: UserLeaderboardSummary,
    currentProfileName: String?
) {
    val isCurrentUser = !currentProfileName.isNullOrBlank() && summary.userName == currentProfileName
    val cardBgColor = if (isCurrentUser) Color(0xFF5E35B1) else Color(0xFF7E57C2)
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Top Row (Rank, Avatar, Player Name, Points)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Number
                Text(
                    text = "$rank",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.width(32.dp)
                )

                // Avatar Photo Circle
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    if (!summary.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = summary.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Player Name & Topic Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCurrentUser) "${summary.userName} (YOU)" else summary.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    if (summary.latestTopicTitle.isNotBlank()) {
                        Text(
                            text = "📚 ${summary.latestTopicTitle}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                }

                // Points Badge
                Text(
                    text = "${summary.totalScore} คะแนน",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Expanded Details Section inside the Purple Card
            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.18f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        DetailRow("ชื่อผู้เล่น:", "\"${summary.userName}\"")
                        DetailRow("หมวดหมู่แบบทดสอบ:", "\"${if (summary.latestTopicTitle.isNotBlank()) summary.latestTopicTitle else "แบบทดสอบทั่วไป"}\"")
                        DetailRow("คะแนนที่ได้:", "${summary.latestScore}")
                        DetailRow("จำนวนข้อทั้งหมด:", "${summary.latestTotalQuestions}")
                        DetailRow("เวลาที่ใช้:", "${summary.latestDurationSeconds} วินาที")
                        DetailRow("วันเวลาที่เล่น:", summary.latestDateString)
                        DetailRow("อีเมลสมาชิก:", maskEmail(summary.userEmail))
                    }
                }
            }
        }
    }
}

private fun maskEmail(email: String?): String {
    if (email.isNullOrBlank()) return "-"
    val parts = email.split("@")
    if (parts.size != 2) return email
    val username = parts[0]
    val domain = parts[1]

    val maskedUsername = if (username.length > 2) {
        username.dropLast(2) + "XX"
    } else {
        username.take(1) + "XX"
    }
    return "$maskedUsername@$domain"
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
        )
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


private fun fetchFirestoreScores(
    profilesList: List<com.pongploydev.profilefeature.data.UserProfile> = emptyList(),
    currentProfile: com.pongploydev.profilefeature.data.UserProfile? = null,
    onResult: (List<DisplayQuizScore>) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH"))
    val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val nameToProfileMap = profilesList.filter { !it.displayName.isNullOrBlank() }.associateBy { it.displayName }

    val firestore = FirebaseFirestore.getInstance()

    fun parseSnapshot(result: com.google.firebase.firestore.QuerySnapshot): List<DisplayQuizScore> {
        return result.toObjects(QuizScore::class.java).map { s ->
            val dateStr = s.timestamp?.let { dateFormat.format(it) } ?: "เพิ่งเมื่อสักครู่"
            val key = s.timestamp?.let { dateKeyFormat.format(it) } ?: ""
            val matchingProf = nameToProfileMap[s.userName]
            val avatar = s.avatarUrl?.ifBlank { null }
                ?: matchingProf?.avatarUrl?.ifBlank { null }
                ?: if (s.userName == currentProfile?.displayName) currentProfile?.avatarUrl?.ifBlank { null } else null

            DisplayQuizScore(
                userName = s.userName.ifBlank { "ผู้เล่นนิรนาม" },
                avatarUrl = avatar,
                topicTitle = s.topicTitle.ifBlank { "แบบทดสอบทั่วไป" },
                score = s.score,
                totalQuestions = if (s.totalQuestions > 0) s.totalQuestions else 10,
                durationSeconds = s.durationSeconds.toInt(),
                dateString = dateStr,
                rawTimestamp = s.timestamp?.time?.toString() ?: "",
                dateKey = key,
                email = s.email?.ifBlank { null }
            )
        }
    }

    firestore.collection("quiz_scores_game")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(50)
        .get()
        .addOnSuccessListener { result1 ->
            val list1 = parseSnapshot(result1)
            firestore.collection("quiz_scores")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener { result2 ->
                    val list2 = parseSnapshot(result2)
                    onResult((list1 + list2).sortedByDescending { it.rawTimestamp })
                }
                .addOnFailureListener { onResult(list1) }
        }
        .addOnFailureListener {
            firestore.collection("quiz_scores")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener { res2 -> onResult(parseSnapshot(res2)) }
                .addOnFailureListener { onResult(emptyList()) }
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


