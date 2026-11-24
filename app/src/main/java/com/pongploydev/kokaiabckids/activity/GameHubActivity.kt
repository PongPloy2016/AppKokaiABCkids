package com.pongploydev.kokaiabckids.activity

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.kokaiabckids.R
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class GameHubActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            GameHubApp(
                onBack = { finish() },
                onSpeak = { text -> speakText(text) }
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("th"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    private fun speakText(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GameHubTTS")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

enum class ActiveGame {
    HUB,
    MEMORY_MATCH,
    SOUND_QUIZ,
    SHADOW_PUZZLE,
    BALLOON_POP,
    ABC_MATCH,
    COLOR_QUIZ,
    ANIMAL_SOUND,
    KIDS_MATH,
    CATEGORY_SORT,
    WORD_BUILDER
}

private fun playLocalSound(context: Context, resId: Int) {
    try {
        val mp = MediaPlayer.create(context, resId)
        mp?.setOnCompletionListener { it.release() }
        mp?.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun GameHubApp(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeGame by remember { mutableStateOf(ActiveGame.HUB) }

    // User Profile state
    val supabaseAuth = remember { SupabaseAuthManager.getInstance(context) }
    val dataManager = remember { SupabaseDataManager.getInstance(context) }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val storedUser = remember { supabaseAuth.getStoredUser() }
    val userId = storedUser?.id ?: firebaseAuth.currentUser?.uid ?: supabaseAuth.getGuestUserId()
    var userName by remember { mutableStateOf(storedUser?.userMetadata?.displayName ?: "คนเก่ง") }
    var userStars by remember { mutableIntStateOf(dataManager.getUserTotalStars()) }
    var userLevel by remember { mutableIntStateOf(dataManager.getUserCurrentLevel()) }

    // High Scores from SharedPreferences
    val prefs = remember { context.getSharedPreferences("game_hub_scores", Context.MODE_PRIVATE) }
    var memoryHighScore by remember { mutableIntStateOf(prefs.getInt("memory_high_score", 0)) }
    var soundHighScore by remember { mutableIntStateOf(prefs.getInt("sound_high_score", 0)) }
    var shadowHighScore by remember { mutableIntStateOf(prefs.getInt("shadow_high_score", 0)) }
    var balloonHighScore by remember { mutableIntStateOf(prefs.getInt("balloon_high_score", 0)) }
    var abcHighScore by remember { mutableIntStateOf(prefs.getInt("abc_high_score", 0)) }
    var colorHighScore by remember { mutableIntStateOf(prefs.getInt("color_high_score", 0)) }
    var animalHighScore by remember { mutableIntStateOf(prefs.getInt("animal_high_score", 0)) }
    var mathHighScore by remember { mutableIntStateOf(prefs.getInt("math_high_score", 0)) }
    var sortHighScore by remember { mutableIntStateOf(prefs.getInt("sort_high_score", 0)) }
    var wordHighScore by remember { mutableIntStateOf(prefs.getInt("word_high_score", 0)) }

    // Fetch latest user profile from Supabase
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            dataManager.fetchUserProfile(userId).onSuccess { profile ->
                userStars = profile.totalStars
                userLevel = profile.currentLevel
                if (!profile.displayName.isNullOrBlank()) {
                    userName = profile.displayName!!
                }
            }
        }
    }

    // Save Game Score to Supabase and Local
    val saveScoreAndReward: (gameType: String, gameTitle: String, score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSpent: Int) -> Unit =
        { gameType, gameTitle, score, earnedStars, totalQ, correctQ, timeSpent ->
            // 1. Update Local High Score
            val key = "${gameType}_high_score"
            val currentBest = prefs.getInt(key, 0)
            if (score > currentBest) {
                prefs.edit().putInt(key, score).apply()
                when (gameType) {
                    "memory" -> memoryHighScore = score
                    "sound" -> soundHighScore = score
                    "shadow" -> shadowHighScore = score
                    "balloon" -> balloonHighScore = score
                    "abc" -> abcHighScore = score
                    "color" -> colorHighScore = score
                    "animal" -> animalHighScore = score
                    "math" -> mathHighScore = score
                    "sort" -> sortHighScore = score
                    "word" -> wordHighScore = score
                }
            }
            val totalGames = prefs.getInt("total_games_played", 0) + 1
            prefs.edit().putInt("total_games_played", totalGames).apply()

            // 2. Play celebratory sound
            playLocalSound(context, R.raw.is_sound_success_sound)

            // 3. Save to Local and Supabase
            scope.launch {
                val (newStars, nextLvl) = dataManager.addStars(userId, earnedStars)
                userStars = newStars
                userLevel = nextLvl

                if (userId.isNotEmpty()) {
                    dataManager.saveQuizResult(
                        userId = userId,
                        topicId = when (gameType) {
                            "memory_match", "memory" -> 101
                            "sound_quiz", "sound" -> 102
                            "shadow_puzzle", "shadow" -> 103
                            "balloon_pop", "balloon" -> 104
                            "abc_match", "abc" -> 105
                            "color_quiz", "color" -> 106
                            "animal_sound", "animal" -> 107
                            "kids_math", "math" -> 108
                            "category_sort", "sort" -> 109
                            "word_builder", "word" -> 110
                            else -> 100
                        },
                        topicName = "เกม: $gameTitle",
                        score = score,
                        totalQuestions = totalQ,
                        timeSpentSeconds = timeSpent
                    )
                    dataManager.saveLearningProgress(userId, "game_hub", gameType, score)
                }
            }
            Toast.makeText(context, "บันทึกคะแนน $score แต้ม! ได้รับ ⭐ +$earnedStars ดาว 🎉", Toast.LENGTH_SHORT).show()
        }

    BackHandler {
        if (activeGame != ActiveGame.HUB) {
            activeGame = ActiveGame.HUB
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F5E9), Color(0xFFE1F5FE), Color(0xFFF3E5F5))
                )
            )
    ) {
        when (activeGame) {
            ActiveGame.HUB -> {
                GameHubMenuScreen(
                    userName = userName,
                    userStars = userStars,
                    userLevel = userLevel,
                    memoryHighScore = memoryHighScore,
                    soundHighScore = soundHighScore,
                    shadowHighScore = shadowHighScore,
                    balloonHighScore = balloonHighScore,
                    abcHighScore = abcHighScore,
                    colorHighScore = colorHighScore,
                    animalHighScore = animalHighScore,
                    mathHighScore = mathHighScore,
                    sortHighScore = sortHighScore,
                    wordHighScore = wordHighScore,
                    onBack = onBack,
                    onSelectGame = { activeGame = it }
                )
            }
            ActiveGame.MEMORY_MATCH -> {
                MemoryMatchGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onComplete = { score, stars, timeSec ->
                        saveScoreAndReward("memory", "จับคู่การ์ดความจำ", score, stars, 6, 6, timeSec)
                    }
                )
            }
            ActiveGame.SOUND_QUIZ -> {
                SoundQuizGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onSpeak = onSpeak,
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("sound", "ฟังเสียงทายตัวอักษร", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.SHADOW_PUZZLE -> {
                ShadowPuzzleGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("shadow", "ทายภาพเงาปริศนา", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.BALLOON_POP -> {
                BalloonPopGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("balloon", "จิ้มลูกโป่งนับจำนวน", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.ABC_MATCH -> {
                AbcMonsterMatchGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onSpeak = onSpeak,
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("abc", "จับคู่ตัวอักษร A-Z", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.COLOR_QUIZ -> {
                ColorQuizGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onSpeak = onSpeak,
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("color", "ทายสีสันสดใส", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.ANIMAL_SOUND -> {
                AnimalSoundGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onSpeak = onSpeak,
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("animal", "ฟังเสียงสัตว์ร้องทายชื่อ", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.KIDS_MATH -> {
                KidsMathGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("math", "บวกลบเลขคิดเร็ว", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.CATEGORY_SORT -> {
                CategorySortGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("sort", "แยกหมวดหมู่แสนสนุก", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
            ActiveGame.WORD_BUILDER -> {
                WordBuilderGameScreen(
                    onExit = { activeGame = ActiveGame.HUB },
                    onSpeak = onSpeak,
                    onComplete = { score, stars, totalQ, correctQ, timeSec ->
                        saveScoreAndReward("word", "เรียงคำศัพท์แสนสนุก", score, stars, totalQ, correctQ, timeSec)
                    }
                )
            }
        }
    }
}

// ==================== 1. HUB MENU SCREEN ====================

@Composable
fun GameHubMenuScreen(
    userName: String,
    userStars: Int,
    userLevel: Int,
    memoryHighScore: Int,
    soundHighScore: Int,
    shadowHighScore: Int,
    balloonHighScore: Int,
    abcHighScore: Int,
    colorHighScore: Int,
    animalHighScore: Int,
    mathHighScore: Int,
    sortHighScore: Int,
    wordHighScore: Int,
    onBack: () -> Unit,
    onSelectGame: (ActiveGame) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF6584), Color(0xFFFF9E6D), Color(0xFFFFC107))
                        )
                    )
                    .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.clickable { onBack() }
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "กลับ",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Star badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⭐ $userStars", color = Color(0xFFFFEB3B), fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("เลเวล $userLevel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🎮 ศูนย์รวมเกมแสนสนุก",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "เล่นเกมฝึกสมอง สะสมคะแนนและดาวทอง ⭐ ($userName)",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game 1: Memory Match
                GameMenuCard(
                    title = "🃏 เกมจับคู่การ์ดความจำ",
                    subtitle = "เปิดแผ่นป้ายจับคู่ตัวอักษร ก-ฮ ยิ่งเร็วยิ่งได้ดาวเยอะ!",
                    tag = "ฝึกความจำและสมาธิ",
                    starsReward = "+15 ⭐",
                    highScore = memoryHighScore,
                    gradient = listOf(Color(0xFFFF7043), Color(0xFFFF5252)),
                    iconRes = R.drawable.home_alphabet,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.MEMORY_MATCH)
                    }
                )

                // Game 2: Sound Quiz
                GameMenuCard(
                    title = "🔊 เกมฟังเสียงทายตัวอักษร",
                    subtitle = "ฟังเสียงอ่านแล้วเลือกการ์ดที่ถูกต้อง มีทั้งหมด 5 ข้อ",
                    tag = "ฝึกการฟังและจดจำ",
                    starsReward = "+20 ⭐",
                    highScore = soundHighScore,
                    gradient = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5)),
                    iconRes = R.drawable.ic_auto_read,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.SOUND_QUIZ)
                    }
                )

                // Game 3: Shadow Puzzle
                GameMenuCard(
                    title = "👥 เกมทายภาพเงาปริศนา",
                    subtitle = "ดูเงาสีดำปริศนา แล้วทายว่าเป็นรูปของสิ่งใด!",
                    tag = "ฝึกทักษะการสังเกต",
                    starsReward = "+20 ⭐",
                    highScore = shadowHighScore,
                    gradient = listOf(Color(0xFFAB47BC), Color(0xFF7E57C2)),
                    iconRes = R.drawable.home_animal,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.SHADOW_PUZZLE)
                    }
                )

                // Game 4: Balloon Pop
                GameMenuCard(
                    title = "🎈 เกมจิ้มลูกโป่งนับจำนวน",
                    subtitle = "นับจำนวนผลไม้หรือสิ่งของ แล้วจิ้มลูกโป่งเลขที่ถูกต้อง!",
                    tag = "ฝึกนับเลขและไหวพริบ",
                    starsReward = "+20 ⭐",
                    highScore = balloonHighScore,
                    gradient = listOf(Color(0xFF00B4D8), Color(0xFF0077B6)),
                    iconRes = R.drawable.kids_math_3d,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.BALLOON_POP)
                    }
                )

                // Game 5: ABC Monster Match
                GameMenuCard(
                    title = "🔤 เกมจับคู่ตัวอักษร A-Z",
                    subtitle = "หาตัวอักษรมอนสเตอร์ภาษาอังกฤษที่ตรงกับโจทย์ สนุกและได้ความรู้!",
                    tag = "ฝึกภาษาอังกฤษ A-Z",
                    starsReward = "+20 ⭐",
                    highScore = abcHighScore,
                    gradient = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
                    iconRes = R.drawable.words_a,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.ABC_MATCH)
                    }
                )

                // Game 6: Color Quiz
                GameMenuCard(
                    title = "🎨 เกมทายสีสันสดใส",
                    subtitle = "ดูชื่อสีและเสียงคำอ่าน แล้วแตะเลือกจานสีที่ถูกต้อง!",
                    tag = "เรียนรู้เรื่องสีสัน",
                    starsReward = "+20 ⭐",
                    highScore = colorHighScore,
                    gradient = listOf(Color(0xFFE91E63), Color(0xFFFF4081)),
                    iconRes = R.drawable.home_color,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.COLOR_QUIZ)
                    }
                )

                // Game 7: Animal Sound & Quiz
                GameMenuCard(
                    title = "🦁 เกมทายเสียงและชื่อสัตว์",
                    subtitle = "ฟังเสียงร้องและดูคำใบ้ แล้วเลือกเพื่อนสัตว์แสนน่ารัก!",
                    tag = "ฝึกฟังและรู้จักสัตว์",
                    starsReward = "+20 ⭐",
                    highScore = animalHighScore,
                    gradient = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)),
                    iconRes = R.drawable.home_animal,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.ANIMAL_SOUND)
                    }
                )

                // Game 8: Kids Math
                GameMenuCard(
                    title = "➕ เกมนับเลขและบวกเลขเร็ว",
                    subtitle = "นับผลไม้และคิดเลขบวกลบง่ายๆ สำหรับวัยอนุบาลและปฐมวัย!",
                    tag = "พื้นฐานคณิตศาสตร์",
                    starsReward = "+20 ⭐",
                    highScore = mathHighScore,
                    gradient = listOf(Color(0xFF00BCD4), Color(0xFF00838F)),
                    iconRes = R.drawable.kids_math_3d,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.KIDS_MATH)
                    }
                )

                // Game 9: Category Sort
                GameMenuCard(
                    title = "🍉 เกมแยกหมวดหมู่แสนสนุก",
                    subtitle = "ผลไม้ สัตว์ ยานพาหนะ หรือผัก? จัดกลุ่มให้ถูกต้อง!",
                    tag = "ฝึกคิดวิเคราะห์และจัดหมวดหมู่",
                    starsReward = "+20 ⭐",
                    highScore = sortHighScore,
                    gradient = listOf(Color(0xFFFF7043), Color(0xFFE64A19)),
                    iconRes = R.drawable.home_fruits,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.CATEGORY_SORT)
                    }
                )

                // Game 10: Word Builder
                GameMenuCard(
                    title = "🧩 เกมเรียงคำศัพท์สะกดคำ",
                    subtitle = "เรียงบล็อกพยัญชนะและสระให้เป็นคำศัพท์ภาษาไทยที่ถูกต้อง!",
                    tag = "ฝึกสะกดคำภาษาไทย",
                    starsReward = "+20 ⭐",
                    highScore = wordHighScore,
                    gradient = listOf(Color(0xFF9C27B0), Color(0xFF6A1B9A)),
                    iconRes = R.drawable.words_a,
                    onClick = {
                        playLocalSound(context, R.raw.is_sound_buttonswitch)
                        onSelectGame(ActiveGame.WORD_BUILDER)
                    }
                )
            }
        }
    }
}

