package com.pongploydev.priyankvex.paintapp.activity.learning

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.confixdata.DataManager
import com.pongploydev.priyankvex.paintapp.drawingView.KidTracingCanvasView
import com.pongploydev.priyankvex.paintapp.model.ThaiAlphabetRepository

class FunTracingActivity : AppCompatActivity() {

    data class TracingItem(
        val symbol: String,
        val name: String,
        val thaiName: String = "",
        val rhyme: String = "",
        val imageRes: Int = 0,
        val audioRawRes: Int = 0,
        val soundResName: String = ""
    )

    private lateinit var context: Context
    private lateinit var tracingCanvasView: KidTracingCanvasView
    private lateinit var tvTracingTitle: TextView
    private lateinit var tvStarScore: TextView
    private lateinit var btnBackPill: View

    // 3 Stars on top
    private lateinit var starProgress1: ImageView
    private lateinit var starProgress2: ImageView
    private lateinit var starProgress3: ImageView

    // Navigation Buttons (Floating & Bottom)
    private lateinit var btnFloatingPrev: View
    private lateinit var btnFloatingNext: View
    private lateinit var btnPrevLetter: View
    private lateinit var btnNextLetter: View
    private lateinit var centerMascotIndicator: View
    private lateinit var tvCurrentLetterIndicator: TextView

    // Crayons
    private lateinit var crayonRed: ImageView
    private lateinit var crayonGreen: ImageView
    private lateinit var crayonYellow: ImageView
    private lateinit var crayonBlue: ImageView
    private lateinit var crayonOrange: ImageView
    private lateinit var crayonPurple: ImageView
    private lateinit var toolEraser: ImageView

    // Action Buttons
    private lateinit var btnResetCanvas: View
    private lateinit var btnDoneTracing: View

    private val itemList = mutableListOf<TracingItem>()
    private var categoryMode: String = "THAI"
    private var currentSymbol: String = "ก"
    private var currentName: String = "ก ไก่"
    private var thaiName: String = ""
    private var rhyme: String = ""
    private var currentImageRes: Int = 0
    private var audioRawRes: Int = 0
    private var soundResName: String = ""
    private var categoryPosition: Int = 1
    private var selectedPosition: Int = 0
    private var starScore: Int = 120
    private var isCompleted: Boolean = false

