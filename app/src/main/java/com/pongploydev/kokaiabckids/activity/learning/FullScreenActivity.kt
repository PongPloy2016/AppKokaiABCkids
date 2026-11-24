package com.pongploydev.kokaiabckids.activity.learning

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.activity.learning.showKids.ColoringGameActivity
import com.pongploydev.kokaiabckids.activity.thailetters.ThaiLettersActivity
import com.pongploydev.kokaiabckids.adapter.LearningPagerAdapter
import com.pongploydev.kokaiabckids.confixdata.*
import com.pongploydev.kokaiabckids.model.LearningDataModel
import com.pongploydev.kokaiabckids.model.ThaiAlphabetRepository
import com.pongploydev.kokaiabckids.utils.AppControl
import com.pongploydev.kokaiabckids.utils.CategoryDataHelper
import com.pongploydev.kokaiabckids.utils.Constant
import com.pongploydev.kokaiabckids.utils.NonSwipeAbleViewPager
import com.pongploydev.kokaiabckids.utils.Utils
import java.util.*

class FullScreenActivity : AppCompatActivity() {

    data class LearningDisplayInfo(
        val symbol: String,
        val thaiName: String,
        val englishName: String,
        val ttsSpeakText: String,
        val traceSymbol: String
    )

    private lateinit var context: Context
    private lateinit var viewPager: NonSwipeAbleViewPager

    // Text & Pronunciation Views
    private lateinit var tvItemSymbol: TextView
    private lateinit var tvThaiName: TextView
    private lateinit var tvEnglishName: TextView
    private lateinit var btnPronunciationSound: View
    private lateinit var tvPronunciationHelp: TextView

    // Tracing Preview Views
    private lateinit var cardTracingPreview: View
    private lateinit var tvTraceGuideLetter: TextView
    private lateinit var tvTraceArrows: TextView

    // Navigation & Header Views
    private lateinit var imgBtnNext: View
    private lateinit var imgBtnPrev: View
    private lateinit var imgSoundToggle: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var imgStarProgress: ImageView
    private var tvCategoryTitle: TextView? = null