@Composable
fun GameMenuCard(
    title: String,
    subtitle: String,
    tag: String,
    starsReward: String,
    highScore: Int,
    gradient: List<Color>,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradient))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = tag,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD54F)
                ) {
                    Text(
                        text = starsReward,
                        color = Color(0xFF5D4037),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (highScore > 0) "🏆 สถิติสูงสุด: $highScore แต้ม" else "🌟 ยังไม่มีสถิติ (เริ่มเล่นเลย!)",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "เล่นเลย ▶",
                        color = gradient.first(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

// ==================== 2. GAME 1: MEMORY MATCH ====================

data class MemoryCard(
    val id: Int,
    val thaiName: String,
    val drawableRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryMatchGameScreen(
    onExit: () -> Unit,
    onComplete: (score: Int, earnedStars: Int, timeSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var timeSeconds by remember { mutableIntStateOf(0) }
    var moves by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    // Thai letters data pool (6 pairs = 12 cards)
    val pool = remember {
        listOf(
            Pair("ก ไก่", R.drawable.thai_card_1),
            Pair("ข ไข่", R.drawable.thai_card_2),
            Pair("ค ควาย", R.drawable.thai_card_3),
            Pair("ง งู", R.drawable.thai_card_4),
            Pair("จ จาน", R.drawable.thai_card_5),
            Pair("ช ช้าง", R.drawable.thai_card_6),
            Pair("ด เด็ก", R.drawable.thai_card_7),
            Pair("ต เต่า", R.drawable.thai_card_8),
            Pair("ม ม้า", R.drawable.thai_card_9),
            Pair("ย ยักษ์", R.drawable.thai_card_10)
        )
    }

    var cards by remember {
        mutableStateOf(generateMemoryCards(pool))
    }

    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var secondSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                delay(1000)
                timeSeconds++
            }
        }
    }

    // Check Match
    LaunchedEffect(secondSelectedIndex) {
        if (firstSelectedIndex != null && secondSelectedIndex != null) {
            isProcessing = true
            val first = cards[firstSelectedIndex!!]
            val second = cards[secondSelectedIndex!!]
            moves++

            if (first.thaiName == second.thaiName) {
                // Matched!
                playLocalSound(context, R.raw.chiotrue)
                cards = cards.mapIndexed { idx, card ->
                    if (idx == firstSelectedIndex || idx == secondSelectedIndex) {
                        card.copy(isMatched = true, isFlipped = true)
                    } else card
                }
                firstSelectedIndex = null
                secondSelectedIndex = null
                isProcessing = false

                // Check Win
                if (cards.all { it.isMatched }) {
                    isGameOver = true
                    val timeBonus = maxOf(0, 100 - timeSeconds)
                    val movesPenalty = maxOf(0, (moves - 6) * 5)
                    val finalScore = maxOf(30, 120 + timeBonus - movesPenalty)
                    val earnedStars = 15
                    onComplete(finalScore, earnedStars, timeSeconds)
                }
            } else {
                // Not Matched
                playLocalSound(context, R.raw.chiofalse)
                delay(900)
                cards = cards.mapIndexed { idx, card ->
                    if (idx == firstSelectedIndex || idx == secondSelectedIndex) {
                        card.copy(isFlipped = false)
                    } else card
                }
                firstSelectedIndex = null
                secondSelectedIndex = null
                isProcessing = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Game Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "ออก", tint = Color(0xFF37474F))
            }
            Text("🃏 จับคู่การ์ดความจำ", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFE082)) {
                Text(
                    "⏱️ $timeSeconds วิ",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D4037)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards Grid (3 columns x 4 rows)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                val rotation by animateFloatAsState(
                    targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
                    animationSpec = tween(400),
                    label = "flip"
                )

                Card(
                    modifier = Modifier
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !card.isMatched && !card.isFlipped && !isProcessing) {
                            playLocalSound(context, R.raw.is_sound_buttonswitch)
                            cards = cards.mapIndexed { i, c ->
                                if (i == index) c.copy(isFlipped = true) else c
                            }
                            if (firstSelectedIndex == null) {
                                firstSelectedIndex = index
                            } else {
                                secondSelectedIndex = index
                            }
                        }
                        .graphicsLayer { rotationY = rotation },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (card.isMatched) Color(0xFFC8E6C9)
                        else if (card.isFlipped) Color.White
                        else Color(0xFF42A5F5)
                    ),
                    border = BorderStroke(
                        3.dp,
                        if (card.isMatched) Color(0xFF4CAF50)
                        else if (card.isFlipped) Color(0xFFFFCA28)
                        else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (rotation > 90f) {
                            // Front of card (Icon & Name)
                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .graphicsLayer { rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = card.drawableRes),
                                    contentDescription = card.thaiName,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.thaiName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0277BD)
                                )
                            }
                        } else {
                            // Back of card
                            Text("❓", fontSize = 28.sp)
                        }
                    }
                }
            }
        }

        // Win Dialog
        if (isGameOver) {
            GameWinDialog(
                title = "🎉 ชนะแล้ว! เก่งมาก",
                score = maxOf(30, 120 + maxOf(0, 100 - timeSeconds) - (moves - 6) * 5),
                earnedStars = 15,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    cards = generateMemoryCards(pool)
                    timeSeconds = 0
                    moves = 0
                    firstSelectedIndex = null
                    secondSelectedIndex = null
                    isProcessing = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

private fun generateMemoryCards(pool: List<Pair<String, Int>>): List<MemoryCard> {
    val selected = pool.shuffled().take(6)
    val doubleList = selected + selected
    return doubleList.shuffled().mapIndexed { index, pair ->
        MemoryCard(id = index, thaiName = pair.first, drawableRes = pair.second)
    }
}

// ==================== 3. GAME 2: SOUND QUIZ ====================

data class SoundQuestion(
    val thaiLetter: String,
    val soundResId: Int?,
    val correctCardRes: Int,
    val options: List<Pair<String, Int>> // (name, resId)
)

@Composable
fun SoundQuizGameScreen(
    onExit: () -> Unit,
    onSpeak: (String) -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var currentQIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    // 5 Questions Pool
    val allLetters = remember {
        listOf(
            Triple("ก ไก่", R.drawable.thai_card_1, R.raw.soundk1),
            Triple("ข ไข่", R.drawable.thai_card_2, R.raw.soundk2),
            Triple("ค ควาย", R.drawable.thai_card_3, R.raw.soundk3),
            Triple("ง งู", R.drawable.thai_card_4, R.raw.soundk4),
            Triple("จ จาน", R.drawable.thai_card_5, R.raw.soundk5),
            Triple("ช ช้าง", R.drawable.thai_card_6, R.raw.soundk6),
            Triple("ด เด็ก", R.drawable.thai_card_7, R.raw.soundk7),
            Triple("ต เต่า", R.drawable.thai_card_8, R.raw.soundk8),
            Triple("บ ใบไม้", R.drawable.thai_card_9, R.raw.soundk9),
            Triple("ป ปลา", R.drawable.thai_card_10, R.raw.soundk10)
        )
    }

    val questions = remember {
        val picked = allLetters.shuffled().take(5)
        picked.map { target ->
            val wrongOptions = allLetters.filter { it.first != target.first }.shuffled().take(3)
            val all4 = (wrongOptions + target).shuffled().map { Pair(it.first, it.second) }
            SoundQuestion(
                thaiLetter = target.first,
                soundResId = target.third,
                correctCardRes = target.second,
                options = all4
            )
        }
    }

    val currentQ = questions.getOrNull(currentQIndex)

    // Play Sound on question start
    LaunchedEffect(currentQIndex) {
        if (currentQ != null) {
            delay(300)
            if (currentQ.soundResId != null) {
                playLocalSound(context, currentQ.soundResId)
            } else {
                onSpeak(currentQ.thaiLetter)
            }
        }
    }

    // Timer
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                delay(1000)
                timeSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "ออก", tint = Color(0xFF37474F))
            }
            Text("🔊 ฟังเสียงทายตัวอักษร", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF81D4FA)) {
                Text(
                    "ข้อที่ ${currentQIndex + 1}/5",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF01579B)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Sound Button
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF29B6F6)),
            border = BorderStroke(6.dp, Color.White),
            modifier = Modifier
                .size(110.dp)
                .clickable {
                    if (currentQ != null) {
                        if (currentQ.soundResId != null) {
                            playLocalSound(context, currentQ.soundResId)
                        } else {
                            onSpeak(currentQ.thaiLetter)
                        }
                    }
                }
                .shadow(10.dp, CircleShape)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "ฟังเสียงอีกครั้ง",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("แตะที่ลำโพงเพื่อฟังเสียงซ้ำ 🔊", fontSize = 13.sp, color = Color(0xFF546E7A), fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // Options (4 Cards, 2x2 grid)
        if (currentQ != null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(currentQ.options) { _, option ->
                    val isSelected = selectedOption == option.first
                    val isCorrect = option.first == currentQ.thaiLetter

                    val cardBg = if (isAnswered) {
                        if (isCorrect) Color(0xFFC8E6C9)
                        else if (isSelected) Color(0xFFFFCDD2)
                        else Color.White
                    } else Color.White

                    val borderColor = if (isAnswered) {
                        if (isCorrect) Color(0xFF4CAF50)
                        else if (isSelected) Color(0xFFE53935)
                        else Color(0xFFB0BEC5)
                    } else Color(0xFF90CAF9)

                    Card(
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = !isAnswered) {
                                selectedOption = option.first
                                isAnswered = true
                                if (isCorrect) {
                                    playLocalSound(context, R.raw.chiotrue)
                                    score += 20
                                    correctCount++
                                } else {
                                    playLocalSound(context, R.raw.chiofalse)
                                }
                            }
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(4.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = option.second),
                                contentDescription = option.first,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = option.first,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF37474F)
                            )
                        }
                    }
                }
            }

            // Next button if answered
            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedOption = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, 5, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 เล่นจบแล้ว! เก่งมาก",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedOption = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 4. GAME 3: SHADOW PUZZLE ====================