    private var successSound: MediaPlayer? = null
    private var letterMediaPlayer: MediaPlayer? = null
    private var textToSpeech: android.speech.tts.TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_tracing)
        supportActionBar?.hide()

        context = this
        initIntentData()
        loadCategoryItems()
        initViews()
        setupCrayons()
        setupCanvas()
        setupSounds()
        setupTTS()

        // Load Initial Item
        if (itemList.isNotEmpty() && selectedPosition in itemList.indices) {
            loadItemAt(selectedPosition, playAudio = false)
        } else {
            updateUIForCurrentSymbol()
        }

        // Play letter sound after short delay on enter
        window.decorView.postDelayed({
            playLetterSound()
        }, 350)
    }

    private fun initIntentData() {
        intent?.let {
            currentSymbol = it.getStringExtra("symbol") ?: "ก"
            currentName = it.getStringExtra("name") ?: "ก ไก่"
            thaiName = it.getStringExtra("thaiName") ?: currentName
            rhyme = it.getStringExtra("rhyme") ?: ""
            currentImageRes = it.getIntExtra("imageRes", 0)
            audioRawRes = it.getIntExtra("audioRawRes", 0)
            soundResName = it.getStringExtra("soundResName") ?: ""
            categoryMode = it.getStringExtra("categoryMode") ?: "THAI"
            categoryPosition = it.getIntExtra("categoryPosition", 1)
            selectedPosition = it.getIntExtra("selectedPosition", 0)
        }
    }

    private fun loadCategoryItems() {
        itemList.clear()
        when (categoryMode) {
            "THAI" -> {
                ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "THAI_VOWELS" -> {
                ThaiAlphabetRepository.getThaiVowelList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "TONE_GROUPS" -> {
                ThaiAlphabetRepository.getToneGroupsList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "NUMBERS" -> {
                val list = if (categoryPosition == 0) ThaiAlphabetRepository.getEnglishNumbersList() else ThaiAlphabetRepository.getNumbersList()
                list.forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "ENGLISH" -> {
                ThaiAlphabetRepository.getEnglishAlphabetList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "ANIMALS" -> {
                ThaiAlphabetRepository.getAnimalsList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "FRUITS" -> {
                ThaiAlphabetRepository.getFruitsList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "VEHICLES" -> {
                ThaiAlphabetRepository.getVehiclesList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "BODY_PARTS" -> {
                ThaiAlphabetRepository.getBodyPartsList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "COLORS_DAYS" -> {
                ThaiAlphabetRepository.getColorsAndDaysList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "SPELLING" -> {
                ThaiAlphabetRepository.getSpellingList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            else -> {
                // If opened from other categories
                if (categoryPosition in 0..20) {
                    val data = DataManager.getDataByCategory(categoryPosition)
                    if (data.isNotEmpty()) {
                        data.forEachIndexed { idx, model ->
                            val sym = if (categoryPosition == 0) ('A' + idx).toString() else if (categoryPosition == 1) idx.toString() else (idx + 1).toString()
                            itemList.add(TracingItem(sym, model.showTitle ?: sym, model.showTitle ?: "", model.speakTitle ?: ""))
                        }
                    } else {
                        ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                            itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                        }
                    }
                } else {
                    ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                        itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                    }
                }
            }
        }

        // If selectedPosition out of bounds, find index matching currentSymbol or name
        if (selectedPosition !in itemList.indices && itemList.isNotEmpty()) {
            val matchedIndex = itemList.indexOfFirst { it.symbol == currentSymbol || it.name == currentName || (it.imageRes != 0 && it.imageRes == currentImageRes) }
            selectedPosition = if (matchedIndex >= 0) matchedIndex else 0
        }
    }

    private fun initViews() {
        tracingCanvasView = findViewById(R.id.tracingCanvasView)
        tvTracingTitle = findViewById(R.id.tvTracingTitle)
        tvStarScore = findViewById(R.id.tvStarScore)
        btnBackPill = findViewById(R.id.btnBackPill)

        starProgress1 = findViewById(R.id.starProgress1)
        starProgress2 = findViewById(R.id.starProgress2)
        starProgress3 = findViewById(R.id.starProgress3)

        btnFloatingPrev = findViewById(R.id.btnFloatingPrev)
        btnFloatingNext = findViewById(R.id.btnFloatingNext)
        btnPrevLetter = findViewById(R.id.btnPrevLetter)
        btnNextLetter = findViewById(R.id.btnNextLetter)
        centerMascotIndicator = findViewById(R.id.centerMascotIndicator)
        tvCurrentLetterIndicator = findViewById(R.id.tvCurrentLetterIndicator)

        crayonRed = findViewById(R.id.crayonRed)
        crayonGreen = findViewById(R.id.crayonGreen)
        crayonYellow = findViewById(R.id.crayonYellow)
        crayonBlue = findViewById(R.id.crayonBlue)
        crayonOrange = findViewById(R.id.crayonOrange)
        crayonPurple = findViewById(R.id.crayonPurple)
        toolEraser = findViewById(R.id.toolEraser)

        btnResetCanvas = findViewById(R.id.btnResetCanvas)
        btnDoneTracing = findViewById(R.id.btnDoneTracing)

        tvStarScore.text = "$starScore Stars"

        tvTracingTitle.setOnClickListener {
            YoYo.with(Techniques.Bounce).duration(400).playOn(tvTracingTitle)
            playLetterSound()
        }

        centerMascotIndicator.setOnClickListener {
            YoYo.with(Techniques.RubberBand).duration(400).playOn(centerMascotIndicator)
            playLetterSound()
        }

        btnBackPill.setOnClickListener {
            YoYo.with(Techniques.FadeOut).duration(200).playOn(btnBackPill)
            finish()
        }

        btnResetCanvas.setOnClickListener {
            YoYo.with(Techniques.RotateIn).duration(400).playOn(btnResetCanvas)
            resetCanvasAndStars()
            Toast.makeText(this, "ล้างกระดานแล้ว 🔄", Toast.LENGTH_SHORT).show()
        }

        btnDoneTracing.setOnClickListener {
            YoYo.with(Techniques.RubberBand).duration(400).playOn(btnDoneTracing)
            onTracingCompleteCelebration()
        }

        // Navigation Listeners
        val prevListener = View.OnClickListener {
            YoYo.with(Techniques.Pulse).duration(250).playOn(it)
            onPreviousClicked()
        }
        val nextListener = View.OnClickListener {
            YoYo.with(Techniques.Pulse).duration(250).playOn(it)
            onNextClicked()
        }

        btnPrevLetter.setOnClickListener(prevListener)
        btnFloatingPrev.setOnClickListener(prevListener)
        btnNextLetter.setOnClickListener(nextListener)
        btnFloatingNext.setOnClickListener(nextListener)

        updateNavButtonsState()
    }

    private fun onPreviousClicked() {
        if (selectedPosition > 0) {
            selectedPosition--
            loadItemAt(selectedPosition, playAudio = true)
        } else {
            Toast.makeText(this, "นี่คือภาพแรกแล้วจ้า 🌟", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onNextClicked() {
        if (selectedPosition < itemList.size - 1) {
            selectedPosition++
            loadItemAt(selectedPosition, playAudio = true)
        } else {
            Toast.makeText(this, "ครบทุกภาพแล้ว เก่งมาก! 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadItemAt(index: Int, playAudio: Boolean = true) {
        if (index !in itemList.indices) return
        selectedPosition = index
        val item = itemList[index]
        currentSymbol = item.symbol
        currentName = item.name
        thaiName = item.thaiName
        rhyme = item.rhyme
        currentImageRes = item.imageRes
        audioRawRes = item.audioRawRes
        soundResName = item.soundResName

        updateUIForCurrentSymbol()

        // Update Canvas with either Guide Image or Character Symbol
        tracingCanvasView.guideImageRes = currentImageRes
        tracingCanvasView.currentSymbol = currentSymbol
        resetCanvasAndStars()

        // Animate
        YoYo.with(Techniques.FadeIn).duration(300).playOn(tracingCanvasView)

        // Update Navigation Buttons
        updateNavButtonsState()

        if (playAudio) {
            playLetterSound()
        }
    }

    private fun updateUIForCurrentSymbol() {
        val hasThai = currentSymbol.any { it in '\u0E00'..'\u0E7F' } || currentName.any { it in '\u0E00'..'\u0E7F' }
        tvTracingTitle.text = if (currentImageRes != 0) {
            "ระบายสี $currentName 🎨"
        } else if (hasThai) {
            "ฝึกเขียน $currentName ✏️"
        } else if (categoryPosition == 0 || categoryMode == "ENGLISH") {
            "Fun Letter Tracing! 🔤"
        } else {
            "Fun Number Tracing! 🔢"
        }
        tvCurrentLetterIndicator.text = if (currentSymbol.length > 3) currentSymbol.take(3) else currentSymbol
        YoYo.with(Techniques.BounceIn).duration(400).playOn(tvTracingTitle)
        YoYo.with(Techniques.Pulse).duration(300).playOn(tvCurrentLetterIndicator)
    }

    private fun updateNavButtonsState() {
        val canPrev = selectedPosition > 0
        val canNext = selectedPosition < itemList.size - 1

        btnPrevLetter.isEnabled = canPrev
        btnPrevLetter.alpha = if (canPrev) 1.0f else 0.4f
        btnFloatingPrev.isEnabled = canPrev
        btnFloatingPrev.alpha = if (canPrev) 1.0f else 0.3f

        btnNextLetter.isEnabled = canNext
        btnNextLetter.alpha = if (canNext) 1.0f else 0.4f
        btnFloatingNext.isEnabled = canNext
        btnFloatingNext.alpha = if (canNext) 1.0f else 0.3f
    }

    private fun setupTTS() {
        try {
            textToSpeech = android.speech.tts.TextToSpeech(this) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    val hasThai = currentSymbol.any { it in '\u0E00'..'\u0E7F' } || currentName.any { it in '\u0E00'..'\u0E7F' }
                    if (hasThai) {
                        textToSpeech?.language = java.util.Locale("th", "TH")
                    } else {
                        textToSpeech?.language = java.util.Locale.US
                    }
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playLetterSound() {
        try {
            var played = false
            if (audioRawRes != 0) {
                playRawAudio(audioRawRes)
                played = true
            } else if (soundResName.isNotEmpty()) {
                val resId = resources.getIdentifier(soundResName, "raw", packageName)
                if (resId != 0) {
                    playRawAudio(resId)
                    played = true
                }
            }

            if (!played && isTtsReady) {
                val speakText = if (rhyme.isNotEmpty()) rhyme else currentName
                textToSpeech?.speak(speakText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "tracing_speak")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playRawAudio(resId: Int) {
        try {
            letterMediaPlayer?.release()
            letterMediaPlayer = MediaPlayer.create(this, resId)?.apply {
                setOnCompletionListener {
                    release()
                    letterMediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupCanvas() {
        tracingCanvasView.currentSymbol = currentSymbol
        tracingCanvasView.tracingListener = object : KidTracingCanvasView.OnTracingListener {
            override fun onProgressUpdate(progress: Float) {
                runOnUiThread {
                    updateStarsProgress(progress)
                }
            }

            override fun onTracingCompleted() {
                runOnUiThread {
                    if (!isCompleted) {
                        isCompleted = true
                        onTracingCompleteCelebration()
                    }
                }
            }
        }
    }

    private fun setupCrayons() {
        val crayons = listOf(
            crayonRed to Color.parseColor("#FF5252"),
            crayonGreen to Color.parseColor("#4CAF50"),
            crayonYellow to Color.parseColor("#FFC107"),
            crayonBlue to Color.parseColor("#2196F3"),
            crayonOrange to Color.parseColor("#FF9800"),
            crayonPurple to Color.parseColor("#9C27B0")
        )

        crayons.forEach { (view, color) ->
            view.setOnClickListener {
                selectCrayon(view, color)
            }
        }

        toolEraser.setOnClickListener {
            selectEraser(toolEraser)
        }
    }

    private fun selectCrayon(selectedView: ImageView, color: Int) {
        tracingCanvasView.currentColor = color
        tracingCanvasView.isEraser = false

        // Animate selection
        resetCrayonHeights()
        selectedView.layoutParams.height = dpToPx(64)
        selectedView.elevation = dpToPx(6).toFloat()
        selectedView.requestLayout()
        YoYo.with(Techniques.Bounce).duration(300).playOn(selectedView)
    }

    private fun selectEraser(selectedView: ImageView) {
        tracingCanvasView.isEraser = true

        resetCrayonHeights()
        selectedView.layoutParams.height = dpToPx(64)
        selectedView.elevation = dpToPx(6).toFloat()
        selectedView.requestLayout()
        YoYo.with(Techniques.Bounce).duration(300).playOn(selectedView)
        Toast.makeText(this, "ใช้ยางลบ 🧼", Toast.LENGTH_SHORT).show()
    }

    private fun resetCrayonHeights() {
        val allTools = listOf(crayonRed, crayonGreen, crayonYellow, crayonBlue, crayonOrange, crayonPurple, toolEraser)
        allTools.forEach {
            it.layoutParams.height = dpToPx(56)
            it.elevation = dpToPx(2).toFloat()
            it.requestLayout()
        }
    }

    private fun updateStarsProgress(progress: Float) {
        if (progress >= 0.25f) {
            starProgress1.setImageResource(R.drawable.star)
            YoYo.with(Techniques.Pulse).duration(300).playOn(starProgress1)
        } else {
            starProgress1.setImageResource(R.drawable.ic_star_outline)
        }

        if (progress >= 0.55f) {
            starProgress2.setImageResource(R.drawable.star)
            YoYo.with(Techniques.Pulse).duration(300).playOn(starProgress2)
        } else {
            starProgress2.setImageResource(R.drawable.ic_star_outline)
        }

        if (progress >= 0.75f) {
            starProgress3.setImageResource(R.drawable.star)
            YoYo.with(Techniques.Pulse).duration(300).playOn(starProgress3)
        } else {
            starProgress3.setImageResource(R.drawable.ic_star_outline)
        }
    }

    private fun setupSounds() {
        try {
            successSound = MediaPlayer.create(this, R.raw.is_sound_success_sound)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSuccessSound() {
        try {
            successSound?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onTracingCompleteCelebration() {
        playSuccessSound()

        starScore += 10
        tvStarScore.text = "$starScore Stars"

        // Light up all 3 stars
        starProgress1.setImageResource(R.drawable.star)
        starProgress2.setImageResource(R.drawable.star)
        starProgress3.setImageResource(R.drawable.star)

        YoYo.with(Techniques.Tada).duration(800).playOn(starProgress1)
        YoYo.with(Techniques.Tada).duration(800).playOn(starProgress2)
        YoYo.with(Techniques.Tada).duration(800).playOn(starProgress3)
        YoYo.with(Techniques.BounceIn).duration(800).playOn(tvStarScore)

        showCelebrationDialog()
    }

    private fun showCelebrationDialog() {
        val hasNext = selectedPosition < itemList.size - 1
        val actionText = if (currentImageRes != 0) "ระบายสี" else "เขียน"
        val builder = AlertDialog.Builder(this)
            .setTitle("เก่งมากเลย! 🎉✨")
            .setMessage("หนู$actionText $currentName สำเร็จแล้ว!\nได้รับ ⭐ +10 ดวง!")
            .setPositiveButton(if (currentImageRes != 0) "ระบายสีอีกครั้ง 🔄" else "ฝึกเขียนอีกครั้ง 🔄") { dialog, _ ->
                dialog.dismiss()
                resetCanvasAndStars()
            }

        if (hasNext) {
            val nextItem = itemList[selectedPosition + 1]
            builder.setNeutralButton("ถัดไป: ${nextItem.symbol} ➡️") { dialog, _ ->
                dialog.dismiss()
                onNextClicked()
            }
        }

        builder.setNegativeButton("กลับหน้าเดิม ✅") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        builder.show()
    }

    private fun resetCanvasAndStars() {
        isCompleted = false
        tracingCanvasView.clearCanvas()
        starProgress1.setImageResource(R.drawable.ic_star_outline)
        starProgress2.setImageResource(R.drawable.ic_star_outline)
        starProgress3.setImageResource(R.drawable.ic_star_outline)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        successSound?.release()
        successSound = null
        letterMediaPlayer?.release()
        letterMediaPlayer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
