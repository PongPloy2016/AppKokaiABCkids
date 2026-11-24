package com.pongploydev.kokaiabckids.activity

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.confixdata.DataManager
import com.pongploydev.kokaiabckids.confixdata.File_Confix_Data
import com.pongploydev.kokaiabckids.confixdata.LearningCategory
import com.pongploydev.kokaiabckids.confixdata.ThaiLearningDictionary
import com.pongploydev.kokaiabckids.model.LearningDataModel
import com.pongploydev.kokaiabckids.model.QuizScore
import com.pongploydev.kokaiabckids.utils.QuizProgressManager
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.profilefeature.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

enum class QuizLanguageMode(val label: String) {
    THAI("ไทย"),
    ENGLISH("EN"),
    BILINGUAL("ไทย+EN")
}

enum class QuizPlayMode {
    STANDARD,
    LISTENING,
    MATH,
    TIMED
}

enum class QuizLifeline(
    val thaiLabel: String,
    val engLabel: String,
    val starCost: Int,
    val accentColor: Color,
    val bgColor: Color,
    val icon: ImageVector
) {
    FIFTY_FIFTY(
        thaiLabel = "ตัด 2 ข้อ",
        engLabel = "50:50",
        starCost = 2,
        accentColor = Color(0xFF0284C7),
        bgColor = Color(0xFFE0F2FE),
        icon = Icons.Rounded.ContentCut
    ),
    HINT(
        thaiLabel = "ใบ้คำตอบ",
        engLabel = "Hint",
        starCost = 3,
        accentColor = Color(0xFFD97706),
        bgColor = Color(0xFFFEF3C7),
        icon = Icons.Rounded.Lightbulb
    ),
    SKIP_PASS(
        thaiLabel = "ข้ามข้อ",
        engLabel = "Skip",
        starCost = 5,
        accentColor = Color(0xFF7C3AED),
        bgColor = Color(0xFFEDE9FE),
        icon = Icons.Rounded.FastForward
    )
}

data class QuizItem(
    val id: String,
    val englishText: String,
    val thaiText: String,
    val imageRes: Int = 0,
    val soundRes: Int? = null,
    val mathEquation: String? = null,
    val mathEmojiGroupA: String? = null,
    val mathEmojiGroupB: String? = null,
    val mathOperator: String? = null,
    val mathSpeakThai: String? = null,
    val mathSpeakEng: String? = null,
    val mathAnswerNum: Int? = null
)

private data class QuizResultSummary(
    val score: Int,
    val totalQuestions: Int,
    val durationSeconds: Long,
    val defaultPlayerName: String,
    val isRegisteredMember: Boolean
)

class QuizSessionActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var rawMediaPlayer: MediaPlayer? = null

    private var topicId = 1
    private var topicTitle = ""
    private var playMode = QuizPlayMode.STANDARD
    private var quizList: List<QuizItem> = emptyList()
    private var totalQuestions = 20

    private lateinit var dataManager: SupabaseDataManager
    private lateinit var authManager: SupabaseAuthManager

    private var isQuizResultSaved = false

    companion object {
        const val EXTRA_QUIZ_MODE = "EXTRA_QUIZ_MODE"
        const val EXTRA_QUESTION_COUNT = "EXTRA_QUESTION_COUNT"
        private const val PREF_QUIZ_SETTINGS = "quiz_session_settings"
        private const val KEY_LANG_MODE = "quiz_lang_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        window.statusBarColor = "#E6F4FE".toColorInt()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        dataManager = SupabaseDataManager.getInstance(this)
        authManager = SupabaseAuthManager.getInstance(this)

        topicId = intent.getIntExtra("EXTRA_TOPIC_ID", 1)
        topicTitle = intent.getStringExtra("EXTRA_TOPIC_TITLE") ?: "แบบทดสอบ"
        playMode = runCatching {
            QuizPlayMode.valueOf(intent.getStringExtra(EXTRA_QUIZ_MODE) ?: QuizPlayMode.STANDARD.name)
        }.getOrDefault(QuizPlayMode.STANDARD)
        totalQuestions = intent.getIntExtra(
            EXTRA_QUESTION_COUNT,
            when (playMode) {
                QuizPlayMode.MATH -> 15
                QuizPlayMode.TIMED -> 30
                else -> 20
            }
        ).coerceIn(5, 40)

        quizList = if (playMode == QuizPlayMode.MATH) {
            emptyList()
        } else {
            buildQuizData(topicId)
        }
        if (playMode != QuizPlayMode.MATH && quizList.isEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลแบบทดสอบในหมวดนี้", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tts = TextToSpeech(this, this)

        val savedLang = loadPreferredLanguage(topicId)
        val initialStars = dataManager.getUserTotalStars()

        setContent {
            MaterialTheme {
                QuizSessionScreen(
                    topicId = topicId,
                    topicTitle = topicTitle,
                    playMode = playMode,
                    quizList = quizList,
                    totalQuestions = totalQuestions,
                    initialLangMode = savedLang,
                    initialStarBalance = initialStars,
                    onLanguageChanged = { mode -> savePreferredLanguage(mode) },
                    onSpeakItem = { item, langMode, slow -> speakItem(item, langMode, slow) },
                    onSpeakFeedback = { isCorrect, correctItem, langMode ->
                        speakAnswerFeedback(isCorrect, correctItem, langMode)
                    },
                    onSpendStars = { cost -> spendUserStars(cost) },
                    onQuizFinished = { finalScore, durationSec, onSummaryReady ->
                        handleQuizFinished(finalScore, durationSec, onSummaryReady)
                    },
                    onSaveLeaderboard = { playerName, finalScore, durationSec, onSaved ->
                        saveScoreToFirestore(playerName, finalScore, durationSec, onSaved)
                    },
                    onRestartResetFlag = { isQuizResultSaved = false },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun loadPreferredLanguage(topicId: Int): QuizLanguageMode {
        val saved = getSharedPreferences(PREF_QUIZ_SETTINGS, Context.MODE_PRIVATE)
            .getString(KEY_LANG_MODE, null)
        if (saved != null) {
            return runCatching { QuizLanguageMode.valueOf(saved) }.getOrDefault(QuizLanguageMode.BILINGUAL)
        }
        return when (topicId) {
            1 -> QuizLanguageMode.THAI
            2 -> QuizLanguageMode.ENGLISH
            else -> QuizLanguageMode.BILINGUAL
        }
    }

    private fun savePreferredLanguage(mode: QuizLanguageMode) {
        getSharedPreferences(PREF_QUIZ_SETTINGS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG_MODE, mode.name)
            .apply()
    }

    private fun buildQuizData(topicId: Int): List<QuizItem> {
        fun List<LearningDataModel>.toBilingualItems(): List<QuizItem> = map { item ->
            val eng = ThaiLearningDictionary.toEnglish(item.showTitle)
            val thai = ThaiLearningDictionary.toThai(item.showTitle, item.speakTitle)
            QuizItem(
                id = "${item.showTitle}_${item.image}",
                englishText = eng.ifBlank { item.showTitle },
                thaiText = thai.ifBlank { item.showTitle },
                imageRes = item.image
            )
        }

        return when (topicId) {
            1 -> {
                val letters = arrayOf(
                    "ก", "ข", "ฃ", "ค", "ฅ", "ฆ", "ง", "จ", "ฉ", "ช", "ซ", "ฌ", "ญ", "ฎ", "ฏ",
                    "ฐ", "ฑ", "ฒ", "ณ", "ด", "ต", "ถ", "ท", "ธ", "น", "บ", "ป", "ผ", "ฝ", "พ",
                    "ฟ", "ภ", "ม", "ย", "ร", "ล", "ว", "ศ", "ษ", "ส", "ห", "ฬ", "อ", "ฮ"
                )
                val names = arrayOf(
                    "ก ไก่", "ข ไข่", "ฃ ขวด", "ค ควาย", "ฅ คน", "ฆ ระฆัง", "ง งู", "จ จาน",
                    "ฉ ฉิ่ง", "ช ช้าง", "ซ โซ่", "ฌ เฌอ", "ญ หญิง", "ฎ ชฎา", "ฏ ปฏัก", "ฐ ฐาน",
                    "ฑ มณโฑ", "ฒ ผู้เฒ่า", "ณ เณร", "ด เด็ก", "ต เต่า", "ถ ถุง", "ท ทหาร", "ธ ธง",
                    "น หนู", "บ ใบไม้", "ป ปลา", "ผ ผึ้ง", "ฝ ฝา", "พ พาน", "ฟ ฟัน", "ภ สำเภา",
                    "ม ม้า", "ย ยักษ์", "ร เรือ", "ล ลิง", "ว แหวน", "ศ ศาลา", "ษ ฤๅษี", "ส เสือ",
                    "ห หีบ", "ฬ จุฬา", "อ อ่าง", "ฮ นกฮูก"
                )
                letters.indices.map { i ->
                    QuizItem(
                        id = letters[i],
                        englishText = letters[i],
                        thaiText = names[i],
                        imageRes = File_Confix_Data.image_confixe1[i],
                        soundRes = File_Confix_Data.sound1[i]
                    )
                }
            }
            2 -> {
                val letters = (0 until 26).map { ('A' + it).toString() }
                val drawables = intArrayOf(
                    R.drawable.e_a, R.drawable.e_b, R.drawable.e_c, R.drawable.e_d, R.drawable.e_e,
                    R.drawable.e_f, R.drawable.e_g, R.drawable.e_h, R.drawable.e_i, R.drawable.e_j,
                    R.drawable.e_k, R.drawable.e_l, R.drawable.e_m, R.drawable.e_n, R.drawable.e_o,
                    R.drawable.e_p, R.drawable.e_q, R.drawable.e_r, R.drawable.e_s, R.drawable.e_t,
                    R.drawable.e_u, R.drawable.e_v, R.drawable.e_w, R.drawable.e_x, R.drawable.e_y,
                    R.drawable.e_z
                )
                letters.indices.map { i ->
                    QuizItem(letters[i], letters[i], letters[i], drawables[i])
                }
            }
            3 -> {
                val thaiDigits = arrayOf("๐ ศูนย์", "๑ หนึ่ง", "๒ สอง", "๓ สาม", "๔ สี่", "๕ ห้า", "๖ หก", "๗ เจ็ด", "๘ แปด", "๙ เก้า")
                val engDigits = arrayOf("0 Zero", "1 One", "2 Two", "3 Three", "4 Four", "5 Five", "6 Six", "7 Seven", "8 Eight", "9 Nine")
                val drawables = File_Confix_Data.imgMenu3
                val sounds = File_Confix_Data.imgMenu1MediaPlayer2
                (0 until 10).map { i ->
                    QuizItem(
                        id = i.toString(),
                        englishText = engDigits[i],
                        thaiText = thaiDigits[i],
                        imageRes = drawables[i],
                        soundRes = sounds[i]
                    )
                }
            }
            4 -> DataManager.getDataByCategory(0).toBilingualItems()
            5 -> LearningCategory.ALPHABET.data.toBilingualItems()
            6 -> LearningCategory.NUMBERS.data.take(10).toBilingualItems()
            7 -> LearningCategory.ANIMALS.data.toBilingualItems()
            8 -> LearningCategory.FRUITS.data.toBilingualItems()
            9 -> LearningCategory.COLORS.data.toBilingualItems()
            10 -> LearningCategory.VEGETABLES.data.toBilingualItems()
            11 -> LearningCategory.SPORTS.data.toBilingualItems()
            12 -> LearningCategory.VEHICLES.data.toBilingualItems()
            13 -> LearningCategory.SHAPES.data.toBilingualItems()
            14 -> LearningCategory.FOODS.data.toBilingualItems()
            15 -> LearningCategory.BODY_PARTS.data.toBilingualItems()
            16 -> LearningCategory.FLOWERS.data.toBilingualItems()
            17 -> LearningCategory.BIRDS.data.toBilingualItems()
            18 -> LearningCategory.PROFESSIONS.data.toBilingualItems()
            99, 106, 301 -> {
                val categoriesToMix = listOf(
                    LearningCategory.ANIMALS.data,
                    LearningCategory.FRUITS.data,
                    LearningCategory.COLORS.data,
                    LearningCategory.VEHICLES.data,
                    LearningCategory.SHAPES.data,
                    LearningCategory.FOODS.data,
                    LearningCategory.SPORTS.data,
                    LearningCategory.PROFESSIONS.data,
                    LearningCategory.CLOTHES.data.take(20),
                    LearningCategory.SCHOOL_ITEMS.data
                )
                categoriesToMix.flatMap { it.shuffled().take(5).toBilingualItems() }.shuffled()
            }
            101 -> buildQuizData(1)
            102 -> buildQuizData(3)
            103 -> LearningCategory.ANIMALS.data.toBilingualItems()
            104 -> (LearningCategory.FRUITS.data + LearningCategory.FOODS.data).toBilingualItems().shuffled()
            105 -> (LearningCategory.COLORS.data + LearningCategory.SHAPES.data).toBilingualItems().shuffled()
            302 -> LearningCategory.CLOTHES.data.take(20).toBilingualItems()
            303 -> LearningCategory.HOUSE_ITEMS.data.toBilingualItems()
            304 -> LearningCategory.SCHOOL_ITEMS.data.toBilingualItems()
            305 -> LearningCategory.COUNTRIES.data.toBilingualItems()
            306 -> LearningCategory.MONTHS.data.toBilingualItems()
            307 -> LearningCategory.GEOMETRY.data.toBilingualItems()
            else -> emptyList()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale("th", "TH")
        }
    }

    private fun speakItem(item: QuizItem, langMode: QuizLanguageMode, slow: Boolean = false) {
        try {
            rawMediaPlayer?.release()
            rawMediaPlayer = null

            // Math mode speaks the math question naturally
            if (playMode == QuizPlayMode.MATH && (item.mathSpeakThai != null || item.mathSpeakEng != null)) {
                if (!isTtsReady) return
                tts?.setSpeechRate(if (slow) 0.72f else 0.92f)
                if (langMode == QuizLanguageMode.ENGLISH) {
                    tts?.language = Locale.US
                    tts?.speak(item.mathSpeakEng ?: item.englishText, TextToSpeech.QUEUE_FLUSH, null, "quiz_math_en")
                } else {
                    tts?.language = Locale("th", "TH")
                    tts?.speak(item.mathSpeakThai ?: item.thaiText, TextToSpeech.QUEUE_FLUSH, null, "quiz_math_th")
                }
                return
            }

            // Play authentic Thai audio for Topic 1 / 101 when in Thai mode (at normal speed)
            if (!slow && (topicId == 1 || topicId == 101) && langMode == QuizLanguageMode.THAI && item.soundRes != null && item.soundRes != 0) {
                rawMediaPlayer = MediaPlayer.create(this, item.soundRes)?.apply {
                    setOnCompletionListener {
                        release()
                        rawMediaPlayer = null
                    }
                    start()
                }
                return
            }

            if (!isTtsReady) return
            tts?.setSpeechRate(if (slow) 0.65f else 0.92f)
            when (langMode) {
                QuizLanguageMode.THAI -> {
                    tts?.language = Locale("th", "TH")
                    tts?.speak(item.thaiText, TextToSpeech.QUEUE_FLUSH, null, "quiz_item_th")
                }
                QuizLanguageMode.ENGLISH -> {
                    val hasThaiOnly = item.englishText.any { it in '\u0E00'..'\u0E7F' }
                    tts?.language = if (hasThaiOnly) Locale("th", "TH") else Locale.US
                    tts?.speak(item.englishText, TextToSpeech.QUEUE_FLUSH, null, "quiz_item_en")
                }
                QuizLanguageMode.BILINGUAL -> {
                    val hasThaiOnly = item.englishText.any { it in '\u0E00'..'\u0E7F' }
                    if (hasThaiOnly || item.englishText.equals(item.thaiText, ignoreCase = true)) {
                        tts?.language = Locale("th", "TH")
                        tts?.speak(item.thaiText, TextToSpeech.QUEUE_FLUSH, null, "quiz_item_bi")
                    } else {
                        tts?.language = Locale.US
                        tts?.speak(item.englishText, TextToSpeech.QUEUE_FLUSH, null, "quiz_item_bi_en")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("QuizSession", "speakItem error: ${e.message}")
        }
    }

    private fun speakAnswerFeedback(isCorrect: Boolean, correctItem: QuizItem, langMode: QuizLanguageMode) {
        if (!isTtsReady) return
        try {
            tts?.stop()
            if (langMode == QuizLanguageMode.ENGLISH) {
                tts?.language = Locale.US
                val phrase = if (isCorrect) "Great job!" else "It is ${correctItem.englishText}"
                tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "quiz_fb_en")
            } else {
                tts?.language = Locale("th", "TH")
                val phrase = if (isCorrect) "เก่งมากค่ะ" else "คำตอบคือ ${correctItem.thaiText}"
                tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "quiz_fb_th")
            }
        } catch (e: Exception) {
            Log.w("QuizSession", "speakAnswerFeedback error: ${e.message}")
        }
    }

    private suspend fun spendUserStars(cost: Int): Pair<Boolean, Int> {
        val supabaseUser = authManager.getStoredUser()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = supabaseUser?.id ?: firebaseUser?.uid ?: authManager.getGuestUserId()
        return withContext(Dispatchers.IO) {
            dataManager.spendStars(userId, cost)
        }
    }

    private fun handleQuizFinished(
        finalScore: Int,
        durationSec: Long,
        onSummaryReady: (QuizResultSummary, Int) -> Unit
    ) {
        QuizProgressManager.recordQuizResult(this, topicId, finalScore, totalQuestions)
        saveQuizResultToSupabase(finalScore, durationSec)

        val supabaseUser = authManager.getStoredUser()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = supabaseUser?.id ?: authManager.getGuestUserId()

        lifecycleScope.launch(Dispatchers.IO) {
            val userProf = dataManager.fetchUserProfile(userId).getOrNull()
            val guestDefaultName = authManager.getOrCreateGuestDisplayName()
            val isRegisteredMember = (supabaseUser != null && supabaseUser.isAnonymous != true) ||
                (firebaseUser != null && !firebaseUser.isAnonymous)

            val memberName = userProf?.displayName?.takeIf {
                it.isNotBlank() && it != "นักอ่านตัวน้อย" && !it.startsWith("ผู้ใช้ชั่วคราว")
            } ?: supabaseUser?.userMetadata?.displayName?.takeIf {
                it.isNotBlank() && !it.startsWith("ผู้ใช้ชั่วคราว")
            } ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: supabaseUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() && it != "guest" }
                ?: firebaseUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: guestDefaultName

            val latestStars = dataManager.getUserTotalStars()

            withContext(Dispatchers.Main) {
                if (!isFinishing) {
                    onSummaryReady(
                        QuizResultSummary(
                            score = finalScore,
                            totalQuestions = totalQuestions,
                            durationSeconds = durationSec,
                            defaultPlayerName = memberName,
                            isRegisteredMember = isRegisteredMember
                        ),
                        latestStars
                    )
                }
            }
        }
    }

    private fun buildUserProfile(
        userId: String,
        email: String?,
        displayName: String
    ): UserProfile {
        return UserProfile(
            id = userId,
            email = email,
            displayName = displayName,
            avatarUrl = dataManager.getLocalAvatarUrl() ?: "",
            role = "student",
            totalStars = dataManager.getUserTotalStars(),
            totalCoins = 0,
            currentLevel = dataManager.getUserCurrentLevel(),
            streakDays = dataManager.getUserStreakDays()
        )
    }

    private fun saveQuizResultToSupabase(finalScore: Int, duration: Long) {
        if (isQuizResultSaved) return
        isQuizResultSaved = true

        val supabaseUser = authManager.getStoredUser()
        val userId = supabaseUser?.id ?: authManager.getGuestUserId()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (supabaseUser == null) {
                    authManager.signInAnonymously()
                }
                val guestOrMemberName = supabaseUser?.userMetadata?.displayName
                    ?.takeIf { it.isNotBlank() && !it.startsWith("ผู้ใช้ชั่วคราว") }
                    ?: dataManager.getLocalDisplayName()
                    ?: authManager.getOrCreateGuestDisplayName()
                val cleanEmail = supabaseUser?.email?.takeIf { it.isNotBlank() && it != "guest@supabase.io" }

                dataManager.upsertUserProfile(
                    buildUserProfile(
                        userId = userId,
                        email = cleanEmail,
                        displayName = guestOrMemberName
                    )
                )

                dataManager.saveQuizResult(
                    userId = userId,
                    topicId = topicId,
                    topicName = topicTitle,
                    score = finalScore,
                    totalQuestions = totalQuestions,
                    timeSpentSeconds = duration.toInt()
                )

                dataManager.addStars(userId, finalScore)
                if (finalScore >= 8) {
                    dataManager.unlockAchievement(
                        userId = userId,
                        badgeKey = "badge_quiz_master",
                        badgeName = "เกียรติบัตรนักตอบคำถาม 🏆",
                        badgeIcon = "🎓"
                    )
                }
            } catch (e: Exception) {
                Log.e("QuizSession", "Supabase save error", e)
            }
        }
    }

    private fun saveScoreToFirestore(
        name: String,
        finalScore: Int,
        duration: Long,
        onDone: () -> Unit
    ) {
        saveQuizResultToSupabase(finalScore, duration)

        val supabaseUser = authManager.getStoredUser()
        val userId = supabaseUser?.id ?: authManager.getGuestUserId()

        lifecycleScope.launch(Dispatchers.IO) {
            dataManager.saveUserProgress(
                stars = dataManager.getUserTotalStars(),
                level = dataManager.getUserCurrentLevel(),
                displayName = name
            )
            val cleanEmail = supabaseUser?.email?.takeIf { it.isNotBlank() && it != "guest@supabase.io" }
            val upsertedProf = dataManager.upsertUserProfile(
                buildUserProfile(
                    userId = userId,
                    email = cleanEmail,
                    displayName = name
                )
            ).getOrNull()
            val userProf = upsertedProf ?: dataManager.fetchUserProfile(userId).getOrNull()
            val avatarUrl = userProf?.avatarUrl?.ifBlank { null }
            val email = userProf?.email?.ifBlank { null } ?: cleanEmail
            val supabaseUserId = userProf?.id ?: userId

            val quizScore = QuizScore(
                userName = name,
                score = finalScore,
                totalQuestions = totalQuestions,
                topicTitle = topicTitle,
                durationSeconds = duration,
                avatarUrl = avatarUrl,
                email = email,
                user_id_supabase = supabaseUserId
            )

            withContext(Dispatchers.Main) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("quiz_scores_game")
                    .add(quizScore)
                    .addOnSuccessListener {
                        Toast.makeText(this@QuizSessionActivity, "บันทึกคะแนนสำเร็จแล้ว!", Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                    .addOnFailureListener {
                        firestore.collection("quiz_scores")
                            .add(quizScore)
                            .addOnSuccessListener {
                                Toast.makeText(this@QuizSessionActivity, "บันทึกคะแนนสำเร็จแล้ว!", Toast.LENGTH_SHORT).show()
                                onDone()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@QuizSessionActivity, "บันทึกคะแนนบนระบบแล้ว!", Toast.LENGTH_SHORT).show()
                                onDone()
                            }
                    }
            }
        }
    }

    override fun onDestroy() {
        rawMediaPlayer?.release()
        rawMediaPlayer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}

private data class QuestionRound(
    val questionNumber: Int,
    val correctItem: QuizItem,
    val options: List<QuizItem>,
    val correctIndex: Int
)

private fun buildQuestionRound(
    questionNumber: Int,
    quizList: List<QuizItem>,
    playMode: QuizPlayMode,
    topicId: Int
): QuestionRound {
    if (playMode == QuizPlayMode.MATH) {
        return buildMathQuestionRound(questionNumber, topicId)
    }

    val correctItem = quizList[Random.nextInt(quizList.size)]
    val correctIndex = Random.nextInt(4)
    val usedIds = mutableSetOf(correctItem.id, correctItem.englishText.lowercase())
    val opts = mutableListOf<QuizItem>()

    for (i in 0 until 4) {
        if (i == correctIndex) {
            opts.add(correctItem)
        } else {
            var candidate = quizList[Random.nextInt(quizList.size)]
            var attempts = 0
            while ((usedIds.contains(candidate.id) || usedIds.contains(candidate.englishText.lowercase())) && attempts < 25) {
                candidate = quizList[Random.nextInt(quizList.size)]
                attempts++
            }
            usedIds.add(candidate.id)
            usedIds.add(candidate.englishText.lowercase())
            opts.add(candidate)
        }
    }
    return QuestionRound(questionNumber, correctItem, opts, correctIndex)
}

private fun buildMathQuestionRound(questionNumber: Int, topicId: Int): QuestionRound {
    val emojis = listOf("🍎", "⭐", "🐶", "🍓", "🎈", "🐱", "🌸", "🚗", "🐟", "🦋", "🧁", "🐤")
    val emoji = emojis[Random.nextInt(emojis.size)]

    val subMode = when (topicId) {
        201 -> 0 // Counting
        202 -> 1 // Addition
        203 -> 2 // Subtraction
        else -> Random.nextInt(3) // Mixed Math
    }

    val answer: Int
    val equation: String
    val groupA: String
    val groupB: String?
    val operator: String?
    val speakTh: String
    val speakEn: String

    when (subMode) {
        0 -> {
            // Counting 1..10
            answer = Random.nextInt(1, 11)
            equation = "มีทั้งหมดกี่อัน?"
            groupA = emoji.repeat(answer)
            groupB = null
            operator = null
            speakTh = "ลองนับดูสิ มีทั้งหมดกี่อัน"
            speakEn = "Count carefully. How many are there?"
        }
        1 -> {
            // Addition (sum <= 10 or <= 15 for mixed)
            val maxOperand = if (topicId == 204) 8 else 5
            val a = Random.nextInt(1, maxOperand + 1)
            val b = Random.nextInt(1, (if (topicId == 204) 15 - a else 10 - a).coerceAtLeast(1) + 1)
            answer = a + b
            equation = "$a + $b = ?"
            groupA = emoji.repeat(a)
            groupB = emoji.repeat(b)
            operator = "+"
            speakTh = "$a บวก $b เท่ากับเท่าไหร่"
            speakEn = "What is $a plus $b?"
        }
        else -> {
            // Subtraction (positive answer >= 1)
            val maxStart = if (topicId == 204) 12 else 10
            val a = Random.nextInt(3, maxStart + 1)
            val b = Random.nextInt(1, a)
            answer = a - b
            equation = "$a − $b = ?"
            groupA = emoji.repeat(answer)
            groupB = emoji.repeat(b)
            operator = "−"
            speakTh = "$a ลบ $b เหลือเท่าไหร่"
            speakEn = "What is $a minus $b?"
        }
    }

    val thaiNumWords = mapOf(
        0 to "ศูนย์", 1 to "หนึ่ง", 2 to "สอง", 3 to "สาม", 4 to "สี่",
        5 to "ห้า", 6 to "หก", 7 to "เจ็ด", 8 to "แปด", 9 to "เก้า",
        10 to "สิบ", 11 to "สิบเอ็ด", 12 to "สิบสอง", 13 to "สิบสาม", 14 to "สิบสี่", 15 to "สิบห้า"
    )

    fun makeNumberOption(n: Int, isAnswer: Boolean): QuizItem {
        val thWord = thaiNumWords[n]?.let { "$n ($it)" } ?: "$n"
        return QuizItem(
            id = "math_${questionNumber}_$n",
            englishText = "$n",
            thaiText = thWord,
            imageRes = 0,
            mathEquation = if (isAnswer) equation else null,
            mathEmojiGroupA = if (isAnswer) groupA else null,
            mathEmojiGroupB = if (isAnswer) groupB else null,
            mathOperator = if (isAnswer) operator else null,
            mathSpeakThai = if (isAnswer) speakTh else null,
            mathSpeakEng = if (isAnswer) speakEn else null,
            mathAnswerNum = n
        )
    }

    val correctItem = makeNumberOption(answer, true)
    val wrongNumbers = mutableSetOf<Int>()
    val candidates = ((answer - 3).coerceAtLeast(0)..(answer + 4).coerceAtMost(15))
        .filter { it != answer }
        .shuffled()
    for (c in candidates) {
        if (wrongNumbers.size < 3) wrongNumbers.add(c)
    }
    var fallback = 1
    while (wrongNumbers.size < 3) {
        if (fallback != answer) wrongNumbers.add(fallback)
        fallback++
    }

    val correctIndex = Random.nextInt(4)
    val wrongList = wrongNumbers.toList()
    var wIdx = 0
    val opts = (0 until 4).map { idx ->
        if (idx == correctIndex) correctItem else makeNumberOption(wrongList[wIdx++], false)
    }
    return QuestionRound(questionNumber, correctItem, opts, correctIndex)
}

private fun getQuestionPrompt(
    topicId: Int,
    langMode: QuizLanguageMode,
    playMode: QuizPlayMode
): Pair<String, String?> {
    if (playMode == QuizPlayMode.LISTENING) {
        val th = "ฟังเสียงแล้วเลือกรูปที่ถูกต้อง!"
        val en = "Listen & tap the matching picture!"
        return when (langMode) {
            QuizLanguageMode.THAI -> th to null
            QuizLanguageMode.ENGLISH -> en to null
            QuizLanguageMode.BILINGUAL -> th to en
        }
    }
    if (playMode == QuizPlayMode.MATH) {
        val (th, en) = when (topicId) {
            201 -> "นับจำนวนภาพแล้วเลือกตัวเลข" to "Count the pictures & pick the number"
            202 -> "บวกเลขให้ถูกต้องนะ!" to "Add them together!"
            203 -> "ลบเลขแล้วเหลือเท่าไหร่?" to "Subtract & find what's left!"
            else -> "คิดเลขให้ถูกต้อง!" to "Solve the math puzzle!"
        }
        return when (langMode) {
            QuizLanguageMode.THAI -> th to null
            QuizLanguageMode.ENGLISH -> en to null
            QuizLanguageMode.BILINGUAL -> th to en
        }
    }

    val (th, en) = when (topicId) {
        1 -> "นี่คือพยัญชนะตัวอะไร?" to "Which Thai letter is this?"
        2 -> "นี่คือตัวอักษรอะไร?" to "Which letter is this?"
        3, 6 -> "นี่คือตัวเลขอะไร?" to "What number is this?"
        7 -> "นี่คือสัตว์อะไร?" to "What animal is this?"
        8 -> "นี่คือผลไม้อะไร?" to "What fruit is this?"
        9 -> "นี่คือสีอะไร?" to "What color is this?"
        10 -> "นี่คือผักอะไร?" to "What vegetable is this?"
        11 -> "นี่คือกีฬาอะไร?" to "What sport is this?"
        12 -> "นี่คือยานพาหนะอะไร?" to "What vehicle is this?"
        13 -> "นี่คือรูปทรงอะไร?" to "What shape is this?"
        14 -> "นี่คืออาหารอะไร?" to "What food is this?"
        15 -> "นี่คืออวัยวะส่วนใด?" to "What body part is this?"
        16 -> "นี่คือดอกไม้อะไร?" to "What flower is this?"
        17 -> "นี่คือนกอะไร?" to "What bird is this?"
        18 -> "นี่คืออาชีพอะไร?" to "What job is this?"
        99 -> "ท้าทายรวม: รูปนี้คืออะไร?" to "Mixed Challenge: What is this?"
        301 -> "สายฟ้า 60 วิ: รูปนี้คืออะไร?" to "Speed Blitz: What is this?"
        302 -> "นี่คือเสื้อผ้าหรือเครื่องแต่งกายอะไร?" to "What clothing item is this?"
        303 -> "นี่คือของใช้ในบ้านอะไร?" to "What house item is this?"
        304 -> "นี่คืออุปกรณ์การเรียนอะไร?" to "What school item is this?"
        305 -> "นี่คือธงชาติของประเทศอะไร?" to "Which country is this?"
        306 -> "นี่คือเดือนอะไร?" to "Which month is this?"
        307 -> "นี่คือรูปเรขาคณิตอะไร?" to "What geometry shape is this?"
        else -> "รูปนี้คืออะไร?" to "What is this?"
    }
    return when (langMode) {
        QuizLanguageMode.THAI -> th to null
        QuizLanguageMode.ENGLISH -> en to null
        QuizLanguageMode.BILINGUAL -> th to en
    }
}

private fun buildHintClue(
    item: QuizItem,
    langMode: QuizLanguageMode,
    playMode: QuizPlayMode
): String {
    if (playMode == QuizPlayMode.MATH) {
        val ans = item.mathAnswerNum ?: 0
        val low = (ans - 1).coerceAtLeast(0)
        val high = ans + 1
        return if (langMode == QuizLanguageMode.ENGLISH) {
            "Hint: The answer is between $low and $high"
        } else {
            "คำใบ้: คำตอบอยู่ระหว่าง $low ถึง $high ลองนับดูนะ!"
        }
    }

    if (playMode == QuizPlayMode.LISTENING) {
        return when (langMode) {
            QuizLanguageMode.THAI -> "คำใบ้เสียงที่อ่าน: \"${item.thaiText}\""
            QuizLanguageMode.ENGLISH -> "Voice clue: \"${item.englishText}\""
            QuizLanguageMode.BILINGUAL -> "คำใบ้เสียงที่อ่าน: ${item.thaiText} (${item.englishText})"
        }
    }

    fun firstLetterClue(word: String): String {
        val clean = word.trim()
        if (clean.length <= 1) return clean
        val first = clean.first()
        return "\"$first...\" (${clean.length})"
    }

    return when (langMode) {
        QuizLanguageMode.THAI -> {
            val crossHint = if (!item.englishText.equals(item.thaiText, ignoreCase = true)) {
                " • ภาษาอังกฤษ: ${item.englishText}"
            } else ""
            "คำใบ้: ขึ้นต้นด้วย ${firstLetterClue(item.thaiText)}$crossHint"
        }
        QuizLanguageMode.ENGLISH -> {
            val crossHint = if (!item.englishText.equals(item.thaiText, ignoreCase = true)) {
                " • ภาษาไทย: ${item.thaiText}"
            } else ""
            "Hint: Starts with ${firstLetterClue(item.englishText)}$crossHint"
        }
        QuizLanguageMode.BILINGUAL -> {
            "คำใบ้: ขึ้นต้นด้วย ${firstLetterClue(item.thaiText)} / ${firstLetterClue(item.englishText)}"
        }
    }
}

@Composable
private fun QuizSessionScreen(
    topicId: Int,
    topicTitle: String,
    playMode: QuizPlayMode,
    quizList: List<QuizItem>,
    totalQuestions: Int,
    initialLangMode: QuizLanguageMode,
    initialStarBalance: Int,
    onLanguageChanged: (QuizLanguageMode) -> Unit,
    onSpeakItem: (QuizItem, QuizLanguageMode, Boolean) -> Unit,
    onSpeakFeedback: (Boolean, QuizItem, QuizLanguageMode) -> Unit,
    onSpendStars: suspend (Int) -> Pair<Boolean, Int>,
    onQuizFinished: (Int, Long, (QuizResultSummary, Int) -> Unit) -> Unit,
    onSaveLeaderboard: (String, Int, Long, () -> Unit) -> Unit,
    onRestartResetFlag: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var langMode by remember { mutableStateOf(initialLangMode) }
    var slowSpeech by remember { mutableStateOf(false) }
    var starBalance by remember { mutableIntStateOf(initialStarBalance) }
    var starSpendPop by remember { mutableStateOf<Int?>(null) }

    var round by remember { mutableStateOf(buildQuestionRound(1, quizList, playMode, topicId)) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var startTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var remainingSeconds by remember { mutableIntStateOf(60) }

    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var eliminatedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var activeHintText by remember { mutableStateOf<String?>(null) }
    var wasSkippedThisRound by remember { mutableStateOf(false) }

    // 3 Lifelines — each usable 1 time per quiz session
    var usedLifelines by remember { mutableStateOf<Set<QuizLifeline>>(emptySet()) }
    var notEnoughStarsDialog by remember { mutableStateOf<QuizLifeline?>(null) }
    var resultSummary by remember { mutableStateOf<QuizResultSummary?>(null) }
    var isSavingLeaderboard by remember { mutableStateOf(false) }

    val progressFraction by animateFloatAsState(
        targetValue = if (playMode == QuizPlayMode.TIMED) {
            (remainingSeconds.toFloat() / 60f).coerceIn(0f, 1f)
        } else {
            round.questionNumber.toFloat() / totalQuestions.toFloat()
        },
        animationSpec = tween(300),
        label = "quizSessionProgress"
    )

    // 60-second countdown timer for TIMED mode
    LaunchedEffect(playMode, startTimeMs, resultSummary == null) {
        if (playMode == QuizPlayMode.TIMED && resultSummary == null) {
            while (remainingSeconds > 0 && resultSummary == null) {
                delay(1000L)
                remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
            }
            if (remainingSeconds == 0 && resultSummary == null) {
                onQuizFinished(score, 60L) { summary, updatedStars ->
                    starBalance = updatedStars
                    resultSummary = summary
                }
            }
        }
    }

    // Speak new question automatically
    LaunchedEffect(round.questionNumber, round.correctItem.id) {
        delay(260)
        onSpeakItem(round.correctItem, langMode, slowSpeech)
    }

    // Clear floating "-X ⭐" badge after brief display
    LaunchedEffect(starSpendPop) {
        if (starSpendPop != null) {
            delay(1400)
            starSpendPop = null
        }
    }

    fun moveToNextOrFinish() {
        if (round.questionNumber < totalQuestions && (playMode != QuizPlayMode.TIMED || remainingSeconds > 0)) {
            round = buildQuestionRound(round.questionNumber + 1, quizList, playMode, topicId)
            selectedOptionIndex = -1
            answered = false
            eliminatedIndices = emptySet()
            activeHintText = null
            wasSkippedThisRound = false
        } else {
            val durationSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).coerceAtLeast(1L)
            onQuizFinished(score, durationSec) { summary, updatedStars ->
                starBalance = updatedStars
                resultSummary = summary
            }
        }
    }

    fun restartSession() {
        onRestartResetFlag()
        resultSummary = null
        score = 0
        streak = 0
        remainingSeconds = 60
        selectedOptionIndex = -1
        answered = false
        eliminatedIndices = emptySet()
        activeHintText = null
        wasSkippedThisRound = false
        usedLifelines = emptySet()
        startTimeMs = System.currentTimeMillis()
        round = buildQuestionRound(1, quizList, playMode, topicId)
    }

    // Auto-advance smoothly after answering
    LaunchedEffect(answered, round.questionNumber) {
        if (answered) {
            val waitMs = if (selectedOptionIndex == round.correctIndex || wasSkippedThisRound) 1050L else 1550L
            delay(waitMs)
            if (answered) {
                moveToNextOrFinish()
            }
        }
    }

    fun handleOptionClick(index: Int) {
        if (answered || eliminatedIndices.contains(index)) return
        selectedOptionIndex = index
        answered = true
        val isCorrect = index == round.correctIndex
        if (isCorrect) {
            score++
            streak++
        } else {
            streak = 0
        }
        onSpeakFeedback(isCorrect, round.correctItem, langMode)
    }

    fun handleLifelineClick(lifeline: QuizLifeline) {
        if (usedLifelines.contains(lifeline) || answered) return

        if (starBalance < lifeline.starCost) {
            notEnoughStarsDialog = lifeline
            return
        }

        scope.launch {
            val (success, remainingStars) = onSpendStars(lifeline.starCost)
            if (!success) {
                starBalance = remainingStars
                notEnoughStarsDialog = lifeline
                return@launch
            }

            starBalance = remainingStars
            starSpendPop = lifeline.starCost
            usedLifelines = usedLifelines + lifeline

            when (lifeline) {
                QuizLifeline.FIFTY_FIFTY -> {
                    val wrongIndices = (0..3).filter { it != round.correctIndex && !eliminatedIndices.contains(it) }
                    eliminatedIndices = eliminatedIndices + wrongIndices.shuffled().take(2)
                }
                QuizLifeline.HINT -> {
                    activeHintText = buildHintClue(round.correctItem, langMode, playMode)
                    onSpeakItem(round.correctItem, langMode, slowSpeech)
                }
                QuizLifeline.SKIP_PASS -> {
                    wasSkippedThisRound = true
                    selectedOptionIndex = round.correctIndex
                    answered = true
                    score++
                    streak++
                    onSpeakFeedback(true, round.correctItem, langMode)
                }
            }
        }
    }

    val (primaryPrompt, secondaryPrompt) = getQuestionPrompt(topicId, langMode, playMode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE6F4FE), Color(0xFFF5FAFF), Color(0xFFFFFDF7))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Clean Top Bar: Back + Title + Real Star Wallet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBack)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "ย้อนกลับ",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Text(
                text = topicTitle,
                color = Color(0xFF0F172A),
                fontSize = 17.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            // Real Star Wallet + animated "-X" pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedVisibility(
                    visible = starSpendPop != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444)
                    ) {
                        Text(
                            text = "-${starSpendPop ?: 0} ⭐",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF8E1),
                    border = BorderStroke(1.2.dp, Color(0xFFFCD34D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "ดาวสะสม",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "$starBalance",
                            color = Color(0xFFB45309),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 2. Language Switcher + Question Progress Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Language Segmented Pill (ไทย | EN | ไทย+EN)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    QuizLanguageMode.entries.forEach { mode ->
                        val selected = langMode == mode
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = if (selected) Color(0xFF0284C7) else Color.Transparent,
                            modifier = Modifier.clickable {
                                if (langMode != mode) {
                                    langMode = mode
                                    onLanguageChanged(mode)
                                    if (activeHintText != null) {
                                        activeHintText = buildHintClue(round.correctItem, mode, playMode)
                                    }
                                    onSpeakItem(round.correctItem, mode, slowSpeech)
                                }
                            }
                        ) {
                            Text(
                                text = mode.label,
                                color = if (selected) Color.White else Color(0xFF475569),
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Question Counter + Score + Streak (+ Countdown Timer for TIMED mode)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (playMode == QuizPlayMode.TIMED) {
                    val urgentBg = when {
                        remainingSeconds <= 10 -> Color(0xFFFEE2E2)
                        remainingSeconds <= 20 -> Color(0xFFFFEDD5)
                        else -> Color(0xFFE0F2FE)
                    }
                    val urgentColor = when {
                        remainingSeconds <= 10 -> Color(0xFFDC2626)
                        remainingSeconds <= 20 -> Color(0xFFEA580C)
                        else -> Color(0xFF0284C7)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = urgentBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = urgentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${remainingSeconds}s",
                                color = urgentColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else if (streak >= 2) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEDD5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "$streak",
                                color = Color(0xFFC2410C),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = if (langMode == QuizLanguageMode.ENGLISH) "Score $score" else "ได้ $score คะแนน",
                        color = Color(0xFF15803D),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${round.questionNumber}/$totalQuestions",
                    color = Color(0xFF334155),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Smooth Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (playMode == QuizPlayMode.TIMED) Color(0xFFFFEDD5) else Color(0xFFDBEAFE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (playMode == QuizPlayMode.TIMED) {
                                listOf(Color(0xFFF97316), Color(0xFFEF4444))
                            } else {
                                listOf(Color(0xFF0EA5E9), Color(0xFF6366F1))
                            }
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Question Card Stage (adapts cleanly for Standard, Listening, Math, and Timed)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(
                1.2.dp,
                when (playMode) {
                    QuizPlayMode.LISTENING -> Color(0xFF99F6E4)
                    QuizPlayMode.MATH -> Color(0xFFBBF7D0)
                    QuizPlayMode.TIMED -> Color(0xFFFED7AA)
                    QuizPlayMode.STANDARD -> Color(0xFFBAE6FD)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (playMode == QuizPlayMode.LISTENING) 0.78f else 1.05f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Question Title
                Text(
                    text = primaryPrompt,
                    color = Color(0xFF0F172A),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (secondaryPrompt != null) {
                    Text(
                        text = secondaryPrompt,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (playMode) {
                    QuizPlayMode.LISTENING -> {
                        ListeningQuestionStage(
                            langMode = langMode,
                            slowSpeech = slowSpeech,
                            onToggleSlow = {
                                slowSpeech = !slowSpeech
                                onSpeakItem(round.correctItem, langMode, slowSpeech)
                            },
                            onReplay = { onSpeakItem(round.correctItem, langMode, slowSpeech) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                    QuizPlayMode.MATH -> {
                        MathQuestionStage(
                            item = round.correctItem,
                            langMode = langMode,
                            onSpeak = { onSpeakItem(round.correctItem, langMode, slowSpeech) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                    else -> {
                        // Standard & Timed Illustration Box (tap to speak)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { onSpeakItem(round.correctItem, langMode, slowSpeech) },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = round.correctItem.imageRes),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )

                            // Floating Audio Speaker Pill inside bottom-right of image stage
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0284C7),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "ฟังเสียง",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (langMode == QuizLanguageMode.ENGLISH) "Listen" else "ฟังเสียง",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Hint Banner (shown when Hint lifeline is used on this question)
                AnimatedVisibility(
                    visible = activeHintText != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = activeHintText.orEmpty(),
                                color = Color(0xFF92400E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Three Lifelines Bar (ตัวช่วย 3 อย่าง • 1 ครั้ง/หมวด • เสียดาว)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuizLifeline.entries.forEach { lifeline ->
                val isUsed = usedLifelines.contains(lifeline)
                val isEnabled = !isUsed && !answered
                LifelineButton(
                    lifeline = lifeline,
                    langMode = langMode,
                    isUsed = isUsed,
                    isEnabled = isEnabled,
                    hasEnoughStars = starBalance >= lifeline.starCost,
                    modifier = Modifier.weight(1f),
                    onClick = { handleLifelineClick(lifeline) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Four Answer Choices (2x2 Grid — shows pictures in Listening mode, text in other modes)
        val isPictureChoiceMode = playMode == QuizPlayMode.LISTENING
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isPictureChoiceMode) 1.22f else 0.95f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuizAnswerCard(
                    badgeLetter = "A",
                    badgeColor = Color(0xFFEF4444),
                    item = round.options[0],
                    langMode = langMode,
                    isPictureChoiceMode = isPictureChoiceMode,
                    isEliminated = eliminatedIndices.contains(0),
                    answered = answered,
                    isSelected = selectedOptionIndex == 0,
                    isCorrect = round.correctIndex == 0,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleOptionClick(0) }
                )
                QuizAnswerCard(
                    badgeLetter = "B",
                    badgeColor = Color(0xFF0EA5E9),
                    item = round.options[1],
                    langMode = langMode,
                    isPictureChoiceMode = isPictureChoiceMode,
                    isEliminated = eliminatedIndices.contains(1),
                    answered = answered,
                    isSelected = selectedOptionIndex == 1,
                    isCorrect = round.correctIndex == 1,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleOptionClick(1) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuizAnswerCard(
                    badgeLetter = "C",
                    badgeColor = Color(0xFF10B981),
                    item = round.options[2],
                    langMode = langMode,
                    isPictureChoiceMode = isPictureChoiceMode,
                    isEliminated = eliminatedIndices.contains(2),
                    answered = answered,
                    isSelected = selectedOptionIndex == 2,
                    isCorrect = round.correctIndex == 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleOptionClick(2) }
                )
                QuizAnswerCard(
                    badgeLetter = "D",
                    badgeColor = Color(0xFFF59E0B),
                    item = round.options[3],
                    langMode = langMode,
                    isPictureChoiceMode = isPictureChoiceMode,
                    isEliminated = eliminatedIndices.contains(3),
                    answered = answered,
                    isSelected = selectedOptionIndex == 3,
                    isCorrect = round.correctIndex == 3,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { handleOptionClick(3) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Bottom Next / Finish Button
        Button(
            onClick = {
                if (answered) {
                    moveToNextOrFinish()
                }
            },
            enabled = answered,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                disabledContainerColor = Color(0xFFCBD5E1),
                contentColor = Color.White,
                disabledContentColor = Color(0xFF64748B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(50.dp)
        ) {
            val isLast = round.questionNumber >= totalQuestions
            val btnLabel = when {
                !answered && langMode == QuizLanguageMode.ENGLISH -> "Choose an answer"
                !answered -> "เลือกคำตอบด้านบน"
                isLast && langMode == QuizLanguageMode.ENGLISH -> "View Results"
                isLast -> "ดูผลคะแนน"
                langMode == QuizLanguageMode.ENGLISH -> "Next Question"
                else -> "ข้อถัดไป"
            }
            Text(
                text = btnLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            if (answered) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }

    // Not Enough Stars Dialog
    notEnoughStarsDialog?.let { lifeline ->
        val isEn = langMode == QuizLanguageMode.ENGLISH
        AlertDialog(
            onDismissRequest = { notEnoughStarsDialog = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF8E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isEn) "Not Enough Stars" else "ดาวสะสมไม่พอ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "Using ${lifeline.engLabel} requires ${lifeline.starCost} stars, but you currently have $starBalance stars. Finish quizzes or tracing lessons to earn more!"
                    } else {
                        "การใช้ตัวช่วย \"${lifeline.thaiLabel}\" ต้องใช้ ${lifeline.starCost} ดาว แต่ตอนนี้คุณมี $starBalance ดาว ทำแบบทดสอบหรือฝึกเขียนเพื่อสะสมดาวเพิ่มนะ"
                    },
                    color = Color(0xFF475569),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { notEnoughStarsDialog = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(if (isEn) "Got it" else "เข้าใจแล้ว", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Quiz Result Summary Dialog
    resultSummary?.let { summary ->
        var playerName by remember(summary) { mutableStateOf(summary.defaultPlayerName) }
        val accuracy = (summary.score * 100) / summary.totalQuestions.coerceAtLeast(1)
        val starsEarned = QuizProgressManager.getStarRating(summary.score, summary.totalQuestions)
        val isEn = langMode == QuizLanguageMode.ENGLISH

        val praiseText = when {
            accuracy >= 80 -> if (isEn) "Awesome job!" else "ยอดเยี่ยมมาก!"
            accuracy >= 50 -> if (isEn) "Well done!" else "ทำได้ดีมาก!"
            else -> if (isEn) "Keep practicing!" else "พยายามต่อไปนะ!"
        }

        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(26.dp),
            containerColor = Color.White,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { idx ->
                            Icon(
                                imageVector = if (idx < starsEarned) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = null,
                                tint = if (idx < starsEarned) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = praiseText,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ResultStatCol(
                                label = if (isEn) "Score" else "คะแนน",
                                value = "${summary.score}/${summary.totalQuestions}",
                                valueColor = Color(0xFF0284C7)
                            )
                            ResultStatCol(
                                label = if (isEn) "Accuracy" else "ความแม่นยำ",
                                value = "$accuracy%",
                                valueColor = Color(0xFF16A34A)
                            )
                            ResultStatCol(
                                label = if (isEn) "Time" else "เวลา",
                                value = "${summary.durationSeconds}s",
                                valueColor = Color(0xFFD97706)
                            )
                        }
                    }

                    Text(
                        text = if (isEn) "Earned +${summary.score} stars!" else "ได้รับ +${summary.score} ดาวสะสม!",
                        color = Color(0xFFB45309),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        singleLine = true,
                        label = {
                            Text(
                                text = if (summary.isRegisteredMember) {
                                    if (isEn) "Player Name" else "ชื่อสมาชิก"
                                } else {
                                    if (isEn) "Guest Name" else "ชื่อผู้เล่น"
                                }
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSavingLeaderboard,
                    onClick = {
                        isSavingLeaderboard = true
                        val cleanName = playerName.trim().ifEmpty { summary.defaultPlayerName }
                        onSaveLeaderboard(cleanName, summary.score, summary.durationSeconds) {
                            isSavingLeaderboard = false
                            restartSession()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(
                        text = if (isSavingLeaderboard) "..." else if (isEn) "Save & Replay" else "บันทึก & เล่นใหม่",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { restartSession() },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (isEn) "Replay" else "เล่นใหม่")
                    }
                    TextButton(onClick = onBack) {
                        Text(if (isEn) "Exit" else "ออก", color = Color(0xFF64748B))
                    }
                }
            }
        )
    }
}

@Composable
private fun LifelineButton(
    lifeline: QuizLifeline,
    langMode: QuizLanguageMode,
    isUsed: Boolean,
    isEnabled: Boolean,
    hasEnoughStars: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "lifelineScale"
    )

    val label = if (langMode == QuizLanguageMode.ENGLISH) lifeline.engLabel else lifeline.thaiLabel
    val bgColor = when {
        isUsed -> Color(0xFFF1F5F9)
        !hasEnoughStars -> Color(0xFFF8FAFC)
        else -> lifeline.bgColor
    }
    val borderColor = when {
        isUsed -> Color(0xFFE2E8F0)
        !hasEnoughStars -> Color(0xFFCBD5E1)
        else -> lifeline.accentColor.copy(alpha = 0.45f)
    }
    val contentTint = if (isUsed || !hasEnoughStars) Color(0xFF94A3B8) else lifeline.accentColor

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.2.dp, borderColor),
        shadowElevation = if (isEnabled && hasEnoughStars) 2.dp else 0.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = lifeline.icon,
                contentDescription = label,
                tint = contentTint,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = label,
                    color = if (isUsed) Color(0xFF94A3B8) else Color(0xFF1E293B),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isUsed) {
                        if (langMode == QuizLanguageMode.ENGLISH) "Used" else "ใช้แล้ว"
                    } else {
                        "-${lifeline.starCost} ⭐"
                    },
                    color = if (isUsed) Color(0xFF94A3B8) else contentTint,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ListeningQuestionStage(
    langMode: QuizLanguageMode,
    slowSpeech: Boolean,
    onToggleSlow: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "listenPulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakerPulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF0FDFA), Color(0xFFE0F2FE))
                )
            )
            .clickable(onClick = onReplay)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(CircleShape)
                    .background(Color(0xFF0D9488).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0D9488), Color(0xFF0284C7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Headphones,
                        contentDescription = "ฟังเสียง",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (langMode == QuizLanguageMode.ENGLISH) "Tap to hear again" else "แตะตรงนี้เพื่อฟังเสียงอีกครั้ง",
                color = Color(0xFF0F766E),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Slow Speech Toggle Chip (top-right)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (slowSpeech) Color(0xFF0D9488) else Color.White,
            border = BorderStroke(1.dp, if (slowSpeech) Color(0xFF0D9488) else Color(0xFF99F6E4)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(onClick = onToggleSlow)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SlowMotionVideo,
                    contentDescription = null,
                    tint = if (slowSpeech) Color.White else Color(0xFF0F766E),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (langMode == QuizLanguageMode.ENGLISH) "Slow" else "ช้าลง",
                    color = if (slowSpeech) Color.White else Color(0xFF0F766E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MathQuestionStage(
    item: QuizItem,
    langMode: QuizLanguageMode,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF0FDF4), Color(0xFFFEFCE8))
                )
            )
            .clickable(onClick = onSpeak)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Visual Counting Emojis
            val groupA = item.mathEmojiGroupA.orEmpty()
            val groupB = item.mathEmojiGroupB
            val op = item.mathOperator

            when (op) {
                "+" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Text(
                                text = groupA,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Text(
                            text = "+",
                            color = Color(0xFF16A34A),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = groupB.orEmpty(),
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                "−" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Text(
                                text = groupA,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEE2E2).copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Text(
                                text = groupB.orEmpty(),
                                fontSize = 22.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = Color(0xFFDC2626),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .alpha(0.55f)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Text(
                            text = groupA,
                            fontSize = if (groupA.length > 10) 24.sp else 28.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Equation Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF16A34A)
            ) {
                Text(
                    text = item.mathEquation ?: "?",
                    color = Color.White,
                    fontSize = if (op != null) 28.sp else 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }

        // Listen Pill at bottom-right
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0284C7),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "ฟังโจทย์",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (langMode == QuizLanguageMode.ENGLISH) "Read" else "อ่านโจทย์",
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuizAnswerCard(
    badgeLetter: String,
    badgeColor: Color,
    item: QuizItem,
    langMode: QuizLanguageMode,
    isPictureChoiceMode: Boolean = false,
    isEliminated: Boolean,
    answered: Boolean,
    isSelected: Boolean,
    isCorrect: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !answered && !isEliminated) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "answerScale"
    )

    val showCorrectState = answered && isCorrect
    val showWrongState = answered && isSelected && !isCorrect

    val cardBg by animateColorAsState(
        targetValue = when {
            showCorrectState -> Color(0xFFDCFCE7)
            showWrongState -> Color(0xFFFEE2E2)
            isEliminated -> Color(0xFFF1F5F9)
            else -> Color.White
        },
        label = "answerBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            showCorrectState -> Color(0xFF16A34A)
            showWrongState -> Color(0xFFEF4444)
            isEliminated -> Color(0xFFE2E8F0)
            else -> badgeColor.copy(alpha = 0.35f)
        },
        label = "answerBorder"
    )
    val lipColor = when {
        showCorrectState -> Color(0xFF16A34A).copy(alpha = 0.35f)
        showWrongState -> Color(0xFFEF4444).copy(alpha = 0.35f)
        isEliminated -> Color(0xFFE2E8F0)
        else -> badgeColor.copy(alpha = 0.25f)
    }

    val (primaryText, secondaryText) = when (langMode) {
        QuizLanguageMode.THAI -> item.thaiText to null
        QuizLanguageMode.ENGLISH -> item.englishText to null
        QuizLanguageMode.BILINGUAL -> {
            if (item.thaiText.equals(item.englishText, ignoreCase = true)) {
                item.thaiText to null
            } else {
                item.thaiText to item.englishText
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(if (isEliminated && !answered) 0.35f else 1f)
            .clip(RoundedCornerShape(20.dp))
            .background(lipColor)
            .padding(bottom = 3.5.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(
                width = if (showCorrectState || showWrongState) 2.dp else 1.2.dp,
                color = borderColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isEliminated) 0.dp else 1.5.dp),
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = !answered && !isEliminated,
                    onClick = onClick
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (isPictureChoiceMode && item.imageRes != 0) {
                    // Picture Choice Card (Listening Mode)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = primaryText,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        if (answered) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    showCorrectState -> Color(0xFF16A34A)
                                    showWrongState -> Color(0xFFEF4444)
                                    else -> Color(0xFFE2E8F0)
                                }
                            ) {
                                Text(
                                    text = primaryText,
                                    color = if (showCorrectState || showWrongState) Color.White else Color(0xFF334155),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Text Choice Card (Standard, Math, Timed)
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 12.dp, start = 4.dp, end = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val primaryFontSize = when {
                            item.mathAnswerNum != null && langMode == QuizLanguageMode.ENGLISH -> 30.sp
                            primaryText.length <= 2 -> 28.sp
                            primaryText.length <= 8 -> 19.sp
                            primaryText.length <= 14 -> 16.sp
                            else -> 14.sp
                        }

                        Text(
                            text = primaryText,
                            color = when {
                                showCorrectState -> Color(0xFF14532D)
                                showWrongState -> Color(0xFF991B1B)
                                isEliminated -> Color(0xFF94A3B8)
                                else -> Color(0xFF0F172A)
                            },
                            fontSize = primaryFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            textDecoration = if (isEliminated && !answered) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = (primaryFontSize.value + 4).sp
                        )

                        if (secondaryText != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = secondaryText,
                                color = when {
                                    showCorrectState -> Color(0xFF15803D)
                                    showWrongState -> Color(0xFFB91C1C)
                                    else -> Color(0xFF64748B)
                                },
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                textDecoration = if (isEliminated && !answered) TextDecoration.LineThrough else null,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Top-left A/B/C/D badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                showCorrectState -> Color(0xFF16A34A)
                                showWrongState -> Color(0xFFEF4444)
                                isEliminated -> Color(0xFFCBD5E1)
                                else -> badgeColor
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeLetter,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Top-right Check / Close result icon
                if (showCorrectState || showWrongState) {
                    Icon(
                        imageVector = if (showCorrectState) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = if (showCorrectState) Color(0xFF16A34A) else Color(0xFFEF4444),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultStatCol(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 11.5.sp
        )
    }
}
