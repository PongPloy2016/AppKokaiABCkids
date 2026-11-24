package com.pongploydev.kokaiabckids.compose

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.activity.QuizLeaderboardActivity
import com.pongploydev.kokaiabckids.activity.QuizPlayMode
import com.pongploydev.kokaiabckids.activity.QuizSessionActivity
import com.pongploydev.kokaiabckids.log.AnalyticsLogger
import com.pongploydev.kokaiabckids.utils.QuizProgressManager
import com.pongploydev.profilefeature.QuizHistoryActivity
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class QuizHubTab(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    LESSONS("บทเรียน", Icons.AutoMirrored.Rounded.MenuBook, Color(0xFF0284C7)),
    LISTENING("ฟังเสียง", Icons.Rounded.Headphones, Color(0xFF0D9488)),
    MATH("คิดเลข", Icons.Rounded.Calculate, Color(0xFF16A34A)),
    CHALLENGE("ท้าทาย", Icons.Rounded.Bolt, Color(0xFFEA580C))
}

private enum class QuizGroup {
    LANGUAGE,
    VOCABULARY
}

private data class QuizTopicEntry(
    val id: Int,
    val title: String,
    val description: String,
    val group: QuizGroup,
    val iconRes: Int,
    val isFullBleed: Boolean = false,
    val accentColor: Color,
    val stageBgColor: Color,
    val questionCount: Int = 20,
    val playMode: QuizPlayMode = QuizPlayMode.STANDARD,
    val badgeLabel: String? = null
)