data class ShadowQuestion(
    val title: String,
    val imageRes: Int,
    val options: List<Pair<String, Int>> // (name, res)
)

@Composable
fun ShadowPuzzleGameScreen(
    onExit: () -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var currentQIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    // Objects / Animals for Shadow Puzzle
    val shadowPool = remember {
        listOf(
            Pair("แอปเปิ้ล (A)", R.drawable.words_a),
            Pair("นก (Birds)", R.drawable.home_birds),
            Pair("สัตว์ต่างๆ (Animal)", R.drawable.home_animal),
            Pair("ผลไม้ (Fruits)", R.drawable.home_fruits),
            Pair("ผักสด (Vegetable)", R.drawable.home_vegetable),
            Pair("ยานพาหนะ (Vehicle)", R.drawable.home_vehicle),
            Pair("ช้าง (ช)", R.drawable.thai_card_6),
            Pair("ม้า (ม)", R.drawable.thai_card_9),
            Pair("ปลา (ป)", R.drawable.thai_card_10)
        )
    }

    val questions = remember {
        val picked = shadowPool.shuffled().take(5)
        picked.map { target ->
            val wrong = shadowPool.filter { it.first != target.first }.shuffled().take(3)
            val all4 = (wrong + target).shuffled()
            ShadowQuestion(
                title = target.first,
                imageRes = target.second,
                options = all4
            )
        }
    }

    val currentQ = questions.getOrNull(currentQIndex)

    // Timer
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                delay(1000)
                timeSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "ออก", tint = Color(0xFF37474F))
            }
            Text("👥 ทายภาพเงาปริศนา", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFD1C4E9)) {
                Text(
                    "ข้อที่ ${currentQIndex + 1}/5",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4527A0)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Silhouette Mystery Card
        if (currentQ != null) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(4.dp, Color(0xFFBA68C8)),
                modifier = Modifier
                    .size(160.dp)
                    .shadow(10.dp, RoundedCornerShape(28.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = currentQ.imageRes),
                        contentDescription = "เงาปริศนา",
                        modifier = Modifier.size(110.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (!isAnswered) ColorFilter.tint(Color.Black) else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (!isAnswered) "ทายซิว่า... เงานี้คือภาพอะไรเอ่ย? 🤔" else "เฉลย: ${currentQ.title} ✨",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isAnswered) Color(0xFF4A148C) else Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(currentQ.options) { _, option ->
                    val isSelected = selectedOption == option.first
                    val isCorrect = option.first == currentQ.title

                    val cardBg = if (isAnswered) {
                        if (isCorrect) Color(0xFFC8E6C9)
                        else if (isSelected) Color(0xFFFFCDD2)
                        else Color.White
                    } else Color.White

                    val borderColor = if (isAnswered) {
                        if (isCorrect) Color(0xFF4CAF50)
                        else if (isSelected) Color(0xFFE53935)
                        else Color(0xFFB0BEC5)
                    } else Color(0xFFCE93D8)

                    Card(
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = !isAnswered) {
                                selectedOption = option.first
                                isAnswered = true
                                if (isCorrect) {
                                    playLocalSound(context, R.raw.chiotrue)
                                    score += 20
                                    correctCount++
                                } else {
                                    playLocalSound(context, R.raw.chiofalse)
                                }
                            }
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(4.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = option.second),
                                contentDescription = option.first,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = option.first,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedOption = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, 5, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 ทายเงาครบแล้ว! ยอดเยี่ยม",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedOption = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 5. WIN / RESULT DIALOG ====================

@Composable
fun GameWinDialog(
    title: String,
    score: Int,
    earnedStars: Int,
    timeSeconds: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0288D1),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF9C4),
                    border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆 คะแนนของคุณ: $score แต้ม",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⭐ ได้รับดาวสะสม: +$earnedStars ดวง",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "⏱️ เวลาที่ใช้: $timeSeconds วินาที",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color(0xFFB0BEC5))
                    ) {
                        Text("กลับหน้าเกม", color = Color(0xFF455A64), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("เล่นอีกครั้ง 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

// ==================== 5. GAME 4: BALLOON POP COUNTING ====================

data class BalloonQuestion(
    val prompt: String,
    val displayEmojis: String,
    val correctAnswer: Int,
    val options: List<Int>
)

@Composable
fun BalloonPopGameScreen(
    onExit: () -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var currentQIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val balloonQuestions = remember {
        val pool = listOf(
            BalloonQuestion("นับผลแอปเปิ้ลดูสิ มีกี่ผลเอ่ย?", "🍎  🍎  🍎", 3, listOf(2, 3, 4, 5).shuffled()),
            BalloonQuestion("นับเจ้าเหมียวดูสิ มีกี่ตัวนะ?", "🐱  🐱", 2, listOf(1, 2, 3, 4).shuffled()),
            BalloonQuestion("นับดาวส่องแสง มีกี่ดวงเอ่ย?", "⭐  ⭐  ⭐  ⭐  ⭐", 5, listOf(3, 4, 5, 6).shuffled()),
            BalloonQuestion("นับสตรอว์เบอร์รีแสนอร่อย?", "🍓  🍓  🍓  🍓", 4, listOf(2, 3, 4, 5).shuffled()),
            BalloonQuestion("นับรถยนต์คันเก่ง มีกี่คันนะ?", "🚗", 1, listOf(1, 2, 3, 4).shuffled()),
            BalloonQuestion("ลูกโป่งหลากสี มีกี่ลูกเอ่ย?", "🎈  🎈  🎈  🎈  🎈  🎈", 6, listOf(4, 5, 6, 7).shuffled()),
            BalloonQuestion("น้องเป็ดก้าบๆ มีกี่ตัวนะ?", "🐥  🐥  🐥  🐥", 4, listOf(3, 4, 5, 6).shuffled()),
            BalloonQuestion("ส้มหวานฉ่ำ มีกี่ผลเอ่ย?", "🍊  🍊  🍊  🍊  🍊", 5, listOf(4, 5, 6, 7).shuffled())
        )
        pool.shuffled().take(5)
    }

    val currentQ = balloonQuestions.getOrNull(currentQIndex)

    val balloonColors = remember {
        listOf(
            Color(0xFFFF5252), // Coral Red
            Color(0xFF00B4D8), // Sky Blue
            Color(0xFF4CAF50), // Lime Green
            Color(0xFFFFB300)  // Golden Amber
        )
    }

    // Floating animation for balloons
    val infiniteTransition = rememberInfiniteTransition(label = "balloonFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloonY"
    )

    // Timer
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                delay(1000)
                timeSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "ออก", tint = Color(0xFF37474F))
            }
            Text("🎈 จิ้มลูกโป่งนับจำนวน", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFB2EBF2)) {
                Text(
                    "ข้อที่ ${currentQIndex + 1}/5",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006064)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentQ != null) {
            // Counting Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(3.dp, Color(0xFF4DD0E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentQ.prompt,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00838F),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE0F7FA),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = currentQ.displayEmojis,
                            fontSize = 30.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (!isAnswered) "จิ้มลูกโป่งที่มีตัวเลขถูกต้องเลย! 🎈"
                        else if (selectedAnswer == currentQ.correctAnswer) "🎉 ป๊อป! ถูกต้องนะจ๊ะ เก่งมาก!"
                        else "คำตอบที่ถูกต้องคือ ${currentQ.correctAnswer} จ้า 🎈",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isAnswered) Color(0xFF546E7A)
                        else if (selectedAnswer == currentQ.correctAnswer) Color(0xFF2E7D32)
                        else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Balloons Grid (2x2)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(currentQ.options) { index, optionNum ->
                    val isSelected = selectedAnswer == optionNum
                    val isCorrect = optionNum == currentQ.correctAnswer
                    val balloonColor = balloonColors[index % balloonColors.size]
                    val bobbing = if (index % 2 == 0) floatOffset else -floatOffset

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationY = bobbing },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Balloon body
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 48.dp,
                                topEnd = 48.dp,
                                bottomStart = 44.dp,
                                bottomEnd = 44.dp
                            ),
                            color = if (isAnswered) {
                                if (isCorrect) Color(0xFF4CAF50)
                                else if (isSelected) Color(0xFFE53935)
                                else balloonColor.copy(alpha = 0.4f)
                            } else balloonColor,
                            shadowElevation = 8.dp,
                            border = BorderStroke(
                                3.dp,
                                if (isAnswered && isCorrect) Color(0xFFFFD54F) else Color.White
                            ),
                            modifier = Modifier
                                .size(width = 110.dp, height = 120.dp)
                                .clickable(enabled = !isAnswered) {
                                    selectedAnswer = optionNum
                                    isAnswered = true
                                    if (isCorrect) {
                                        playLocalSound(context, R.raw.chiotrue)
                                        score += 20
                                        correctCount++
                                    } else {
                                        playLocalSound(context, R.raw.chiofalse)
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                // Glossy highlight
                                Box(
                                    modifier = Modifier
                                        .size(24.dp, 12.dp)
                                        .align(Alignment.TopStart)
                                        .padding(start = 12.dp, top = 8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.5f))
                                )

                                Text(
                                    text = "$optionNum",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        // Knot & String
                        Text("🧵", fontSize = 14.sp)
                    }
                }
            }

            // Next Question Button
            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < balloonQuestions.size - 1) {
                            currentQIndex++
                            selectedAnswer = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, 5, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))
                ) {
                    Text(
                        if (currentQIndex < balloonQuestions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 จิ้มลูกโป่งครบแล้ว! เยี่ยมมาก",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedAnswer = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 6. GAME 5: MONSTER ABC MATCH ====================

data class MonsterAbcQuestion(
    val letter: String,
    val word: String,
    val correctRes: Int,
    val options: List<Triple<String, String, Int>>
)

@Composable
fun AbcMonsterMatchGameScreen(
    onExit: () -> Unit,
    onSpeak: (String) -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSeconds: Int) -> Unit
) {
    val context = LocalContext.current
    var currentQIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    var selectedLetter by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val abcPool = remember {
        listOf(
            Triple("A", "Apple", R.drawable.words_a),
            Triple("B", "Bear", R.drawable.words_b),
            Triple("C", "Cat", R.drawable.words_c),
            Triple("D", "Dog", R.drawable.words_d),
            Triple("E", "Elephant", R.drawable.words_e),
            Triple("F", "Fish", R.drawable.words_f),
            Triple("G", "Giraffe", R.drawable.words_g),
            Triple("H", "Horse", R.drawable.words_h),
            Triple("M", "Monkey", R.drawable.words_m),
            Triple("P", "Panda", R.drawable.words_p),
            Triple("S", "Sun", R.drawable.words_s),
            Triple("Z", "Zebra", R.drawable.words_z)
        )
    }

    val questions = remember {
        val picked = abcPool.shuffled().take(5)
        picked.map { target ->
            val wrong = abcPool.filter { it.first != target.first }.shuffled().take(3)
            val all4 = (wrong + target).shuffled()
            MonsterAbcQuestion(
                letter = target.first,
                word = target.second,
                correctRes = target.third,
                options = all4
            )
        }
    }

    val currentQ = questions.getOrNull(currentQIndex)

    LaunchedEffect(currentQIndex) {
        if (currentQ != null) {
            delay(300)
            onSpeak("Letter ${currentQ.letter}")
        }
    }

    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                delay(1000)
                timeSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "ออก", tint = Color(0xFF37474F))
            }
            Text("🔤 จับคู่ตัวอักษร A-Z", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF37474F))
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFE082)) {
                Text(
                    "ข้อที่ ${currentQIndex + 1}/5",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentQ != null) {
            // Target Letter Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(3.dp, Color(0xFFFFB74D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = currentQ.letter,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "หาการ์ดมอนสเตอร์ '${currentQ.letter}'",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "${currentQ.letter} for ${currentQ.word}",
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { onSpeak("Letter ${currentQ.letter}, for ${currentQ.word}") },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3E0))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "ฟังเสียง",
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Monster Cards (2x2 grid)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(currentQ.options) { _, option ->
                    val isSelected = selectedLetter == option.first
                    val isCorrect = option.first == currentQ.letter

                    val cardBg = if (isAnswered) {
                        if (isCorrect) Color(0xFFC8E6C9)
                        else if (isSelected) Color(0xFFFFCDD2)
                        else Color.White
                    } else Color.White

                    val borderColor = if (isAnswered) {
                        if (isCorrect) Color(0xFF4CAF50)
                        else if (isSelected) Color(0xFFE53935)
                        else Color(0xFFE0E0E0)
                    } else Color(0xFFFFCC80)

                    Card(
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = !isAnswered) {
                                selectedLetter = option.first
                                isAnswered = true
                                if (isCorrect) {
                                    playLocalSound(context, R.raw.chiotrue)
                                    score += 20
                                    correctCount++
                                } else {
                                    playLocalSound(context, R.raw.chiofalse)
                                }
                            }
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(4.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = option.third),
                                contentDescription = option.first,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${option.first} (${option.second})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424242),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Next button
            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedLetter = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, 5, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 ตอบครบทุกข้อแล้ว! เก่งมากๆ",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedLetter = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== SHARED GAME TOP BAR ====================

