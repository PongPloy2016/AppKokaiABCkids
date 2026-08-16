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
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.compose.AlphabetMenuComposeScreen
import com.pongploydev.priyankvex.paintapp.compose.AlphabetMode
import com.pongploydev.priyankvex.paintapp.compose.VideoWatchFeatureTheme
import com.pongploydev.priyankvex.paintapp.model.AlphabetCardItem
import com.pongploydev.priyankvex.paintapp.model.ThaiAlphabetRepository
import com.pongploydev.priyankvex.paintapp.utils.MusicManager
import java.util.Locale
import kotlin.random.Random

class MenuThaiLettersActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    // Auto Read State
    private var isAutoReading by mutableStateOf(false)
    private var currentAutoReadIndex by mutableIntStateOf(-1)

    // Interactive Listening Quiz State
    private var isQuizMode by mutableStateOf(false)
    private var quizScore by mutableIntStateOf(0)
    private var quizTargetItem by mutableStateOf<AlphabetCardItem?>(null)
    private var lastCorrectClickedId by mutableIntStateOf(-1)
    private var lastWrongClickedId by mutableIntStateOf(-1)
    private var currentActiveMode by mutableStateOf(AlphabetMode.THAI)

    private val autoReadHandler = Handler(Looper.getMainLooper())
    private var autoReadRunnable: Runnable? = null
    private val thaiItems by lazy { ThaiAlphabetRepository.getThaiAlphabetList() }
    private val vowelItems by lazy { ThaiAlphabetRepository.getThaiVowelList() }
    private val numberItems by lazy { ThaiAlphabetRepository.getNumbersList() }
    private val engItems by lazy { ThaiAlphabetRepository.getEnglishAlphabetList() }

    private var initialMode = AlphabetMode.THAI

    companion object {
        private const val TAG = "MenuThaiLettersActivity"
        private const val AUTO_READ_DELAY = 3200L // 3.2 seconds per letter
        const val EXTRA_MODE = "EXTRA_MODE" // "THAI", "VOWELS", "NUMBERS", or "ENGLISH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modeExtra = intent.getStringExtra(EXTRA_MODE)
        initialMode = when (modeExtra) {
            "ENGLISH" -> AlphabetMode.ENGLISH
            "VOWELS" -> AlphabetMode.THAI_VOWELS
            "NUMBERS" -> AlphabetMode.NUMBERS
            else -> AlphabetMode.THAI
        }
        currentActiveMode = initialMode

        setupTextToSpeech()

        setContent {
            VideoWatchFeatureTheme {
                AlphabetMenuComposeScreen(
                    initialMode = initialMode,
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
                            if (item.id < 44) {
                                openWritingScreen(item, position)
                            }
                        }
                    },
                    onPracticeWriteClick = { item, position ->
                        if (!isQuizMode && !isAutoReading) {
                            if (item.id < 44) {
                                openWritingScreen(item, position)
                            } else {
                                playSoundForItem(item)
                            }
                        }
                    },
                    onSoundClick = { item, _ ->
                        if (!isQuizMode && !isAutoReading) {
                            playSoundForItem(item)
                        }
                    },
                    onBackClick = { finish() }
                )
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
                    AlphabetMode.NUMBERS -> numberItems
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
                                    "🎉 เก่งมาก! อ่านครบทุกตัวแล้ว",
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
            AlphabetMode.NUMBERS -> numberItems
            AlphabetMode.ENGLISH -> engItems
        }

        if (items.isEmpty()) return

        // Pick random item
        val target = items[Random.nextInt(items.size)]
        quizTargetItem = target

        // Prompt question sound
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

        if (clickedItem.id == target.id) {
            // ✅ CORRECT ANSWER!
            lastCorrectClickedId = clickedItem.id
            lastWrongClickedId = -1
            quizScore++

            // Play celebratory sound & speech
            playRawAudio(R.raw.chiotrue)
            speakTts("เก่งมากค่ะ!")

            // Advance to next question after 1.6s
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
            speakTts("ช่วยลองใหม่อีกครั้งค่ะ")
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
        val intent = Intent(this, ThaiLettersActivity::class.java).apply {
            putExtra("SELECTED_POSITION", position)
            putExtra("SELECTED_LETTER_INDEX", position)
            putExtra("SELECTED_LETTER_NAME", item.name)
            putExtra("SELECTED_ITEM_ID", item.id)
        }
        startActivity(intent)
    }

    private fun playSoundForItem(item: AlphabetCardItem) {
        try {
            var playedSound = false

            // 1. เล่นจากไฟล์เสียงถ้ามี (ก-ฮ, A-Z, ตัวเลข 1-9)
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

            // 2. ถ้าไม่มีไฟล์เสียง ให้ใช้ TTS อ่านออกเสียง (สระ, วรรณยุกต์, ๑๐ สิบ Ten)
            if (!playedSound) {
                speakTts(item.rhyme)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound for item: ${item.name}", e)
            speakTts(item.name)
        }
    }

    private fun speakTts(text: String) {
        try {
            if (isTtsReady && textToSpeech != null) {
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