@Composable
fun QuizComposeScreen(
    isStandalone: Boolean = false,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val topics = remember { getAllQuizTopics() }
    val listeningTopics = remember { getListeningQuizTopics() }
    val mathTopics = remember { getMathQuizTopics() }
    val bonusWorldTopics = remember { getBonusChallengeTopics() }

    val supabaseUser = remember { SupabaseAuthManager.getInstance(context).getStoredUser() }
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val userId = supabaseUser?.id ?: firebaseUser?.uid

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf(QuizHubTab.LESSONS) }
    var selectedFilter by remember { mutableIntStateOf(0) }
    var lockedDialogTopic by remember { mutableStateOf<QuizTopicEntry?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
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
                    refreshTrigger++
                }
            }
        }
    }

    // Compute progress stats from QuizProgressManager
    val progressSnapshot = remember(refreshTrigger) {
        var passedCount = 0
        var totalStars = 0
        var unlockedCount = 0
        var nextRecommended: QuizTopicEntry? = null

        topics.forEach { topic ->
            val unlocked = QuizProgressManager.isTopicUnlocked(context, topic.id)
            val score = QuizProgressManager.getTopicHighScore(context, topic.id)
            val passed = QuizProgressManager.isTopicPassed(context, topic.id)
            if (unlocked) {
                unlockedCount++
                if (!passed && nextRecommended == null) {
                    nextRecommended = topic
                }
            }
            if (passed) passedCount++
            totalStars += QuizProgressManager.getStarRating(score, topic.questionCount)
        }

        val mixedBest = QuizProgressManager.getTopicHighScore(context, QuizProgressManager.TOPIC_MIXED_CHALLENGE)
        val speedBlitzBest = QuizProgressManager.getTopicHighScore(context, QuizProgressManager.TOPIC_SPEED_BLITZ)
        val listeningStars = listeningTopics.sumOf {
            QuizProgressManager.getStarRating(QuizProgressManager.getTopicHighScore(context, it.id), it.questionCount)
        }
        val mathStars = mathTopics.sumOf {
            QuizProgressManager.getStarRating(QuizProgressManager.getTopicHighScore(context, it.id), it.questionCount)
        }

        QuizHubStats(
            passedCount = passedCount,
            totalTopics = topics.size,
            earnedStars = totalStars,
            maxStars = topics.size * 3,
            unlockedCount = unlockedCount,
            nextTopic = nextRecommended ?: topics.first(),
            mixedBestScore = mixedBest,
            speedBlitzBestScore = speedBlitzBest,
            listeningStars = listeningStars,
            listeningMaxStars = listeningTopics.size * 3,
            mathStars = mathStars,
            mathMaxStars = mathTopics.size * 3
        )
    }

    val filteredTopics = remember(selectedFilter, refreshTrigger, topics) {
        when (selectedFilter) {
            1 -> topics.filter { it.group == QuizGroup.LANGUAGE }
            2 -> topics.filter { it.group == QuizGroup.VOCABULARY }
            3 -> topics.filter { QuizProgressManager.isTopicUnlocked(context, it.id) }
            else -> topics
        }
    }

    fun launchTopic(
        topicId: Int,
        topicTitle: String,
        playMode: QuizPlayMode = QuizPlayMode.STANDARD,
        questionCount: Int = 20
    ) {
        AnalyticsLogger.logClickEvent(context, "หัวข้อแบบทดสอบ_${playMode.name}_$topicTitle", "หน้าแบบทดสอบ (Quiz)")
        val intent = Intent(context, QuizSessionActivity::class.java).apply {
            putExtra("EXTRA_TOPIC_ID", topicId)
            putExtra("EXTRA_TOPIC_TITLE", topicTitle)
            putExtra(QuizSessionActivity.EXTRA_QUIZ_MODE, playMode.name)
            putExtra(QuizSessionActivity.EXTRA_QUESTION_COUNT, questionCount)
        }
        context.startActivity(intent)
    }

    val rootModifier = Modifier
        .fillMaxSize()
        .then(if (isStandalone) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier)

    Box(modifier = Modifier.fillMaxSize()) {
        // Playful Kids Storybook Backdrop
        QuizStorybookBackdrop()

        Column(modifier = rootModifier) {
            // Clean Header Bar
            QuizTopHeader(
                isStandalone = isStandalone,
                onBack = onBack,
                onHistoryClick = { QuizHistoryActivity.launch(context) },
                onLeaderboardClick = {
                    context.startActivity(Intent(context, QuizLeaderboardActivity::class.java))
                }
            )

            // 4-Tab Mode Switcher (บทเรียน + 3 แท็บทดสอบเพิ่มเติม: ฟังเสียง, คิดเลข, ท้าทาย)
            QuizModeTabBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = if (isStandalone) 28.dp else 118.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    QuizHubTab.LESSONS -> {
                        // Hero Progress & Quick-Start Card
                        item(key = "hero_dashboard") {
                            QuizProgressHeroCard(
                                stats = progressSnapshot,
                                onContinueClick = {
                                    launchTopic(progressSnapshot.nextTopic.id, progressSnapshot.nextTopic.title)
                                },
                                onMixedChallengeClick = {
                                    launchTopic(QuizProgressManager.TOPIC_MIXED_CHALLENGE, "ท้าทายรวมทุกหมวด")
                                }
                            )
                        }

                        // Filter Chips
                        item(key = "filter_chips") {
                            QuizFilterRow(
                                selectedIndex = selectedFilter,
                                unlockedCount = progressSnapshot.unlockedCount,
                                totalCount = topics.size,
                                onSelect = { selectedFilter = it }
                            )
                        }

                        // Lesson Cards
                        itemsIndexed(
                            items = filteredTopics,
                            key = { _, item -> item.id }
                        ) { listIndex, topic ->
                            val isUnlocked = remember(refreshTrigger, topic.id) {
                                QuizProgressManager.isTopicUnlocked(context, topic.id)
                            }
                            val highScore = remember(refreshTrigger, topic.id) {
                                QuizProgressManager.getTopicHighScore(context, topic.id)
                            }
                            val isRecommended = isUnlocked && topic.id == progressSnapshot.nextTopic.id && !QuizProgressManager.isTopicPassed(context, topic.id)

                            QuizTopicCard(
                                topic = topic,
                                animationIndex = listIndex,
                                isUnlocked = isUnlocked,
                                isRecommended = isRecommended,
                                highScore = highScore,
                                onClick = {
                                    if (isUnlocked) {
                                        launchTopic(topic.id, topic.title, topic.playMode, topic.questionCount)
                                    } else {
                                        lockedDialogTopic = topic
                                    }
                                }
                            )
                        }
                    }

                    QuizHubTab.LISTENING -> {
                        item(key = "listening_banner") {
                            QuizModeBannerCard(
                                title = "ฟังเสียงทายภาพ",
                                subtitle = "ฝึกฟังเสียงอ่านไทยและอังกฤษ แล้วแตะเลือกรูปภาพให้ตรงกัน เหมาะสำหรับน้อง ๆ วัยเริ่มเรียน",
                                badgeText = "${progressSnapshot.listeningStars}/${progressSnapshot.listeningMaxStars}",
                                buttonText = "เริ่มฟังเสียงทันที",
                                icon = Icons.Rounded.Headphones,
                                gradientColors = listOf(Color(0xFF0D9488), Color(0xFF0284C7)),
                                onActionClick = {
                                    val first = listeningTopics.first()
                                    launchTopic(first.id, first.title, first.playMode, first.questionCount)
                                }
                            )
                        }

                        itemsIndexed(
                            items = listeningTopics,
                            key = { _, item -> item.id }
                        ) { listIndex, topic ->
                            val highScore = remember(refreshTrigger, topic.id) {
                                QuizProgressManager.getTopicHighScore(context, topic.id)
                            }
                            QuizTopicCard(
                                topic = topic,
                                animationIndex = listIndex,
                                isUnlocked = true,
                                isRecommended = listIndex == 0 && highScore < 0,
                                highScore = highScore,
                                onClick = {
                                    launchTopic(topic.id, topic.title, topic.playMode, topic.questionCount)
                                }
                            )
                        }
                    }

                    QuizHubTab.MATH -> {
                        item(key = "math_banner") {
                            QuizModeBannerCard(
                                title = "คิดเลขแสนสนุก",
                                subtitle = "ฝึกนับจำนวน บวกเลข และลบเลข พร้อมภาพประกอบช่วยนับสีสันสดใส เข้าใจง่าย",
                                badgeText = "${progressSnapshot.mathStars}/${progressSnapshot.mathMaxStars}",
                                buttonText = "เริ่มนับเลขเลย!",
                                icon = Icons.Rounded.Calculate,
                                gradientColors = listOf(Color(0xFF16A34A), Color(0xFF059669)),
                                onActionClick = {
                                    val first = mathTopics.first()
                                    launchTopic(first.id, first.title, first.playMode, first.questionCount)
                                }
                            )
                        }

                        itemsIndexed(
                            items = mathTopics,
                            key = { _, item -> item.id }
                        ) { listIndex, topic ->
                            val highScore = remember(refreshTrigger, topic.id) {
                                QuizProgressManager.getTopicHighScore(context, topic.id)
                            }
                            QuizTopicCard(
                                topic = topic,
                                animationIndex = listIndex,
                                isUnlocked = true,
                                isRecommended = listIndex == 0 && highScore < 0,
                                highScore = highScore,
                                onClick = {
                                    launchTopic(topic.id, topic.title, topic.playMode, topic.questionCount)
                                }
                            )
                        }
                    }

                    QuizHubTab.CHALLENGE -> {
                        item(key = "speed_challenge_banner") {
                            QuizSpeedChallengeHeroCard(
                                speedBest = progressSnapshot.speedBlitzBestScore,
                                mixedBest = progressSnapshot.mixedBestScore,
                                onSpeedBlitzClick = {
                                    launchTopic(
                                        topicId = QuizProgressManager.TOPIC_SPEED_BLITZ,
                                        topicTitle = "สายฟ้า 60 วินาที",
                                        playMode = QuizPlayMode.TIMED,
                                        questionCount = 30
                                    )
                                },
                                onMixedClick = {
                                    launchTopic(
                                        topicId = QuizProgressManager.TOPIC_MIXED_CHALLENGE,
                                        topicTitle = "ท้าทายรวมทุกหมวด",
                                        playMode = QuizPlayMode.STANDARD,
                                        questionCount = 20
                                    )
                                }
                            )
                        }

                        item(key = "bonus_section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "หมวดความรู้รอบตัวเพิ่มเติม",
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFEDD5)
                                ) {
                                    Text(
                                        text = "ปลดล็อคแล้ว ${bonusWorldTopics.size} หมวด",
                                        color = Color(0xFFC2410C),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        itemsIndexed(
                            items = bonusWorldTopics,
                            key = { _, item -> item.id }
                        ) { listIndex, topic ->
                            val highScore = remember(refreshTrigger, topic.id) {
                                QuizProgressManager.getTopicHighScore(context, topic.id)
                            }
                            QuizTopicCard(
                                topic = topic,
                                animationIndex = listIndex,
                                isUnlocked = true,
                                isRecommended = false,
                                highScore = highScore,
                                onClick = {
                                    launchTopic(topic.id, topic.title, topic.playMode, topic.questionCount)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Helpful Dialog when tapping a locked lesson
    lockedDialogTopic?.let { lockedTopic ->
        val prereqTopic = topics.find { it.id == lockedTopic.id - 1 }
        val prereqUnlocked = prereqTopic != null && QuizProgressManager.isTopicUnlocked(context, prereqTopic.id)

        AlertDialog(
            onDismissRequest = { lockedDialogTopic = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "บทเรียนที่ ${lockedTopic.id} ยังล็อคอยู่",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                val prereqName = prereqTopic?.title ?: "บทเรียนก่อนหน้า"
                Text(
                    text = "ทำแบบทดสอบบทที่ ${lockedTopic.id - 1} ($prereqName) ให้ได้คะแนนอย่างน้อย 10/20 เพื่อปลดล็อคบทเรียนนี้",
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                if (prereqTopic != null && prereqUnlocked) {
                    Button(
                        onClick = {
                            lockedDialogTopic = null
                            launchTopic(prereqTopic.id, prereqTopic.title)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("ไปทำบทที่ ${prereqTopic.id}", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            lockedDialogTopic = null
                            launchTopic(progressSnapshot.nextTopic.id, progressSnapshot.nextTopic.title)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("เล่นด่านถัดไป", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { lockedDialogTopic = null }) {
                    Text("ปิด", color = Color(0xFF64748B))
                }
            }
        )
    }
}

private data class QuizHubStats(
    val passedCount: Int,
    val totalTopics: Int,
    val earnedStars: Int,
    val maxStars: Int,
    val unlockedCount: Int,
    val nextTopic: QuizTopicEntry,
    val mixedBestScore: Int,
    val speedBlitzBestScore: Int,
    val listeningStars: Int,
    val listeningMaxStars: Int,
    val mathStars: Int,
    val mathMaxStars: Int
)

@Composable
private fun QuizModeTabBar(
    selectedTab: QuizHubTab,
    onSelectTab: (QuizHubTab) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.2.dp, Color(0xFFBAE6FD)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuizHubTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) tab.accentColor else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) Color.White else tab.accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizModeBannerCard(
    title: String,
    subtitle: String,
    badgeText: String,
    buttonText: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = 68.dp.toPx(),
                    center = Offset(size.width * 0.92f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = 44.dp.toPx(),
                    center = Offset(size.width * 0.10f, size.height * 0.92f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onActionClick)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = gradientColors.first(),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buttonText,
                            color = Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizSpeedChallengeHeroCard(
    speedBest: Int,
    mixedBest: Int,
    onSpeedBlitzClick: () -> Unit,
    onMixedClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFF59E0B))
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = 70.dp.toPx(),
                    center = Offset(size.width * 0.90f, size.height * 0.18f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ทดสอบสายฟ้า 60 วินาที",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (speedBest >= 0) {
                                "สถิติสูงสุดของคุณ: $speedBest คะแนน • แข่งกับเวลาตอบให้ไวที่สุด!"
                            } else {
                                "แข่งกับเวลา 60 วินาที ตอบคำศัพท์รวมทุกหมวดให้ได้มากที่สุด!"
                            },
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1.15f)
                            .clickable(onClick = onSpeedBlitzClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "เริ่มจับเวลา 60 วิ",
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFBEB),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .weight(0.95f)
                            .clickable(onClick = onMixedClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (mixedBest >= 0) "รวม ($mixedBest/20)" else "รวม 20 ข้อ",
                                color = Color(0xFF92400E),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizTopHeader(
    isStandalone: Boolean,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onLeaderboardClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isStandalone) {
            QuizBouncyIconButton(onClick = onBack, containerColor = Color.White) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "ย้อนกลับ",
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        } else {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE0F2FE),
                border = BorderStroke(1.5.dp, Color(0xFF7DD3FC)),
                modifier = Modifier.size(44.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kids_quiz_3d),
                    contentDescription = "คลังแบบทดสอบ",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "คลังแบบทดสอบ",
                color = Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ฝึกสมอง สะสมดาว และปลดล็อคบทเรียน",
                color = Color(0xFF475569),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Quiz History button
        QuizBouncyIconButton(
            onClick = onHistoryClick,
            containerColor = Color.White,
            borderColor = Color(0xFFCBD5E1)
        ) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = "ประวัติการสอบ",
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Leaderboard Trophy button
        QuizBouncyIconButton(
            onClick = onLeaderboardClick,
            containerColor = Color(0xFFFFF8E1),
            borderColor = Color(0xFFFCD34D)
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = "กระดานผู้นำ",
                tint = Color(0xFFD97706),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun QuizProgressHeroCard(
    stats: QuizHubStats,
    onContinueClick: () -> Unit,
    onMixedChallengeClick: () -> Unit
) {
    val progressRatio = if (stats.totalTopics > 0) {
        stats.passedCount.toFloat() / stats.totalTopics.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "quizHubProgress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF2563EB),
                            Color(0xFF6366F1)
                        )
                    )
                )
        ) {
            // Soft decorative circles inside hero banner
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.09f),
                    radius = 74.dp.toPx(),
                    center = Offset(size.width * 0.92f, size.height * 0.12f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = 48.dp.toPx(),
                    center = Offset(size.width * 0.12f, size.height * 0.95f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ความก้าวหน้าการทดสอบ",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ผ่านแล้ว ${stats.passedCount} จาก ${stats.totalTopics} บทเรียน",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Stars Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${stats.earnedStars}/${stats.maxStars}",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.24f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress.coerceAtLeast(if (stats.passedCount > 0) 0.06f else 0f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFDE047))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Two Quick-Start Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Continue Next Lesson
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1.25f)
                            .clickable(onClick = onContinueClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "บทที่ ${stats.nextTopic.id}: ${stats.nextTopic.title}",
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Mixed Challenge Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFB703),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(0.95f)
                            .clickable(onClick = onMixedChallengeClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (stats.mixedBestScore >= 0) "สุ่มรวม (${stats.mixedBestScore}/20)" else "ท้าทายรวม",
                                color = Color(0xFF1E293B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizFilterRow(
    selectedIndex: Int,
    unlockedCount: Int,
    totalCount: Int,
    onSelect: (Int) -> Unit
) {
    val filters = listOf(
        "ทั้งหมด ($totalCount)" to Color(0xFF0284C7),
        "ภาษาและตัวเลข" to Color(0xFFEF4444),
        "คำศัพท์รอบตัว" to Color(0xFF10B981),
        "พร้อมเล่น ($unlockedCount)" to Color(0xFF7C3AED)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEachIndexed { index, (label, activeColor) ->
            val isSelected = selectedIndex == index
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) activeColor else Color.White.copy(alpha = 0.94f),
                shadowElevation = if (isSelected) 3.dp else 1.dp,
                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.clickable { onSelect(index) }
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFF334155),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizTopicCard(
    topic: QuizTopicEntry,
    animationIndex: Int,
    isUnlocked: Boolean,
    isRecommended: Boolean,
    highScore: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(topic.id) {
        delay((animationIndex * 35L).coerceAtMost(240L))
        visible = true
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quizCardScale"
    )

    val stars = QuizProgressManager.getStarRating(highScore, topic.questionCount)
    val isPassed = highScore >= (topic.questionCount / 2)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + scaleIn(
            initialScale = 0.94f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .alpha(if (isUnlocked) 1f else 0.72f)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (isUnlocked) topic.accentColor.copy(alpha = 0.24f) else Color(0xFFCBD5E1).copy(alpha = 0.5f)
                )
                .padding(bottom = 3.5.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(
                    width = if (isRecommended) 1.8.dp else 1.2.dp,
                    color = when {
                        isRecommended -> topic.accentColor.copy(alpha = 0.7f)
                        isUnlocked -> topic.accentColor.copy(alpha = 0.22f)
                        else -> Color(0xFFE2E8F0)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Image Icon Stage
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isUnlocked) topic.stageBgColor else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = topic.iconRes),
                            contentDescription = topic.title,
                            contentScale = if (topic.isFullBleed) ContentScale.Crop else ContentScale.Fit,
                            modifier = if (topic.isFullBleed) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            }
                        )

                        if (isPassed) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "ผ่านแล้ว",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (!isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center Details
                    Column(modifier = Modifier.weight(1f)) {
                        // Top Pill Row: Lesson # + Status/Stars
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUnlocked) topic.accentColor.copy(alpha = 0.13f) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = topic.badgeLabel ?: "บทที่ ${topic.id}",
                                    color = if (isUnlocked) topic.accentColor else Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            if (isRecommended) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "แนะนำ",
                                            color = Color(0xFFB45309),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (isUnlocked && highScore >= 0) {
                                // Compact 3-star indicator
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(3) { starIdx ->
                                        Icon(
                                            imageVector = if (starIdx < stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                            contentDescription = null,
                                            tint = if (starIdx < stars) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = topic.title,
                            color = if (isUnlocked) Color(0xFF0F172A) else Color(0xFF475569),
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = topic.description,
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Bottom Score / Unlock Hint
                        if (isUnlocked) {
                            if (highScore >= 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFE2E8F0))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth((highScore.toFloat() / topic.questionCount.toFloat()).coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isPassed) Color(0xFF16A34A) else Color(0xFFF59E0B))
                                        )
                                    }
                                    Text(
                                        text = "$highScore/${topic.questionCount}",
                                        color = if (isPassed) Color(0xFF15803D) else Color(0xFFB45309),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "${topic.questionCount} ข้อ • แตะเพื่อเริ่มทดสอบ",
                                    color = topic.accentColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "ผ่านบทที่ ${topic.id - 1} เพื่อปลดล็อค",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Action Badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) topic.accentColor else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.AutoMirrored.Rounded.KeyboardArrowRight else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) Color.White else Color(0xFF64748B),
                            modifier = Modifier.size(if (isUnlocked) 22.dp else 17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizBouncyIconButton(
    onClick: () -> Unit,
    containerColor: Color = Color.White,
    borderColor: Color = Color(0xFFE2E8F0),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "quizIconBtnScale"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun QuizStorybookBackdrop(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "quizStoryBackdrop")

    val cloudDrift1 by infinite.animateFloat(
        initialValue = -0.18f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 38000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "quizCloud1"
    )
    val cloudDrift2 by infinite.animateFloat(
        initialValue = 1.18f,
        targetValue = -0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 48000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "quizCloud2"
    )
    val sunAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "quizSun"
    )
    val twinkle by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quizTwinkle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFDDF3FF),
                    Color(0xFFF0F8FF),
                    Color(0xFFFFFCF3),
                    Color(0xFFECF9EE)
                )
            )
        )

        // Cheerful pastel sun
        val sunCenter = Offset(w * 0.90f, h * 0.08f)
        val sunRadius = 26.dp.toPx()
        drawCircle(
            color = Color(0xFFFFF3B0).copy(alpha = 0.55f),
            radius = sunRadius * 1.5f,
            center = sunCenter
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = sunRadius,
            center = sunCenter
        )
        val rayCount = 10
        for (i in 0 until rayCount) {
            val rad = ((sunAngle + i * (360f / rayCount)) * PI / 180.0)
            drawLine(
                color = Color(0xFFFFCA28).copy(alpha = 0.6f),
                start = Offset(
                    sunCenter.x + (cos(rad) * sunRadius * 1.18f).toFloat(),
                    sunCenter.y + (sin(rad) * sunRadius * 1.18f).toFloat()
                ),
                end = Offset(
                    sunCenter.x + (cos(rad) * sunRadius * 1.58f).toFloat(),
                    sunCenter.y + (sin(rad) * sunRadius * 1.58f).toFloat()
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Soft pastel rainbow arc on upper-left
        val arcCenter = Offset(w * 0.12f, h * 0.22f)
        listOf(
            Color(0xFFFFCDD2),
            Color(0xFFFFE0B2),
            Color(0xFFFFF9C4),
            Color(0xFFC8E6C9),
            Color(0xFFBBDEFB)
        ).forEachIndexed { idx, col ->
            drawCircle(
                color = col.copy(alpha = 0.22f),
                radius = (118 - idx * 8).dp.toPx(),
                center = arcCenter,
                style = Stroke(width = 6.dp.toPx())
            )
        }

        // Drifting clouds
        drawQuizCloud(Offset(w * cloudDrift1, h * 0.11f), 0.92f, 0.80f)
        drawQuizCloud(Offset(w * cloudDrift2, h * 0.26f), 0.72f, 0.68f)

        // Subtle twinkling stars
        drawQuizStar(Offset(w * 0.12f, h * 0.17f), 6.dp.toPx(), Color(0xFFFFB74D).copy(alpha = twinkle * 0.7f))
        drawQuizStar(Offset(w * 0.88f, h * 0.34f), 5.5.dp.toPx(), Color(0xFF38BDF8).copy(alpha = (1.2f - twinkle).coerceIn(0.3f, 0.8f)))
        drawQuizStar(Offset(w * 0.08f, h * 0.56f), 5.dp.toPx(), Color(0xFFF472B6).copy(alpha = twinkle * 0.65f))

        // Gentle meadow hills at bottom
        val backHill = Path().apply {
            moveTo(0f, h * 0.91f)
            quadraticTo(w * 0.30f, h * 0.85f, w * 0.62f, h * 0.91f)
            quadraticTo(w * 0.84f, h * 0.95f, w, h * 0.88f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(backHill, color = Color(0xFFC8E6C9).copy(alpha = 0.65f))

        val frontHill = Path().apply {
            moveTo(0f, h * 0.94f)
            quadraticTo(w * 0.26f, h * 0.98f, w * 0.54f, h * 0.93f)
            quadraticTo(w * 0.80f, h * 0.89f, w, h * 0.94f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(frontHill, color = Color(0xFFA5D6A7).copy(alpha = 0.62f))
    }
}

private fun DrawScope.drawQuizCloud(center: Offset, scale: Float, alpha: Float) {
    val c = Color.White.copy(alpha = alpha)
    val r = 18.dp.toPx() * scale
    drawCircle(color = c, radius = r * 1.15f, center = center)
    drawCircle(color = c, radius = r * 0.88f, center = Offset(center.x - r * 0.98f, center.y + r * 0.2f))
    drawCircle(color = c, radius = r * 0.84f, center = Offset(center.x + r * 1.0f, center.y + r * 0.22f))
}

private fun DrawScope.drawQuizStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.28f, center.y - radius * 0.28f)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + radius * 0.28f, center.y + radius * 0.28f)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.28f, center.y + radius * 0.28f)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - radius * 0.28f, center.y - radius * 0.28f)
        close()
    }
    drawPath(path, color = color)
}

private fun getAllQuizTopics(): List<QuizTopicEntry> {
    return listOf(
        QuizTopicEntry(
            id = 1,
            title = "ภาษาไทย ก-ฮ",
            description = "ทดสอบความรู้พยัญชนะไทย 44 ตัว",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.kids_thai_3d,
            isFullBleed = true,
            accentColor = Color(0xFFEF4444),
            stageBgColor = Color(0xFFFFF5F5)
        ),
        QuizTopicEntry(
            id = 2,
            title = "English A-Z",
            description = "ทดสอบตัวอักษรภาษาอังกฤษ A ถึง Z",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.kids_english_3d,
            isFullBleed = true,
            accentColor = Color(0xFF0284C7),
            stageBgColor = Color(0xFFF0F9FF)
        ),
        QuizTopicEntry(
            id = 3,
            title = "ตัวเลข 0-9",
            description = "ทดสอบการนับเลขและจำตัวเลข",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.home_number,
            accentColor = Color(0xFF10B981),
            stageBgColor = Color(0xFFECFDF5)
        ),
        QuizTopicEntry(
            id = 4,
            title = "English Words",
            description = "คำศัพท์อังกฤษ A for Apple, B for Bird",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.words_a,
            accentColor = Color(0xFFEC4899),
            stageBgColor = Color(0xFFFDF2F8)
        ),
        QuizTopicEntry(
            id = 5,
            title = "ทายคำศัพท์จากภาพ",
            description = "ดูรูปภาพแล้วเลือกคำศัพท์ให้ถูกต้อง",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.ic_svg_alphabet_abc_two,
            accentColor = Color(0xFFF97316),
            stageBgColor = Color(0xFFFFF7ED)
        ),
        QuizTopicEntry(
            id = 6,
            title = "ตัวเลขภาษาอังกฤษ",
            description = "Zero, One, Two... ทดสอบตัวเลขเป็นคำ",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.kids_math_3d,
            isFullBleed = true,
            accentColor = Color(0xFF7C3AED),
            stageBgColor = Color(0xFFF5F3FF)
        ),
        QuizTopicEntry(
            id = 7,
            title = "สัตว์น่ารัก Animals",
            description = "ทายชื่อสัตว์จากรูปภาพ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_animal,
            accentColor = Color(0xFF16A34A),
            stageBgColor = Color(0xFFF0FDF4)
        ),
        QuizTopicEntry(
            id = 8,
            title = "ผลไม้ Fruits",
            description = "ทายชื่อผลไม้ภาษาอังกฤษ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_fruits,
            accentColor = Color(0xFFE11D48),
            stageBgColor = Color(0xFFFFF1F2)
        ),
        QuizTopicEntry(
            id = 9,
            title = "สีสัน Colors",
            description = "ทดสอบชื่อสีภาษาอังกฤษ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_color,
            accentColor = Color(0xFF9333EA),
            stageBgColor = Color(0xFFFAF5FF)
        ),
        QuizTopicEntry(
            id = 10,
            title = "ผัก Vegetables",
            description = "ทายชื่อผักภาษาอังกฤษ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_vegetable,
            accentColor = Color(0xFF059669),
            stageBgColor = Color(0xFFECFDF5)
        ),
        QuizTopicEntry(
            id = 11,
            title = "กีฬา Sports",
            description = "ทายชื่อกีฬาจากรูปภาพ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_sports,
            accentColor = Color(0xFFDB2777),
            stageBgColor = Color(0xFFFDF2F8)
        ),
        QuizTopicEntry(
            id = 12,
            title = "ยานพาหนะ Vehicles",
            description = "ทายชื่อยานพาหนะภาษาอังกฤษ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_vehicle,
            accentColor = Color(0xFFEA580C),
            stageBgColor = Color(0xFFFFF7ED)
        ),
        QuizTopicEntry(
            id = 13,
            title = "รูปทรง Shapes",
            description = "ทายชื่อรูปทรงเรขาคณิตพื้นฐาน",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_shape,
            accentColor = Color(0xFF0EA5E9),
            stageBgColor = Color(0xFFF0F9FF)
        ),
        QuizTopicEntry(
            id = 14,
            title = "อาหาร Foods",
            description = "ทายชื่ออาหารและขนมแสนอร่อย",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_food,
            accentColor = Color(0xFFD97706),
            stageBgColor = Color(0xFFFFFBEB)
        ),
        QuizTopicEntry(
            id = 15,
            title = "ส่วนของร่างกาย",
            description = "ทดสอบคำศัพท์อวัยวะร่างกาย",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_body_parts,
            accentColor = Color(0xFF0D9488),
            stageBgColor = Color(0xFFF0FDFA)
        ),
        QuizTopicEntry(
            id = 16,
            title = "ดอกไม้ Flowers",
            description = "ทายชื่อดอกไม้สวยงาม",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_flower,
            accentColor = Color(0xFFEC4899),
            stageBgColor = Color(0xFFFDF2F8)
        ),
        QuizTopicEntry(
            id = 17,
            title = "นก Birds",
            description = "ทายชื่อนกสายพันธุ์ต่าง ๆ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_birds,
            accentColor = Color(0xFF0284C7),
            stageBgColor = Color(0xFFF0F9FF)
        ),
        QuizTopicEntry(
            id = 18,
            title = "อาชีพ Jobs",
            description = "ทายชื่ออาชีพในฝันของเด็ก ๆ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_jobs,
            accentColor = Color(0xFF6366F1),
            stageBgColor = Color(0xFFEEF2FF)
        )
    )
}

private fun getListeningQuizTopics(): List<QuizTopicEntry> {
    return listOf(
        QuizTopicEntry(
            id = 101,
            title = "ฟังเสียงพยัญชนะ ก-ฮ",
            description = "ฟังเสียงอ่านแล้วเลือกรูปพยัญชนะให้ถูกต้อง",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.kids_thai_3d,
            isFullBleed = true,
            accentColor = Color(0xFF0D9488),
            stageBgColor = Color(0xFFF0FDFA),
            questionCount = 15,
            playMode = QuizPlayMode.LISTENING,
            badgeLabel = "ฟังเสียง"
        ),
        QuizTopicEntry(
            id = 102,
            title = "ฟังเสียงตัวเลข 0-9",
            description = "ฟังเสียงนับเลขไทย/อังกฤษ แล้วเลือกภาพตัวเลข",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.home_number,
            accentColor = Color(0xFF0284C7),
            stageBgColor = Color(0xFFF0F9FF),
            questionCount = 15,
            playMode = QuizPlayMode.LISTENING,
            badgeLabel = "ฟังเสียง"
        ),
        QuizTopicEntry(
            id = 103,
            title = "ฟังเสียงสัตว์โลกน่ารัก",
            description = "ฟังชื่อสัตว์แล้วแตะเลือกรูปสัตว์ให้ตรงกัน",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_animal,
            accentColor = Color(0xFF16A34A),
            stageBgColor = Color(0xFFF0FDF4),
            questionCount = 15,
            playMode = QuizPlayMode.LISTENING,
            badgeLabel = "ฟังเสียง"
        ),
        QuizTopicEntry(
            id = 104,
            title = "ฟังเสียงผลไม้และอาหาร",
            description = "ฝึกฟังคำศัพท์ผลไม้และของอร่อยรอบตัว",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_fruits,
            accentColor = Color(0xFFE11D48),
            stageBgColor = Color(0xFFFFF1F2),
            questionCount = 15,
            playMode = QuizPlayMode.LISTENING,
            badgeLabel = "ฟังเสียง"
        ),
        QuizTopicEntry(
            id = 105,
            title = "ฟังเสียงสีและรูปทรง",
            description = "ฟังชื่อสีและรูปทรงแล้วเลือกรูปที่ตรงกัน",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_color,
            accentColor = Color(0xFF9333EA),
            stageBgColor = Color(0xFFFAF5FF),
            questionCount = 15,
            playMode = QuizPlayMode.LISTENING,
            badgeLabel = "ฟังเสียง"
        ),
        QuizTopicEntry(
            id = 106,
            title = "ฟังเสียงคำศัพท์รวมมิตร",
            description = "สุ่มฟังคำศัพท์หลากหมวด ฝึกหูไวจำแม่น",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.kids_quiz_3d,
            isFullBleed = true,
            accentColor = Color(0xFFEA580C),
            stageBgColor = Color(0xFFFFF7ED),
            questionCount = 20,
            playMode = QuizPlayMode.LISTENING,
            badgeLabel = "รวมมิตร"
        )
    )
}

private fun getMathQuizTopics(): List<QuizTopicEntry> {
    return listOf(
        QuizTopicEntry(
            id = 201,
            title = "นับจำนวนภาพ 1–10",
            description = "ดูภาพน่ารัก ๆ แล้วนับจำนวนให้ถูกต้อง",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.home_number,
            accentColor = Color(0xFF16A34A),
            stageBgColor = Color(0xFFF0FDF4),
            questionCount = 15,
            playMode = QuizPlayMode.MATH,
            badgeLabel = "นับจำนวน"
        ),
        QuizTopicEntry(
            id = 202,
            title = "บวกเลขสนุก (1–10)",
            description = "ฝึกบวกเลขเบื้องต้นพร้อมภาพช่วยนับ",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.kids_math_3d,
            isFullBleed = true,
            accentColor = Color(0xFF0284C7),
            stageBgColor = Color(0xFFF0F9FF),
            questionCount = 15,
            playMode = QuizPlayMode.MATH,
            badgeLabel = "บวกเลข +"
        ),
        QuizTopicEntry(
            id = 203,
            title = "ลบเลขง่าย ๆ (1–10)",
            description = "ฝึกลบเลขเข้าใจง่าย เห็นจำนวนที่ถูกหักออกชัดเจน",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.home_shape,
            accentColor = Color(0xFFEA580C),
            stageBgColor = Color(0xFFFFF7ED),
            questionCount = 15,
            playMode = QuizPlayMode.MATH,
            badgeLabel = "ลบเลข −"
        ),
        QuizTopicEntry(
            id = 204,
            title = "คิดเลขรวมมิตร (+ / −)",
            description = "ท้าทายสมอง สุ่มนับเลข บวก และลบคละกัน",
            group = QuizGroup.LANGUAGE,
            iconRes = R.drawable.home_geometry,
            accentColor = Color(0xFF7C3AED),
            stageBgColor = Color(0xFFF5F3FF),
            questionCount = 15,
            playMode = QuizPlayMode.MATH,
            badgeLabel = "รวมมิตร"
        )
    )
}

private fun getBonusChallengeTopics(): List<QuizTopicEntry> {
    return listOf(
        QuizTopicEntry(
            id = 302,
            title = "เสื้อผ้า Clothes",
            description = "ทายชื่อเครื่องแต่งกายและเสื้อผ้า",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_clothes,
            accentColor = Color(0xFFEC4899),
            stageBgColor = Color(0xFFFDF2F8),
            badgeLabel = "หมวดพิเศษ"
        ),
        QuizTopicEntry(
            id = 303,
            title = "ของใช้ในบ้าน House Items",
            description = "ทายคำศัพท์สิ่งของและเฟอร์นิเจอร์ในบ้าน",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_house,
            accentColor = Color(0xFF0EA5E9),
            stageBgColor = Color(0xFFF0F9FF),
            badgeLabel = "หมวดพิเศษ"
        ),
        QuizTopicEntry(
            id = 304,
            title = "อุปกรณ์การเรียน School Items",
            description = "ทายคำศัพท์สิ่งของในห้องเรียน",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_school,
            accentColor = Color(0xFFF59E0B),
            stageBgColor = Color(0xFFFFFBEB),
            badgeLabel = "หมวดพิเศษ"
        ),
        QuizTopicEntry(
            id = 305,
            title = "ประเทศต่าง ๆ Countries",
            description = "ทายธงชาติและชื่อประเทศรอบโลก",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_country,
            accentColor = Color(0xFF2563EB),
            stageBgColor = Color(0xFFEFF6FF),
            badgeLabel = "หมวดพิเศษ"
        ),
        QuizTopicEntry(
            id = 306,
            title = "เดือนทั้ง 12 Months",
            description = "ทดสอบชื่อเดือนมกราคมถึงธันวาคม",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_month,
            accentColor = Color(0xFF7C3AED),
            stageBgColor = Color(0xFFF5F3FF),
            badgeLabel = "หมวดพิเศษ"
        ),
        QuizTopicEntry(
            id = 307,
            title = "เรขาคณิต Geometry",
            description = "ทายชื่อรูปทรงเรขาคณิต 2 มิติและ 3 มิติ",
            group = QuizGroup.VOCABULARY,
            iconRes = R.drawable.home_geometry,
            accentColor = Color(0xFF0D9488),
            stageBgColor = Color(0xFFF0FDFA),
            badgeLabel = "หมวดพิเศษ"
        )
    )
}