    private var categoryPosition = 0
    private var selectedPosition = 0
    private lateinit var currentData: List<LearningDataModel>
    private lateinit var textToSpeech: TextToSpeech
    private var isSoundOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)
        supportActionBar?.hide()

        context = this
        initViews()
        setupTextToSpeech()
        handleIntent()
        setupViewPager()
        updateProgress()
        updateSoundButton()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tvItemSymbol = findViewById(R.id.tvItemSymbol)
        tvThaiName = findViewById(R.id.tvThaiName)
        tvEnglishName = findViewById(R.id.tvEnglishName)
        btnPronunciationSound = findViewById(R.id.btnPronunciationSound)
        tvPronunciationHelp = findViewById(R.id.tvPronunciationHelp)

        cardTracingPreview = findViewById(R.id.cardTracingPreview)
        tvTraceGuideLetter = findViewById(R.id.tvTraceGuideLetter)
        tvTraceArrows = findViewById(R.id.tvTraceArrows)

        imgBtnNext = findViewById(R.id.imgBtnNext)
        imgBtnPrev = findViewById(R.id.imgBtnPrev)
        imgSoundToggle = findViewById(R.id.imgSoundToggle)
        progressBar = findViewById(R.id.progressBar)
        imgStarProgress = findViewById(R.id.imgStarProgress)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)

        // Setup sound toggle & preferences
        isSoundOn = Utils.getPref(Constant.SOUND, true)

        // Setup Speaker Click Listener
        btnPronunciationSound.setOnClickListener {
            YoYo.with(Techniques.RubberBand).duration(500).playOn(btnPronunciationSound)
            val currentPos = viewPager.currentItem
            if (currentPos in currentData.indices) {
                val info = getDisplayInfo(categoryPosition, currentPos, currentData[currentPos])
                speakText(info.ttsSpeakText)
            }
        }

        // Setup Tracing Card Click Listener
        cardTracingPreview.setOnClickListener {
            YoYo.with(Techniques.Pulse).duration(300).playOn(cardTracingPreview)
            openPracticeWritingScreen(viewPager.currentItem)
        }
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale("th", "TH")
            }
        }
    }

    private fun handleIntent() {
        intent?.let {
            categoryPosition = it.getIntExtra("categoryPosition", 0)
            selectedPosition = it.getIntExtra("selectedPosition", 0)
        }
        tvCategoryTitle?.text = CategoryDataHelper.getCategoryTitle(categoryPosition)
        loadCategoryData(categoryPosition)
    }

    private fun loadCategoryData(categoryIndex: Int) {
        currentData = DataManager.getDataByCategory(categoryIndex)
    }

    private fun setupViewPager() {
        val adapter = LearningPagerAdapter(context, currentData)
        viewPager.adapter = adapter
        viewPager.currentItem = selectedPosition

        updateUI(selectedPosition)
        setupViewPagerListener()
    }

    private fun updateUI(position: Int) {
        if (position !in currentData.indices) return

        val info = getDisplayInfo(categoryPosition, position, currentData[position])

        // Update Text
        tvItemSymbol.text = info.symbol
        tvThaiName.text = info.thaiName
        tvEnglishName.text = info.englishName

        // Update Tracing guide preview
        tvTraceGuideLetter.text = info.traceSymbol
        tvTraceArrows.text = getArrowForSymbol(info.traceSymbol)

        // Animate text gently
        YoYo.with(Techniques.BounceIn).duration(600).playOn(tvThaiName)
        YoYo.with(Techniques.FadeIn).duration(400).playOn(tvEnglishName)

        // Auto speak if sound enabled
        if (isSoundOn) {
            speakText(info.ttsSpeakText)
        }

        // Update navigation buttons
        updateNavigationButtons()
    }

    private fun updateProgress() {
        val current = viewPager.currentItem + 1
        val total = currentData.size
        progressBar.max = total
        progressBar.progress = current

        // Star pulsing animation on milestone
        YoYo.with(Techniques.Pulse).duration(400).playOn(imgStarProgress)
    }

    private fun updateNavigationButtons() {
        imgBtnPrev.alpha = if (viewPager.currentItem > 0) 1.0f else 0.4f
        imgBtnPrev.isEnabled = viewPager.currentItem > 0

        imgBtnNext.alpha = if (viewPager.currentItem < currentData.size - 1) 1.0f else 0.4f
        imgBtnNext.isEnabled = viewPager.currentItem < currentData.size - 1
    }

    private fun updateSoundButton() {
        val soundIcon = if (isSoundOn) R.drawable.ic_sound_on else R.drawable.ic_sound_off
        imgSoundToggle.setImageResource(soundIcon)
    }

    private fun setupViewPagerListener() {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                updateUI(position)
                updateProgress()
                updateNavigationButtons()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun speakText(text: String) {
        if (isSoundOn) {
            try {
                if (::textToSpeech.isInitialized) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "item_speak")
                } else {
                    AppControl.textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openPracticeWritingScreen(currentPos: Int) {
        try {
            if (currentPos in currentData.indices) {
                val info = getDisplayInfo(categoryPosition, currentPos, currentData[currentPos])
                val intent = Intent(this, FunTracingActivity::class.java).apply {
                    putExtra("symbol", info.symbol)
                    putExtra("name", info.englishName)
                    putExtra("thaiName", info.thaiName)
                    putExtra("categoryPosition", categoryPosition)
                    putExtra("selectedPosition", currentPos)
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "มาฝึกเขียนกันเถอะ! ✏️", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================
    // DATA MAPPING HELPER FOR ALL 20 CATEGORIES
    // ============================================
    private fun getDisplayInfo(categoryPos: Int, index: Int, model: LearningDataModel): LearningDisplayInfo {
        val showTitle = model.showTitle ?: ""
        val speakTitle = model.speakTitle ?: ""

        return when (categoryPos) {
            // Category 0: Alphabet A-Z
            0 -> {
                val letter = ('A' + index).toString()
                val engWord = speakTitle.ifBlank { showTitle }
                val thaiWord = ThaiLearningDictionary.toThai(engWord, speakTitle)
                LearningDisplayInfo(
                    symbol = letter,
                    thaiName = thaiWord,
                    englishName = engWord,
                    ttsSpeakText = "$letter $thaiWord $engWord",
                    traceSymbol = letter
                )
            }

            // Category 1: Numbers 0-20
            1 -> {
                val numSymbol = index.toString()
                val thaiNum = ThaiLearningDictionary.toThai(showTitle, speakTitle)
                val engNum = showTitle.ifBlank { speakTitle }
                LearningDisplayInfo(
                    symbol = numSymbol,
                    thaiName = thaiNum,
                    englishName = engNum,
                    ttsSpeakText = "$thaiNum $engNum",
                    traceSymbol = numSymbol
                )
            }

            // Category 20: พยัญชนะไทย ก-ฮ
            20 -> {
                val item = ThaiAlphabetRepository.getThaiAlphabetList().getOrNull(index)
                val letter = item?.letter ?: showTitle.firstOrNull()?.toString() ?: "ก"
                val name = item?.name ?: showTitle
                val rhyme = item?.rhyme ?: speakTitle
                LearningDisplayInfo(
                    symbol = letter,
                    thaiName = name,
                    englishName = rhyme,
                    ttsSpeakText = "$name $rhyme",
                    traceSymbol = letter
                )
            }

            // Category 21: สระภาษาไทย
            21 -> {
                val item = ThaiAlphabetRepository.getThaiVowelList().getOrNull(index)
                val sym = item?.letter ?: showTitle
                val name = item?.name ?: showTitle
                val rhyme = item?.rhyme ?: speakTitle
                LearningDisplayInfo(
                    symbol = sym,
                    thaiName = name,
                    englishName = rhyme,
                    ttsSpeakText = rhyme.ifBlank { name },
                    traceSymbol = sym
                )
            }

            // Category 22: วันทั้ง 7 (7 วัน 7 สี)
            22 -> {
                val emojis = arrayOf("❤️", "💛", "🩷", "💚", "🧡", "💙", "💜")
                val dayEmoji = emojis.getOrElse(index) { "📅" }
                LearningDisplayInfo(
                    symbol = dayEmoji,
                    thaiName = showTitle,
                    englishName = speakTitle,
                    ttsSpeakText = speakTitle.ifBlank { showTitle },
                    traceSymbol = (index + 1).toString()
                )
            }

            // Category 23: ฝึกสะกดคำ
            23 -> {
                val item = ThaiAlphabetRepository.getSpellingList().getOrNull(index)
                val word = item?.letter ?: showTitle
                val formula = item?.name ?: showTitle
                val rhyme = item?.rhyme ?: speakTitle
                LearningDisplayInfo(
                    symbol = word,
                    thaiName = formula,
                    englishName = rhyme,
                    ttsSpeakText = "$word $rhyme",
                    traceSymbol = word
                )
            }

            // Categories 2..19: ใช้พจนานุกรมภาษาไทยกลางครบทั้ง 20 หมวด
            else -> {
                val thai = ThaiLearningDictionary.toThai(showTitle, speakTitle)
                val eng = showTitle.ifBlank { speakTitle }
                val ttsText = if (thai.equals(eng, ignoreCase = true)) thai else "$thai $eng"
                LearningDisplayInfo(
                    symbol = getCategoryEmoji(categoryPos),
                    thaiName = thai,
                    englishName = eng,
                    ttsSpeakText = ttsText,
                    traceSymbol = (index + 1).toString()
                )
            }
        }
    }

    private fun getCategoryEmoji(categoryPos: Int): String = when (categoryPos) {
        2 -> "🎨"
        3 -> "🔷"
        4 -> "🐾"
        5 -> "🐦"
        6 -> "🌸"
        7 -> "🍎"
        8 -> "📅"
        9 -> "🥦"
        10 -> "🧍"
        11 -> "👕"
        12 -> "🌍"
        13 -> "🍽️"
        14 -> "📐"
        15 -> "🏠"
        16 -> "👩‍🏫"
        17 -> "🏫"
        18 -> "⚽"
        19 -> "🚗"
        else -> "🌟"
    }

    private fun getArrowForSymbol(symbol: String): String {
        return when (symbol) {
            "1" -> "⇣"
            "2" -> "↷⇲"
            "3" -> "↷↷"
            "4" -> "⇃→⇣"
            "5" -> "⇃↷→"
            "0" -> "↺"
            "A" -> "↗↘-"
            "B" -> "⇣↷↷"
            "C" -> "↺"
            else -> "✏️"
        }
    }

    //region Click Handlers

    fun onClickBack(view: View) {
        YoYo.with(Techniques.FadeOut).duration(250).playOn(view)
        finish()
    }

    fun onClickPrev(view: View) {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
            YoYo.with(Techniques.BounceIn).duration(300).playOn(view)
        }
    }

    fun onClickNext(view: View) {
        if (viewPager.currentItem < currentData.size - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
            YoYo.with(Techniques.BounceIn).duration(300).playOn(view)
        }
    }

    fun onClickSoundToggle(view: View) {
        isSoundOn = !isSoundOn
        updateSoundButton()
        Utils.setPref(Constant.SOUND, isSoundOn)

        val message = if (isSoundOn) "เปิดเสียง 🔊" else "ปิดเสียง 🔇"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        YoYo.with(Techniques.Pulse).duration(300).playOn(view)
    }

    //endregion

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}