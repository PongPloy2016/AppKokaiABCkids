package com.pongploydev.priyankvex.paintapp.activity.thailetters

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.learning.FunTracingActivity
import com.pongploydev.priyankvex.paintapp.compose.AlphabetMenuComposeScreen
import com.pongploydev.priyankvex.paintapp.compose.AlphabetMode
import com.pongploydev.priyankvex.paintapp.compose.LanguageHubCategory
import com.pongploydev.priyankvex.paintapp.compose.LanguagePortalComposeScreen
import com.pongploydev.priyankvex.paintapp.compose.SubMenuComposeScreen
import com.pongploydev.priyankvex.paintapp.compose.VideoWatchFeatureTheme
import com.pongploydev.priyankvex.paintapp.model.AlphabetCardItem
import com.pongploydev.priyankvex.paintapp.model.ThaiAlphabetRepository
import com.pongploydev.priyankvex.paintapp.utils.MusicManager
import java.util.Locale
import kotlin.random.Random

enum class HubNavState {
    PORTAL,        // หน้าหลักเลือกภาษา (ไทย / อังกฤษ)
    THAI_SUBMENU,  // หน้าเมนูเลือกหมวดภาษาไทย (๘ หมวด)
    ENGLISH_SUBMENU, // หน้าเมนูเลือกหมวดภาษาอังกฤษ (๔ หมวด)
    LEARNING_SCREEN // หน้าจอการเรียนรู้หลัก (Grid + Tabs + AutoRead + Quiz)
}

class MenuThaiLettersActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    // Hub Navigation State
    private var currentNavState by mutableStateOf(HubNavState.PORTAL)
    private var currentLanguageCategory by mutableStateOf(LanguageHubCategory.THAI)
    private var currentActiveMode by mutableStateOf(AlphabetMode.THAI)
    private var isDirectFromMain = false

    // Auto Read State
    private var isAutoReading by mutableStateOf(false)
    private var currentAutoReadIndex by mutableIntStateOf(-1)

    // Interactive Listening Quiz State
    private var isQuizMode by mutableStateOf(false)
    private var quizScore by mutableIntStateOf(0)
    private var quizTargetItem by mutableStateOf<AlphabetCardItem?>(null)
    private var lastCorrectClickedId by mutableIntStateOf(-1)
    private var lastWrongClickedId by mutableIntStateOf(-1)

    private val autoReadHandler = Handler(Looper.getMainLooper())
    private var autoReadRunnable: Runnable? = null

    private val thaiItems by lazy { ThaiAlphabetRepository.getThaiAlphabetList() }
    private val vowelItems by lazy { ThaiAlphabetRepository.getThaiVowelList() }
    private val numberItems by lazy { ThaiAlphabetRepository.getNumbersList() }
    private val engNumberItems by lazy { ThaiAlphabetRepository.getEnglishNumbersList() }
    private val toneGroupItems by lazy { ThaiAlphabetRepository.getToneGroupsList() }
    private val colorDayItems by lazy { ThaiAlphabetRepository.getColorsAndDaysList() }
    private val vehicleItems by lazy { ThaiAlphabetRepository.getVehiclesList() }
    private val bodyPartItems by lazy { ThaiAlphabetRepository.getBodyPartsList() }
    private val spellingItems by lazy { ThaiAlphabetRepository.getSpellingList() }
    private val animalItems by lazy { ThaiAlphabetRepository.getAnimalsList() }
    private val fruitItems by lazy { ThaiAlphabetRepository.getFruitsList() }
    private val engItems by lazy { ThaiAlphabetRepository.getEnglishAlphabetList() }

    companion object {
        private const val TAG = "MenuThaiLettersActivity"
        private const val AUTO_READ_DELAY = 3200L // 3.2 seconds per letter
        const val EXTRA_MODE = "EXTRA_MODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modeExtra = intent.getStringExtra(EXTRA_MODE)
        when (modeExtra) {
            "THAI" -> {
                isDirectFromMain = true
                currentNavState = HubNavState.THAI_SUBMENU
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.THAI
            }
            "ENGLISH" -> {
                isDirectFromMain = true
                currentNavState = HubNavState.ENGLISH_SUBMENU
                currentLanguageCategory = LanguageHubCategory.ENGLISH
                currentActiveMode = AlphabetMode.ENGLISH
            }
            "VOWELS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.THAI_VOWELS
            }
            "NUMBERS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.NUMBERS
            }
            "ANIMALS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.ENGLISH
                currentActiveMode = AlphabetMode.ANIMALS
            }
            "FRUITS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.ENGLISH
                currentActiveMode = AlphabetMode.FRUITS
            }
            "TONE_GROUPS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.TONE_GROUPS
            }
            "COLORS_DAYS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.COLORS_DAYS
            }
            "VEHICLES" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.VEHICLES
            }
            "BODY_PARTS" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.BODY_PARTS
            }
            "SPELLING" -> {
                currentNavState = HubNavState.LEARNING_SCREEN
                currentLanguageCategory = LanguageHubCategory.THAI
                currentActiveMode = AlphabetMode.SPELLING
            }
            else -> {
                currentNavState = HubNavState.PORTAL
            }
        }

        setupTextToSpeech()

        setContent {
            VideoWatchFeatureTheme {
                when (currentNavState) {
                    HubNavState.PORTAL -> {
                        LanguagePortalComposeScreen(
                            onSelectThaiHub = {
                                currentLanguageCategory = LanguageHubCategory.THAI
                                currentActiveMode = AlphabetMode.THAI
                                currentNavState = HubNavState.THAI_SUBMENU
                            },
                            onSelectEnglishHub = {
                                currentLanguageCategory = LanguageHubCategory.ENGLISH
                                currentActiveMode = AlphabetMode.ENGLISH
                                currentNavState = HubNavState.ENGLISH_SUBMENU
                            },
                            onBackClick = { finish() }
                        )
                    }

                    HubNavState.THAI_SUBMENU -> {
                        BackHandler {
                            if (isDirectFromMain) finish() else currentNavState = HubNavState.PORTAL
                        }

                        SubMenuComposeScreen(
                            languageCategory = LanguageHubCategory.THAI,
                            onSelectMode = { selectedMode ->
                                currentActiveMode = selectedMode
                                currentNavState = HubNavState.LEARNING_SCREEN
                            },
                            onBackClick = {
                                if (isDirectFromMain) finish() else currentNavState = HubNavState.PORTAL
                            }
                        )
                    }

                    HubNavState.ENGLISH_SUBMENU -> {
                        BackHandler {
                            if (isDirectFromMain) finish() else currentNavState = HubNavState.PORTAL
                        }

                        SubMenuComposeScreen(
                            languageCategory = LanguageHubCategory.ENGLISH,
                            onSelectMode = { selectedMode ->
                                currentActiveMode = selectedMode
                                currentNavState = HubNavState.LEARNING_SCREEN
                            },
                            onBackClick = {
                                if (isDirectFromMain) finish() else currentNavState = HubNavState.PORTAL
                            }
                        )
                    }

                    HubNavState.LEARNING_SCREEN -> {
                        BackHandler {
                            stopAutoRead()
                            stopQuiz()
                            currentNavState = if (currentLanguageCategory == LanguageHubCategory.THAI) {
                                HubNavState.THAI_SUBMENU
                            } else {
                                HubNavState.ENGLISH_SUBMENU
                            }
                        }

                        AlphabetMenuComposeScreen(
                            languageCategory = currentLanguageCategory,
                            initialMode = currentActiveMode,
                            isAutoReading = isAutoReading,
                            currentAutoReadIndex = currentAutoReadIndex,
                            isQuizMode = isQuizMode,
                            quizScore = quizScore,
                            lastCorrectClickedId = lastCorrectClickedId,
                            lastWrongClickedId = lastWrongClickedId,
                            onStartAutoRead = { mode ->
                                if (isQuizMode) stopQuiz()
                                currentActiveMode = mode
                                startAutoRead(mode)
                            },
                            onStopAutoRead = { stopAutoRead() },
                            onStartQuiz = { mode ->
                                if (isAutoReading) stopAutoRead()
                                currentActiveMode = mode
                                startQuiz(mode)
                            },
                            onStopQuiz = { stopQuiz() },
                            onRepeatQuizSound = { repeatQuizSound() },
                            onCardClick = { item, _ ->
                                if (isQuizMode) {
                                    checkQuizAnswer(item)
                                } else if (!isAutoReading) {
                                    playSoundForItem(item)
                                }
                            },

                            onCardLongClick = { item, position ->
                                if (!isQuizMode && !isAutoReading) {
                                    playSoundForItem(item)
                                    openWritingScreen(item, position)
                                }
                            },
                            onPracticeWriteClick = { item, position ->
                                if (!isQuizMode && !isAutoReading) {
                                    openWritingScreen(item, position)
                                }
                            },
                            onSoundClick = { item, _ ->
                                if (!isQuizMode && !isAutoReading) {
                                    playSoundForItem(item)
                                }
                            },
                            onBackClick = {
                                stopAutoRead()
                                stopQuiz()
                                currentNavState = if (currentLanguageCategory == LanguageHubCategory.THAI) {
                                    HubNavState.THAI_SUBMENU
                                } else {
                                    HubNavState.ENGLISH_SUBMENU
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            MusicManager.getInstance().startMusic(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting background music", e)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isAutoReading) {
            stopAutoRead()
        }
        if (isQuizMode) {
            stopQuiz()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoRead()
        stopQuiz()
        releaseMediaPlayer()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("th", "TH"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Thai TTS language not supported, fallback to default")
                    textToSpeech?.language = Locale.getDefault()
                }
                isTtsReady = true
            } else {
                Log.e(TAG, "TTS Initialization failed")
                isTtsReady = false
            }
        }
    }

    // ── AUTO READ SYSTEM ──
    private fun startAutoRead(mode: AlphabetMode) {
        if (isAutoReading) return

        isAutoReading = true
        var readIndex = 0

        autoReadRunnable = object : Runnable {
            override fun run() {
                val items = when (mode) {
                    AlphabetMode.THAI -> thaiItems
                    AlphabetMode.THAI_VOWELS -> vowelItems
                    AlphabetMode.TONE_GROUPS -> toneGroupItems
                    AlphabetMode.NUMBERS -> if (currentLanguageCategory == LanguageHubCategory.ENGLISH) engNumberItems else numberItems
                    AlphabetMode.COLORS_DAYS -> colorDayItems
                    AlphabetMode.SPELLING -> spellingItems
                    AlphabetMode.VEHICLES -> vehicleItems
                    AlphabetMode.BODY_PARTS -> bodyPartItems
                    AlphabetMode.ANIMALS -> animalItems
                    AlphabetMode.FRUITS -> fruitItems
                    AlphabetMode.ENGLISH -> engItems
                }
                if (!isAutoReading) return

                if (readIndex < items.size) {
                    currentAutoReadIndex = readIndex
                    val currentItem = items[readIndex]
                    readItem(currentItem)
                    readIndex++

                    if (readIndex < items.size) {
                        autoReadHandler.postDelayed(this, AUTO_READ_DELAY)
                    } else {
                        autoReadHandler.postDelayed({
                            if (isAutoReading) {
                                stopAutoRead()
                                Toast.makeText(
                                    this@MenuThaiLettersActivity,
                                    if (currentLanguageCategory == LanguageHubCategory.ENGLISH) "🎉 Great job! Completed all items!" else "🎉 เก่งมาก! อ่านครบทุกตัวแล้ว",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, AUTO_READ_DELAY)
                    }
                } else {
                    stopAutoRead()
                }
            }
        }

        autoReadHandler.post(autoReadRunnable!!)
    }

    private fun stopAutoRead() {
        isAutoReading = false
        currentAutoReadIndex = -1

        autoReadRunnable?.let {
            autoReadHandler.removeCallbacks(it)
        }
        autoReadRunnable = null

        textToSpeech?.stop()
        releaseMediaPlayer()
    }

    // ── QUIZ / RANDOM LISTENING GAME ──
    private fun startQuiz(mode: AlphabetMode) {
        isQuizMode = true
        quizScore = 0
        lastCorrectClickedId = -1
        lastWrongClickedId = -1
        currentActiveMode = mode

        pickNextQuizTarget(mode)
    }

    private fun pickNextQuizTarget(mode: AlphabetMode) {
        if (!isQuizMode) return

        lastCorrectClickedId = -1
        lastWrongClickedId = -1

        val items = when (mode) {
            AlphabetMode.THAI -> thaiItems
            AlphabetMode.THAI_VOWELS -> vowelItems
            AlphabetMode.TONE_GROUPS -> toneGroupItems
            AlphabetMode.NUMBERS -> if (currentLanguageCategory == LanguageHubCategory.ENGLISH) engNumberItems else numberItems
            AlphabetMode.COLORS_DAYS -> colorDayItems
            AlphabetMode.SPELLING -> spellingItems
            AlphabetMode.VEHICLES -> vehicleItems
            AlphabetMode.BODY_PARTS -> bodyPartItems
            AlphabetMode.ANIMALS -> animalItems
            AlphabetMode.FRUITS -> fruitItems
            AlphabetMode.ENGLISH -> engItems
        }

        if (items.isEmpty()) return

        val target = items[Random.nextInt(items.size)]
        quizTargetItem = target

        autoReadHandler.postDelayed({
            if (isQuizMode && quizTargetItem != null) {
                playSoundForItem(target)
            }
        }, 500)
    }

    private fun repeatQuizSound() {
        quizTargetItem?.let { item ->
            playSoundForItem(item)
        }
    }

    private fun checkQuizAnswer(clickedItem: AlphabetCardItem) {
        val target = quizTargetItem ?: return

        val isEnglish = (currentLanguageCategory == LanguageHubCategory.ENGLISH)

        if (clickedItem.id == target.id) {
            // ✅ CORRECT ANSWER!
            lastCorrectClickedId = clickedItem.id
            lastWrongClickedId = -1
            quizScore++

            playRawAudio(R.raw.chiotrue)
            speakTts(if (isEnglish) "Great job!" else "เก่งมากค่ะ!", isEnglish = isEnglish)

            autoReadHandler.postDelayed({
                if (isQuizMode) {
                    pickNextQuizTarget(currentActiveMode)
                }
            }, 1600)
        } else {
            // ❌ WRONG ANSWER!
            lastWrongClickedId = clickedItem.id
            lastCorrectClickedId = -1

            playRawAudio(R.raw.chiofalse)
            speakTts(if (isEnglish) "Try again please" else "ช่วยลองใหม่อีกครั้งค่ะ", isEnglish = isEnglish)
        }
    }

    private fun stopQuiz() {
        isQuizMode = false
        quizTargetItem = null
        lastCorrectClickedId = -1
        lastWrongClickedId = -1
        textToSpeech?.stop()
        releaseMediaPlayer()
    }

    private fun readItem(item: AlphabetCardItem) {
        playSoundForItem(item)
    }

    private fun openWritingScreen(item: AlphabetCardItem, position: Int) {
        val intent = Intent(this, FunTracingActivity::class.java).apply {
            putExtra("symbol", item.letter)
            putExtra("name", item.name)
            putExtra("thaiName", item.name)
            putExtra("rhyme", item.rhyme)
            putExtra("imageRes", item.imageRes)
            putExtra("audioRawRes", item.audioRawRes)
            putExtra("soundResName", item.soundResName)
            putExtra("categoryMode", currentActiveMode.name)
            putExtra("categoryPosition", if (currentLanguageCategory == LanguageHubCategory.ENGLISH) 0 else 1)
            putExtra("selectedPosition", position)
        }
        startActivity(intent)
    }

    private fun playSoundForItem(item: AlphabetCardItem) {
        try {
            var playedSound = false

            if (item.audioRawRes != 0) {
                playRawAudio(item.audioRawRes)
                playedSound = true
            } else if (item.soundResName.isNotEmpty()) {
                val resId = resources.getIdentifier(item.soundResName, "raw", packageName)
                if (resId != 0) {
                    playRawAudio(resId)
                    playedSound = true
                }
            }

            if (!playedSound) {
                val isEnglishHub = (currentLanguageCategory == LanguageHubCategory.ENGLISH)
                val textToSpeak = if (isEnglishHub && (currentActiveMode == AlphabetMode.ANIMALS || currentActiveMode == AlphabetMode.FRUITS || currentActiveMode == AlphabetMode.ENGLISH)) {
                    item.letter
                } else {
                    item.rhyme
                }
                speakTts(textToSpeak, isEnglish = isEnglishHub)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound for item: ${item.name}", e)
            speakTts(item.name)
        }
    }

    private fun speakTts(text: String, isEnglish: Boolean = false) {
        try {
            if (isTtsReady && textToSpeech != null) {
                if (isEnglish) {
                    textToSpeech?.language = Locale.US
                } else {
                    textToSpeech?.language = Locale("th", "TH")
                }
                textToSpeech?.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "AlphabetTTS"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak error", e)
        }
    }

    private fun playRawAudio(rawResId: Int) {
        try {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer.create(applicationContext, rawResId)?.apply {
                setOnCompletionListener {
                    releaseMediaPlayer()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio resId: $rawResId", e)
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
        }
    }
}