@Composable
fun GameScreenTopBar(
    title: String,
    titleColor: Color,
    score: Int,
    badgeBg: Color,
    badgeBorder: Color,
    badgeTextColor: Color,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.clickable { onExit() }
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "กลับ",
                    tint = titleColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = titleColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = badgeBg,
            border = BorderStroke(2.dp, badgeBorder),
            shadowElevation = 2.dp
        ) {
            Text(
                text = "⭐ $score แต้ม",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ==================== 7. COLOR QUIZ GAME ====================

data class ColorQuizItem(
    val nameTh: String,
    val nameEn: String,
    val color: Color,
    val emoji: String
)

@Composable
fun ColorQuizGameScreen(
    onExit: () -> Unit,
    onSpeak: (String) -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSec: Int) -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<ColorQuizItem?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val allColors = remember {
        listOf(
            ColorQuizItem("สีแดง", "Red", Color(0xFFE53935), "🍎"),
            ColorQuizItem("สีน้ำเงิน", "Blue", Color(0xFF1E88E5), "🌊"),
            ColorQuizItem("สีเหลือง", "Yellow", Color(0xFFFDD835), "🍌"),
            ColorQuizItem("สีเขียว", "Green", Color(0xFF43A047), "🐸"),
            ColorQuizItem("สีส้ม", "Orange", Color(0xFFFB8C00), "🍊"),
            ColorQuizItem("สีชมพู", "Pink", Color(0xFFE91E63), "🌸"),
            ColorQuizItem("สีม่วง", "Purple", Color(0xFF8E24AA), "🍇"),
            ColorQuizItem("สีฟ้า", "Sky Blue", Color(0xFF00ACC1), "🐬")
        )
    }

    val questions = remember {
        allColors.shuffled().take(5).map { target ->
            val otherOptions = allColors.filter { it.nameTh != target.nameTh }.shuffled().take(3)
            val options = (otherOptions + target).shuffled()
            Pair(target, options)
        }
    }

    val currentTarget = questions[currentQIndex].first
    val currentOptions = questions[currentQIndex].second

    LaunchedEffect(currentQIndex) {
        onSpeak(currentTarget.nameTh)
    }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(1000L)
            timeSeconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFFFFFFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            // Top Bar
            GameScreenTopBar(
                title = "🎨 ทายสีสันสดใส",
                titleColor = Color(0xFFC2185B),
                score = score,
                badgeBg = Color(0xFFFFF0F5),
                badgeBorder = Color(0xFFF06292),
                badgeTextColor = Color(0xFFC2185B),
                onExit = onExit
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ข้อที่ ${currentQIndex + 1} จาก ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF880E4F)
                )
                Text(
                    text = "⏱ $timeSeconds วินาที",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentQIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFE91E63),
                trackColor = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "สีไหนคือสี...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currentTarget.nameTh} (${currentTarget.nameEn}) ${currentTarget.emoji}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFC2185B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFCE4EC),
                        modifier = Modifier.clickable { onSpeak(currentTarget.nameTh) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "ฟังเสียง",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "กดฟังเสียงคำอ่าน",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E63)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Color Palettes (2x2 Grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0..1) {
                            val optionIndex = row * 2 + col
                            val option = currentOptions[optionIndex]
                            val isSelected = selectedOption == option
                            val isCorrect = option.nameTh == currentTarget.nameTh

                            val borderColor = when {
                                !isAnswered -> Color.Transparent
                                isCorrect -> Color(0xFF4CAF50)
                                isSelected -> Color(0xFFF44336)
                                else -> Color.Transparent
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(88.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(enabled = !isAnswered) {
                                        selectedOption = option
                                        isAnswered = true
                                        if (isCorrect) {
                                            score += 20
                                            correctCount++
                                            playLocalSound(context, R.raw.chiotrue)
                                        } else {
                                            playLocalSound(context, R.raw.chiofalse)
                                        }
                                    }
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                border = if (isAnswered && (isCorrect || isSelected)) BorderStroke(4.dp, borderColor) else null,
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(option.color)
                                                .shadow(3.dp, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = option.nameTh,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333)
                                        )
                                    }

                                    if (isAnswered) {
                                        if (isCorrect) {
                                            Text(
                                                "✓",
                                                color = Color(0xFF4CAF50),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        } else if (isSelected) {
                                            Text(
                                                "✕",
                                                color = Color(0xFFF44336),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Next / Finish Button
            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedOption = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, questions.size, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 ทายสีสันสดใสครบแล้ว! ยอดเยี่ยมมาก",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedOption = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 8. ANIMAL SOUND & QUIZ GAME ====================

data class AnimalSoundItem(
    val soundText: String,
    val animalName: String,
    val emoji: String,
    val clue: String
)

@Composable
fun AnimalSoundGameScreen(
    onExit: () -> Unit,
    onSpeak: (String) -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSec: Int) -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var selectedAnimal by remember { mutableStateOf<AnimalSoundItem?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val allAnimals = remember {
        listOf(
            AnimalSoundItem("โฮ่ง โฮ่ง!", "สุนัข", "🐶", "เพื่อนผู้ซื่อสัตย์ เห่าทักทายโฮ่งๆ"),
            AnimalSoundItem("เหมียว เหมียว!", "แมว", "🐱", "ชอบจับหนู ชอบออดอ้อนคลอเคลีย"),
            AnimalSoundItem("แป๋น แป๋น!", "ช้าง", "🐘", "ตัวโตมหึมา มีงวงยาวและงาขาว"),
            AnimalSoundItem("โฮก โฮก!", "สิงโต", "🦁", "เจ้าป่าผู้สง่างาม มีแผงคอหนานุ่ม"),
            AnimalSoundItem("ก้าบ ก้าบ!", "เป็ด", "🦆", "ว่ายน้ำคล่องแคล่ว ปากแบนเดินต้วมเตี้ยม"),
            AnimalSoundItem("เจี๊ยก เจี๊ยก!", "ลิง", "🐵", "ปีนป่ายต้นไม้คล่องแคล่ว ชอบกินกล้วย"),
            AnimalSoundItem("มอ มอ!", "วัว", "🐮", "กินหญ้าเขียวสด ให้น้ำนมหวานมัน"),
            AnimalSoundItem("เอก อี๊ เอก เอก!", "ไก่", "🐔", "ขันปลุกทุกคนแต่เช้าตรู่ มีหงอนแดง")
        )
    }

    val questions = remember {
        allAnimals.shuffled().take(5).map { target ->
            val distractors = allAnimals.filter { it.animalName != target.animalName }.shuffled().take(3)
            val options = (distractors + target).shuffled()
            Pair(target, options)
        }
    }

    val currentTarget = questions[currentQIndex].first
    val currentOptions = questions[currentQIndex].second

    LaunchedEffect(currentQIndex) {
        onSpeak("${currentTarget.soundText} ฉันคือสัตว์ตัวไหนนะ?")
    }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(1000L)
            timeSeconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFFFFFFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            // Top Bar
            GameScreenTopBar(
                title = "🦁 ทายเสียงสัตว์",
                titleColor = Color(0xFF1B5E20),
                score = score,
                badgeBg = Color(0xFFF1F8E9),
                badgeBorder = Color(0xFF81C784),
                badgeTextColor = Color(0xFF1B5E20),
                onExit = onExit
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ข้อที่ ${currentQIndex + 1} จาก ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "⏱ $timeSeconds วินาที",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentQIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Clue Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ฟังเสียงสัตว์ร้อง",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${currentTarget.soundText}\"",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTarget.clue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF558B2F),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.clickable {
                            onSpeak("${currentTarget.soundText} ฉันคือสัตว์ตัวไหนนะ?")
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "ฟังเสียงอีกครั้ง",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "กดฟังเสียงร้องอีกครั้ง",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Animal Option Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0..1) {
                            val optionIndex = row * 2 + col
                            val option = currentOptions[optionIndex]
                            val isSelected = selectedAnimal == option
                            val isCorrect = option.animalName == currentTarget.animalName

                            val borderColor = when {
                                !isAnswered -> Color.Transparent
                                isCorrect -> Color(0xFF4CAF50)
                                isSelected -> Color(0xFFF44336)
                                else -> Color.Transparent
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(enabled = !isAnswered) {
                                        selectedAnimal = option
                                        isAnswered = true
                                        if (isCorrect) {
                                            score += 20
                                            correctCount++
                                            playLocalSound(context, R.raw.chiotrue)
                                        } else {
                                            playLocalSound(context, R.raw.chiofalse)
                                        }
                                    }
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                border = if (isAnswered && (isCorrect || isSelected)) BorderStroke(4.dp, borderColor) else null,
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(option.emoji, fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = option.animalName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    if (isAnswered) {
                                        if (isCorrect) {
                                            Text(
                                                "✓",
                                                color = Color(0xFF4CAF50),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        } else if (isSelected) {
                                            Text(
                                                "✕",
                                                color = Color(0xFFF44336),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedAnimal = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, questions.size, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 ทายเสียงสัตว์ครบแล้ว! ยอดเยี่ยมมาก",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedAnimal = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 9. KIDS MATH & COUNTING GAME ====================

data class KidsMathItem(
    val equationText: String,
    val visualItems: String,
    val answer: Int,
    val options: List<Int>
)

@Composable
fun KidsMathGameScreen(
    onExit: () -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSec: Int) -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val allQuestions = remember {
        listOf(
            KidsMathItem("3 + 2 = ?", "🍎 🍎 🍎   +   🍎 🍎", 5, listOf(4, 5, 6, 3)),
            KidsMathItem("4 + 3 = ?", "🍊 🍊 🍊 🍊   +   🍊 🍊 🍊", 7, listOf(6, 7, 8, 5)),
            KidsMathItem("5 - 2 = ?", "🍌 🍌 🍌 🍌 🍌  (หักออก 2)", 3, listOf(2, 3, 4, 1)),
            KidsMathItem("6 + 2 = ?", "⭐ ⭐ ⭐ ⭐ ⭐ ⭐   +   ⭐ ⭐", 8, listOf(7, 8, 9, 6)),
            KidsMathItem("7 - 3 = ?", "🍓 🍓 🍓 🍓 🍓 🍓 🍓  (หักออก 3)", 4, listOf(3, 4, 5, 2)),
            KidsMathItem("5 + 4 = ?", "🚗 🚗 🚗 🚗 🚗   +   🚗 🚗 🚗 🚗", 9, listOf(8, 9, 10, 7)),
            KidsMathItem("8 - 4 = ?", "🎈 🎈 🎈 🎈 🎈 🎈 🎈 🎈  (แตก 4)", 4, listOf(3, 4, 5, 6)),
            KidsMathItem("2 + 4 = ?", "🍦 🍦   +   🍦 🍦 🍦 🍦", 6, listOf(5, 6, 7, 4))
        )
    }

    val questions = remember { allQuestions.shuffled().take(5) }
    val currentQuestion = questions[currentQIndex]

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(1000L)
            timeSeconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFFFFFFFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            // Top Bar
            GameScreenTopBar(
                title = "➕ บวกลบเลขเร็ว",
                titleColor = Color(0xFF006064),
                score = score,
                badgeBg = Color(0xFFE0F7FA),
                badgeBorder = Color(0xFF4DD0E1),
                badgeTextColor = Color(0xFF006064),
                onExit = onExit
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ข้อที่ ${currentQIndex + 1} จาก ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006064)
                )
                Text(
                    text = "⏱ $timeSeconds วินาที",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentQIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00ACC1),
                trackColor = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Math Visual Board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "นับและคิดหาคำตอบ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.visualItems,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE0F7FA)
                    ) {
                        Text(
                            text = currentQuestion.equationText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00838F),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Number Answer Options (2x2 Grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0..1) {
                            val optionIndex = row * 2 + col
                            val option = currentQuestion.options[optionIndex]
                            val isSelected = selectedOption == option
                            val isCorrect = option == currentQuestion.answer

                            val borderColor = when {
                                !isAnswered -> Color.Transparent
                                isCorrect -> Color(0xFF4CAF50)
                                isSelected -> Color(0xFFF44336)
                                else -> Color.Transparent
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(enabled = !isAnswered) {
                                        selectedOption = option
                                        isAnswered = true
                                        if (isCorrect) {
                                            score += 20
                                            correctCount++
                                            playLocalSound(context, R.raw.chiotrue)
                                        } else {
                                            playLocalSound(context, R.raw.chiofalse)
                                        }
                                    }
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                border = if (isAnswered && (isCorrect || isSelected)) BorderStroke(4.dp, borderColor) else null,
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$option",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00838F)
                                    )

                                    if (isAnswered) {
                                        if (isCorrect) {
                                            Text(
                                                "✓",
                                                color = Color(0xFF4CAF50),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        } else if (isSelected) {
                                            Text(
                                                "✕",
                                                color = Color(0xFFF44336),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedOption = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, questions.size, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 คิดเลขเก่งมากๆ! ตอบครบทุกข้อแล้ว",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedOption = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 10. CATEGORY SORT GAME ====================

data class CategorySortItem(
    val itemName: String,
    val emoji: String,
    val category: String
)

@Composable
fun CategorySortGameScreen(
    onExit: () -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSec: Int) -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("🍎 ผลไม้", "🥦 ผักสด", "🐶 สัตว์น่ารัก", "🚗 ยานพาหนะ")
    }

    val allItems = remember {
        listOf(
            CategorySortItem("แตงโม", "🍉", "🍎 ผลไม้"),
            CategorySortItem("แครอท", "🥕", "🥦 ผักสด"),
            CategorySortItem("สิงโต", "🦁", "🐶 สัตว์น่ารัก"),
            CategorySortItem("รถไฟ", "🚂", "🚗 ยานพาหนะ"),
            CategorySortItem("แอปเปิ้ล", "🍎", "🍎 ผลไม้"),
            CategorySortItem("บรอกโคลี", "🥦", "🥦 ผักสด"),
            CategorySortItem("ช้าง", "🐘", "🐶 สัตว์น่ารัก"),
            CategorySortItem("เครื่องบิน", "✈️", "🚗 ยานพาหนะ"),
            CategorySortItem("กล้วยหอม", "🍌", "🍎 ผลไม้"),
            CategorySortItem("มะเขือเทศ", "🍅", "🥦 ผักสด")
        )
    }

    val questions = remember { allItems.shuffled().take(5) }
    val currentTarget = questions[currentQIndex]

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(1000L)
            timeSeconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFFFFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            // Top Bar
            GameScreenTopBar(
                title = "🍉 แยกหมวดหมู่",
                titleColor = Color(0xFFE65100),
                score = score,
                badgeBg = Color(0xFFFFF3E0),
                badgeBorder = Color(0xFFFFB74D),
                badgeTextColor = Color(0xFFE65100),
                onExit = onExit
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ข้อที่ ${currentQIndex + 1} จาก ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF360C)
                )
                Text(
                    text = "⏱ $timeSeconds วินาที",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentQIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF9800),
                trackColor = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Item Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "สิ่งนี้จัดอยู่ในหมวดหมู่อะไร?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentTarget.emoji,
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTarget.itemName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Category Boxes (2x2 Grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..1) {
                            val catIndex = row * 2 + col
                            val category = categories[catIndex]
                            val isSelected = selectedCategory == category
                            val isCorrect = category == currentTarget.category

                            val borderColor = when {
                                !isAnswered -> Color.Transparent
                                isCorrect -> Color(0xFF4CAF50)
                                isSelected -> Color(0xFFF44336)
                                else -> Color.Transparent
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(enabled = !isAnswered) {
                                        selectedCategory = category
                                        isAnswered = true
                                        if (isCorrect) {
                                            score += 20
                                            correctCount++
                                            playLocalSound(context, R.raw.chiotrue)
                                        } else {
                                            playLocalSound(context, R.raw.chiofalse)
                                        }
                                    }
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                border = if (isAnswered && (isCorrect || isSelected)) BorderStroke(4.dp, borderColor) else null,
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )

                                    if (isAnswered) {
                                        if (isCorrect) {
                                            Text(
                                                "✓",
                                                color = Color(0xFF4CAF50),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        } else if (isSelected) {
                                            Text(
                                                "✕",
                                                color = Color(0xFFF44336),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                            selectedCategory = null
                            isAnswered = false
                        } else {
                            isGameOver = true
                            onComplete(score, 20, questions.size, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 แยกหมวดหมู่ได้ถูกต้อง! เยี่ยมมาก",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    selectedCategory = null
                    isAnswered = false
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}

// ==================== 11. WORD BUILDER GAME ====================

data class WordBuilderItem(
    val targetWord: String,
    val clue: String,
    val emoji: String,
    val lettersPool: List<String>
)

@Composable
fun WordBuilderGameScreen(
    onExit: () -> Unit,
    onSpeak: (String) -> Unit,
    onComplete: (score: Int, earnedStars: Int, totalQ: Int, correctQ: Int, timeSec: Int) -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableIntStateOf(0) }
    var currentQIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    val allWords = remember {
        listOf(
            WordBuilderItem("กา", "นกกาตัวสีดำ ร้อง กา กา", "🐦", listOf("ก", "า", "ข")),
            WordBuilderItem("ตา", "อวัยวะสำหรับมองดูสิ่งรอบตัว", "👀", listOf("ต", "า", "ด")),
            WordBuilderItem("มา", "เดินเข้ามาหา มานี่เร็ว", "🏃", listOf("ม", "า", "น")),
            WordBuilderItem("ปลา", "สัตว์น้ำว่ายไปมา มีเกล็ดและครีบ", "🐟", listOf("ป", "ล", "า", "ม")),
            WordBuilderItem("แมว", "สัตว์เลี้ยงน่ารัก ร้องเหมียวๆ", "🐱", listOf("แ", "ม", "ว", "น")),
            WordBuilderItem("หมา", "เพื่อนสี่ขาแสนรู้ ร้องโฮ่งๆ", "🐶", listOf("ห", "ม", "า", "ค"))
        )
    }

    val questions = remember { allWords.shuffled().take(5) }
    val currentTarget = questions[currentQIndex]

    // Track chosen tile indices from the pool
    var selectedTileIndices by remember(currentQIndex) { mutableStateOf(listOf<Int>()) }
    var isAnswered by remember(currentQIndex) { mutableStateOf(false) }
    var isCorrect by remember(currentQIndex) { mutableStateOf(false) }

    LaunchedEffect(currentQIndex) {
        onSpeak("คำใบ้คือ ${currentTarget.clue}")
    }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(1000L)
            timeSeconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFFFFFFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
        ) {
            // Top Bar
            GameScreenTopBar(
                title = "🧩 เรียงคำศัพท์",
                titleColor = Color(0xFF6A1B9A),
                score = score,
                badgeBg = Color(0xFFF3E5F5),
                badgeBorder = Color(0xFFBA68C8),
                badgeTextColor = Color(0xFF6A1B9A),
                onExit = onExit
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ข้อที่ ${currentQIndex + 1} จาก ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C)
                )
                Text(
                    text = "⏱ $timeSeconds วินาที",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentQIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF9C27B0),
                trackColor = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Clue Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTarget.emoji,
                        fontSize = 44.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTarget.clue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6A1B9A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF3E5F5),
                        modifier = Modifier.clickable { onSpeak(currentTarget.clue) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "ฟังคำใบ้",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "กดฟังคำใบ้",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Word Slots
            Text(
                text = "แตะตัวอักษรเพื่อเรียงคำ:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A148C),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentTarget.targetWord.forEachIndexed { slotIdx, _ ->
                    val placedTileIndex = selectedTileIndices.getOrNull(slotIdx)
                    val letter = placedTileIndex?.let { currentTarget.lettersPool.getOrNull(it) } ?: ""

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (letter.isNotEmpty()) Color(0xFFE1BEE7) else Color.White,
                        border = BorderStroke(
                            2.dp,
                            if (isAnswered) {
                                if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                            } else {
                                Color(0xFFBA68C8)
                            }
                        ),
                        modifier = Modifier
                            .size(50.dp)
                            .padding(3.dp)
                            .clickable(enabled = !isAnswered && letter.isNotEmpty()) {
                                selectedTileIndices = selectedTileIndices.filterIndexed { idx, _ -> idx != slotIdx }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = letter,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4A148C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Letter Tiles Pool
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentTarget.lettersPool.forEachIndexed { poolIdx, letter ->
                    val isUsed = selectedTileIndices.contains(poolIdx)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isUsed) Color.LightGray.copy(alpha = 0.4f) else Color.White,
                        shadowElevation = if (isUsed) 0.dp else 3.dp,
                        border = BorderStroke(2.dp, if (isUsed) Color.Gray.copy(alpha = 0.3f) else Color(0xFF9C27B0)),
                        modifier = Modifier
                            .size(50.dp)
                            .padding(3.dp)
                            .clickable(enabled = !isAnswered && !isUsed && selectedTileIndices.size < currentTarget.targetWord.length) {
                                selectedTileIndices = selectedTileIndices + poolIdx
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = letter,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isUsed) Color.Gray else Color(0xFF6A1B9A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Check / Clear Row
            if (!isAnswered) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { selectedTileIndices = emptyList() },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, Color(0xFF9C27B0))
                    ) {
                        Text("ล้างคำตอบ", color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val formedWord = selectedTileIndices.map { currentTarget.lettersPool[it] }.joinToString("")
                            if (formedWord == currentTarget.targetWord) {
                                isAnswered = true
                                isCorrect = true
                                score += 20
                                correctCount++
                                playLocalSound(context, R.raw.chiotrue)
                                onSpeak(formedWord)
                            } else {
                                playLocalSound(context, R.raw.chiofalse)
                                Toast.makeText(context, "ยังไม่ถูกต้อง ลองสะกดใหม่อีกครั้งนะจ๊ะ!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = selectedTileIndices.size == currentTarget.targetWord.length,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Text("ตรวจคำตอบ ✓", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQIndex < questions.size - 1) {
                            currentQIndex++
                        } else {
                            isGameOver = true
                            onComplete(score, 20, questions.size, correctCount, timeSeconds)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Text(
                        if (currentQIndex < questions.size - 1) "ข้อถัดไป ▶" else "ดูผลคะแนน 🏆",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (isGameOver) {
            GameWinDialog(
                title = "🎉 สะกดคำศัพท์เก่งมากๆ! ยอดเยี่ยม",
                score = score,
                earnedStars = if (score >= 60) 20 else 10,
                timeSeconds = timeSeconds,
                onPlayAgain = {
                    currentQIndex = 0
                    score = 0
                    correctCount = 0
                    timeSeconds = 0
                    isGameOver = false
                },
                onExit = onExit
            )
        }
    }
}


