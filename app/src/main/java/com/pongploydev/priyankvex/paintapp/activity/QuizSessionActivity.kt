package com.pongploydev.priyankvex.paintapp.activity

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.confixdata.DataManager
import com.pongploydev.priyankvex.paintapp.confixdata.File_Confix_Data
import com.pongploydev.priyankvex.paintapp.confixdata.LearningCategory
import com.pongploydev.priyankvex.paintapp.databinding.ActivityQuizSessionBinding
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel
import com.pongploydev.priyankvex.paintapp.model.QuizScore
import java.util.Locale
import kotlin.random.Random

/** แปลงตัวเลขอารบิกเป็นตัวเลขไทย */
private fun String.toThaiNumber(): String {
    val thaiDigits = charArrayOf('๐', '๑', '๒', '๓', '๔', '๕', '๖', '๗', '๘', '๙')
    return map { if (it in '0'..'9') thaiDigits[it - '0'] else it }.joinToString("")
}

private fun Int.toThai(): String = toString().toThaiNumber()

data class QuizItem(
        val id: String,
        val displayValue: String,
        val thaiName: String,
        val imageRes: Int,
        val soundRes: Int? = null
)

class QuizSessionActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityQuizSessionBinding
    private lateinit var currentItem: QuizItem
    private lateinit var options: List<QuizItem>
    private var correctAnswerIndex = 0
    private var currentQuestion = 0
    private val totalQuestions = 20
    private var score = 0
    private var answered = false
    private var selectedOption = -1
    private var tts: TextToSpeech? = null
    private var isAutoNavigating = false
    private var startTime: Long = 0

    private var topicId = 1
    private var topicTitle = ""
    private lateinit var quizList: List<QuizItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply WindowInsets so toolbar & btnNext are never overlapped by status bar or bottom navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            if (statusBarTop > 0) {
                binding.toolbar.setPadding(
                    binding.toolbar.paddingLeft,
                    statusBarTop,
                    binding.toolbar.paddingRight,
                    binding.toolbar.paddingBottom
                )
            }

            val params = binding.btnNext.layoutParams as? ViewGroup.MarginLayoutParams
            if (params != null) {
                val defaultMargin = (16 * resources.displayMetrics.density).toInt()
                params.bottomMargin = defaultMargin + navBarBottom
                binding.btnNext.layoutParams = params
            }

            insets
        }

        topicId = intent.getIntExtra("EXTRA_TOPIC_ID", 1)
        topicTitle = intent.getStringExtra("EXTRA_TOPIC_TITLE") ?: "Quiz"

        initializeQuizData()

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        setupToolbar()
        setupClickListeners()
        startTime = System.currentTimeMillis()
        loadNewQuestion()
    }

    private fun initializeQuizData() {
        quizList =
                when (topicId) {
                    1 -> { // Thai ก-ฮ
                        val letters =
                                arrayOf(
                                        "ก",
                                        "ข",
                                        "ฃ",
                                        "ค",
                                        "ฅ",
                                        "ฆ",
                                        "ง",
                                        "จ",
                                        "ฉ",
                                        "ช",
                                        "ซ",
                                        "ฌ",
                                        "ญ",
                                        "ฎ",
                                        "ฏ",
                                        "ฐ",
                                        "ฑ",
                                        "ฒ",
                                        "ณ",
                                        "ด",
                                        "ต",
                                        "ถ",
                                        "ท",
                                        "ธ",
                                        "น",
                                        "บ",
                                        "ป",
                                        "ผ",
                                        "ฝ",
                                        "พ",
                                        "ฟ",
                                        "ภ",
                                        "ม",
                                        "ย",
                                        "ร",
                                        "ล",
                                        "ว",
                                        "ศ",
                                        "ษ",
                                        "ส",
                                        "ห",
                                        "ฬ",
                                        "อ",
                                        "ฮ"
                                )
                        val names =
                                arrayOf(
                                        "ก ไก่",
                                        "ข ไข่",
                                        "ฃ ขวด",
                                        "ค ควาย",
                                        "ฅ คน",
                                        "ฆ ระฆัง",
                                        "ง งู",
                                        "จ จาน",
                                        "ฉ ฉิ่ง",
                                        "ช ช้าง",
                                        "ซ โซ่",
                                        "ฌ เฌอ",
                                        "ญ หญิง",
                                        "ฎ ชฎา",
                                        "ฏ ปฏัก",
                                        "ฐ ฐาน",
                                        "ฑ มณโฑ",
                                        "ฒ ผู้เฒ่า",
                                        "ณ เณร",
                                        "ด เด็ก",
                                        "ต เต่า",
                                        "ถ ถุง",
                                        "ท ทหาร",
                                        "ธ ธง",
                                        "น หนู",
                                        "บ ใบไม้",
                                        "ป ปลา",
                                        "ผ ผึ้ง",
                                        "ฝ ฝา",
                                        "พ พาน",
                                        "ฟ ฟัน",
                                        "ภ สำเภา",
                                        "ม ม้า",
                                        "ย ยักษ์",
                                        "ร เรือ",
                                        "ล ลิง",
                                        "ว แหวน",
                                        "ศ ศาลา",
                                        "ษ ฤๅษี",
                                        "ส เสือ",
                                        "ห หีบ",
                                        "ฬ จุฬา",
                                        "อ อ่าง",
                                        "ฮ นกฮูก"
                                )
                        val list = mutableListOf<QuizItem>()
                        for (i in letters.indices) {
                            list.add(
                                    QuizItem(
                                            letters[i],
                                            letters[i],
                                            names[i],
                                            File_Confix_Data.image_confixe1[i],
                                            File_Confix_Data.sound1[i]
                                    )
                            )
                        }
                        list
                    }
                    2 -> { // English A-Z
                        val letters = (0 until 26).map { ('A' + it).toString() }
                        val drawables =
                                intArrayOf(
                                        R.drawable.e_a,
                                        R.drawable.e_b,
                                        R.drawable.e_c,
                                        R.drawable.e_d,
                                        R.drawable.e_e,
                                        R.drawable.e_f,
                                        R.drawable.e_g,
                                        R.drawable.e_h,
                                        R.drawable.e_i,
                                        R.drawable.e_j,
                                        R.drawable.e_k,
                                        R.drawable.e_l,
                                        R.drawable.e_m,
                                        R.drawable.e_n,
                                        R.drawable.e_o,
                                        R.drawable.e_p,
                                        R.drawable.e_q,
                                        R.drawable.e_r,
                                        R.drawable.e_s,
                                        R.drawable.e_t,
                                        R.drawable.e_u,
                                        R.drawable.e_v,
                                        R.drawable.e_w,
                                        R.drawable.e_x,
                                        R.drawable.e_y,
                                        R.drawable.e_z
                                )
                        val list = mutableListOf<QuizItem>()
                        for (i in letters.indices) {
                            list.add(QuizItem(letters[i], letters[i], letters[i], drawables[i]))
                        }
                        list
                    }
                    3 -> { // Numbers 0-9
                        val list = mutableListOf<QuizItem>()
                        val drawables = File_Confix_Data.imgMenu3
                        val sounds = File_Confix_Data.imgMenu1MediaPlayer2
                        for (i in 0 until 10) {
                            list.add(
                                    QuizItem(
                                            i.toString(),
                                            i.toString(),
                                            i.toString(),
                                            drawables[i],
                                            sounds[i]
                                    )
                            )
                        }
                        list
                    }
                    4 -> { // English Words (A for Apple, B for Ball...)
                        DataManager.getDataByCategory(0).map { item ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    5 -> { // คำศัพท์อังกฤษ-ไทย (รูปเป็นคำไทย)
                        LearningCategory.ALPHABET.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    6 -> { // Numbers as Words (Zero, One, Two...)
                        LearningCategory.NUMBERS.data.take(10).map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    7 -> { // Animals
                        LearningCategory.ANIMALS.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    8 -> { // Fruits
                        LearningCategory.FRUITS.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    9 -> { // Colors
                        LearningCategory.COLORS.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    10 -> { // Vegetables
                        LearningCategory.VEGETABLES.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    11 -> { // Sports
                        LearningCategory.SPORTS.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    12 -> { // Vehicles
                        LearningCategory.VEHICLES.data.map { item: LearningDataModel ->
                            QuizItem(item.showTitle, item.showTitle, item.showTitle, item.image)
                        }
                    }
                    else -> listOf()
                }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result =
                    tts?.setLanguage(
                            if (topicId in listOf(2, 4, 6, 7, 8, 9, 10, 11, 12)) Locale.US
                            else Locale("th")
                    )
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                // Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show()
            } else {
                speakCurrentItem()
            }
        }
    }

    private fun speakCurrentItem() {
        if (::currentItem.isInitialized && tts?.isSpeaking == false) {
            if (topicId == 1) { // Thai
                tts?.speak(currentItem.thaiName, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                tts?.speak(currentItem.displayValue, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = topicTitle
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.cardOption1.setOnClickListener { onOptionSelected(0) }
        binding.cardOption2.setOnClickListener { onOptionSelected(1) }
        binding.cardOption3.setOnClickListener { onOptionSelected(2) }
        binding.cardOption4.setOnClickListener { onOptionSelected(3) }

        binding.cvImage.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this, R.anim.bounce)
            binding.cvImage.startAnimation(anim)
            speakCurrentItem()
        }

        binding.btnNext.setOnClickListener {
            if (isAutoNavigating) return@setOnClickListener
            if (answered) {
                goToNextOrResults()
            } else {
                Toast.makeText(this, "โปรดเลือกคำตอบก่อน", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToNextOrResults() {
        isAutoNavigating = false
        if (currentQuestion < totalQuestions) {
            loadNewQuestion()
        } else {
            showResults()
        }
    }

    private fun loadNewQuestion() {
        answered = false
        selectedOption = -1
        currentQuestion++

        binding.tvProgress.text = "ข้อ ${currentQuestion.toThai()} / ${totalQuestions.toThai()}"
        binding.tvScoreBadge.text = "⭐ ${score.toThai()} คะแนน"
        binding.progressBar.progress = (currentQuestion * 100) / totalQuestions

        currentItem = quizList[Random.nextInt(quizList.size)]
        binding.ivQuestionImage.setImageResource(currentItem.imageRes)

        correctAnswerIndex = Random.nextInt(4)
        options = generateOptions(currentItem, correctAnswerIndex)

        binding.tvQuestion.text =
                when (topicId) {
                    1 -> "❓ นี่คือตัวอะไร?"
                    2 -> "❓ นี่คือตัวอักษรอะไร?"
                    3 -> "❓ นี่คือเลขอะไร?"
                    4 -> "❓ นี่คืออะไร?"
                    5 -> "❓ รูปนี้คืออะไร?"
                    6 -> "❓ นี่คือเลขอะไร?"
                    7 -> "❓ นี่คือสัตว์อะไร?"
                    8 -> "❓ นี่คือผลไม้อะไร?"
                    9 -> "❓ นี่คือสีอะไร?"
                    10 -> "❓ นี่คือผักอะไร?"
                    11 -> "❓ นี่คือกีฬาอะไร?"
                    12 -> "❓ นี่คือยานพาหนะอะไร?"
                    else -> "❓ ทายสิคืออะไร?"
                }

        binding.tvOption1.text = options[0].displayValue
        binding.ivOption1.setImageResource(options[0].imageRes)
        
        binding.tvOption2.text = options[1].displayValue
        binding.ivOption2.setImageResource(options[1].imageRes)

        binding.tvOption3.text = options[2].displayValue
        binding.ivOption3.setImageResource(options[2].imageRes)

        binding.tvOption4.text = options[3].displayValue
        binding.ivOption4.setImageResource(options[3].imageRes)

        resetOptionsUI()

        if (currentQuestion == totalQuestions) {
            binding.btnNext.text = "ดูผลลัพธ์ 🏆"
        } else {
            binding.btnNext.text = "ข้อถัดไป ▶"
        }

        binding.root.postDelayed({ speakCurrentItem() }, 500)
    }

    private fun generateOptions(correctItem: QuizItem, correctIndex: Int): List<QuizItem> {
        val usedValues = mutableSetOf<String>()
        usedValues.add(correctItem.displayValue)

        val tempList = mutableListOf<QuizItem>()
        for (i in 0 until 4) {
            if (i == correctIndex) {
                tempList.add(correctItem)
            } else {
                var wrongItem: QuizItem
                var attempts = 0
                do {
                    wrongItem = quizList[Random.nextInt(quizList.size)]
                    attempts++
                } while (usedValues.contains(wrongItem.displayValue) && attempts < 20)
                usedValues.add(wrongItem.displayValue)
                tempList.add(wrongItem)
            }
        }
        return tempList
    }

    private fun onOptionSelected(optionIndex: Int) {
        if (answered) return
        selectedOption = optionIndex
        answered = true
        showAnswerResult(optionIndex)
        if (optionIndex == correctAnswerIndex) {
            score++
            binding.tvScoreBadge.text = "⭐ ${score.toThai()} คะแนน"
            playCorrectSound()
            isAutoNavigating = true
            binding.root.postDelayed(
                    {
                        if (!isFinishing) {
                            goToNextOrResults()
                        }
                    },
                    900
            )
        } else {
            playWrongSound()
            isAutoNavigating = true
            binding.root.postDelayed(
                    {
                        if (!isFinishing) {
                            goToNextOrResults()
                        }
                    },
                    1500
            )
        }
    }

    private fun playCorrectSound() {
        tts?.stop()
        tts?.language = Locale("th")
        tts?.speak("เก่งมากค่ะ", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun playWrongSound() {
        tts?.stop()
        tts?.language = Locale("th")
        tts?.speak(
                "ไม่ถูกค่ะ ตัวนี้คือ ${currentItem.displayValue}",
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
        )
    }

    private fun showAnswerResult(selectedIndex: Int) {
        highlightSelectedOption(selectedIndex, selectedIndex == correctAnswerIndex)
        if (selectedIndex != correctAnswerIndex) {
            highlightCorrectOption()
        }
    }

    private fun highlightSelectedOption(index: Int, isCorrect: Boolean) {
        val cardView = getOptionCard(index)
        val textView = getOptionTextView(index)
        val imageView = getOptionImageView(index)
        val resultIconView = getOptionIconView(index)

        val cardBg = if (isCorrect) Color.parseColor("#E8F5E9") else Color.parseColor("#FFEBEE")
        val strokeColor = if (isCorrect) Color.parseColor("#4CAF50") else Color.parseColor("#EF5350")
        val textColor = if (isCorrect) Color.parseColor("#1B5E20") else Color.parseColor("#B71C1C")

        cardView.setCardBackgroundColor(cardBg)
        cardView.strokeColor = strokeColor
        textView.setTextColor(textColor)
        imageView.clearColorFilter()
        resultIconView.setImageResource(if (isCorrect) R.drawable.ic_check_24 else R.drawable.ic_close_24)
        resultIconView.setColorFilter(strokeColor)
        resultIconView.visibility = View.VISIBLE
    }

    private fun highlightCorrectOption() {
        val cardView = getOptionCard(correctAnswerIndex)
        val textView = getOptionTextView(correctAnswerIndex)
        val imageView = getOptionImageView(correctAnswerIndex)
        val resultIconView = getOptionIconView(correctAnswerIndex)

        val greenBg = Color.parseColor("#E8F5E9")
        val greenStroke = Color.parseColor("#4CAF50")
        val greenText = Color.parseColor("#1B5E20")

        cardView.setCardBackgroundColor(greenBg)
        cardView.strokeColor = greenStroke
        textView.setTextColor(greenText)
        imageView.clearColorFilter()
        resultIconView.setImageResource(R.drawable.ic_check_24)
        resultIconView.setColorFilter(greenStroke)
        resultIconView.visibility = View.VISIBLE
    }

    private fun resetOptionsUI() {
        binding.cardOption1.setCardBackgroundColor(Color.parseColor("#FFF0F3"))
        binding.cardOption1.strokeColor = Color.parseColor("#FF5252")
        binding.tvOption1.setTextColor(Color.parseColor("#D32F2F"))
        binding.ivOption1.clearColorFilter()
        binding.ivResult1.visibility = View.GONE

        binding.cardOption2.setCardBackgroundColor(Color.parseColor("#F0F7FF"))
        binding.cardOption2.strokeColor = Color.parseColor("#29B6F6")
        binding.tvOption2.setTextColor(Color.parseColor("#0288D1"))
        binding.ivOption2.clearColorFilter()
        binding.ivResult2.visibility = View.GONE

        binding.cardOption3.setCardBackgroundColor(Color.parseColor("#F0FFF4"))
        binding.cardOption3.strokeColor = Color.parseColor("#66BB6A")
        binding.tvOption3.setTextColor(Color.parseColor("#388E3C"))
        binding.ivOption3.clearColorFilter()
        binding.ivResult3.visibility = View.GONE

        binding.cardOption4.setCardBackgroundColor(Color.parseColor("#FFF8E7"))
        binding.cardOption4.strokeColor = Color.parseColor("#FFA726")
        binding.tvOption4.setTextColor(Color.parseColor("#F57C00"))
        binding.ivOption4.clearColorFilter()
        binding.ivResult4.visibility = View.GONE
    }

    private fun getOptionCard(index: Int): MaterialCardView =
            when (index) {
                0 -> binding.cardOption1
                1 -> binding.cardOption2
                2 -> binding.cardOption3
                3 -> binding.cardOption4
                else -> binding.cardOption1
            }

    private fun getOptionTextView(index: Int): android.widget.TextView =
            when (index) {
                0 -> binding.tvOption1
                1 -> binding.tvOption2
                2 -> binding.tvOption3
                3 -> binding.tvOption4
                else -> binding.tvOption1
            }

    private fun getOptionImageView(index: Int): android.widget.ImageView =
            when (index) {
                0 -> binding.ivOption1
                1 -> binding.ivOption2
                2 -> binding.ivOption3
                3 -> binding.ivOption4
                else -> binding.ivOption1
            }

    private fun getOptionIconView(index: Int): android.widget.ImageView =
            when (index) {
                0 -> binding.ivResult1
                1 -> binding.ivResult2
                2 -> binding.ivResult3
                3 -> binding.ivResult4
                else -> binding.ivResult1
            }

    private fun showResults() {
        val duration = (System.currentTimeMillis() - startTime) / 1000
        val accuracy = (score * 100) / totalQuestions
        val message =
                when {
                    accuracy >= 80 -> "ยอดเยี่ยมมาก! 🎉"
                    accuracy >= 60 -> "ทำได้ดีมาก! 👍"
                    accuracy >= 40 -> "ไม่เลวเลย! 😊"
                    else -> "พยายามต่อไปนะ! 📚"
                }

        val input = EditText(this).apply {
            hint = "ใส่ชื่อเล่นของคุณที่นี่"
            setPadding(50, 40, 50, 40)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 0)
            addView(input)
        }

        // Record quiz progress and unlock next lesson if passed
        com.pongploydev.priyankvex.paintapp.utils.QuizProgressManager.recordQuizResult(this, topicId, score, totalQuestions)

        androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("สรุปผลคะแนน")
                .setMessage(
                        "$message\n\nคะแนน: ${score.toThai()}/${totalQuestions.toThai()}\nความถูกต้อง: ${accuracy.toThai()}%\nใช้เวลา: ${duration.toInt().toThai()} วินาที"
                )
                .setView(container)
                .setPositiveButton("บันทึกคะแนน") { _, _ ->
                    val name = input.text.toString().trim().ifEmpty { "ผู้เล่นนิรนาม" }
                    saveScoreToFirestore(name, score, duration)
                }
                .setNegativeButton("เล่นใหม่") { _, _ -> restartQuiz() }
                .setNeutralButton("ออก") { _, _ -> finish() }
                .setCancelable(false)
                .show()
    }

    private fun saveScoreToFirestore(name: String, finalScore: Int, duration: Long) {
        val quizScore = QuizScore(
            userName = name,
            score = finalScore,
            totalQuestions = totalQuestions,
            topicTitle = topicTitle,
            durationSeconds = duration
        )

        // 1. Save to Firebase Firestore
        FirebaseFirestore.getInstance().collection("quiz_scores")
            .add(quizScore)
            .addOnSuccessListener {
                Toast.makeText(this, "บันทึกคะแนนสำเร็จแล้ว! 🏆", Toast.LENGTH_SHORT).show()
                restartQuiz()
            }
            .addOnFailureListener {
                Toast.makeText(this, "ไม่สามารถบันทึกได้ กรุณาลองใหม่", Toast.LENGTH_SHORT).show()
                restartQuiz()
            }

        // 2. Save to Supabase PostgreSQL Database via SupabaseDataManager
        val supabaseUser = SupabaseAuthManager.getInstance(this).getStoredUser()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = supabaseUser?.id ?: firebaseUser?.uid ?: "guest_user"
        if (!userId.isNullOrEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val dataManager = SupabaseDataManager.getInstance(this@QuizSessionActivity)
                dataManager.saveQuizResult(
                    userId = userId,
                    topicId = topicId,
                    topicName = topicTitle,
                    score = finalScore,
                    totalQuestions = totalQuestions,
                    timeSpentSeconds = duration.toInt()
                )
                dataManager.fetchUserProfile(userId).onSuccess { prof ->
                    dataManager.addStarsAndLevel(userId, prof.totalStars, prof.currentLevel, finalScore)
                }
                if (finalScore >= 8) {
                    dataManager.unlockAchievement(
                        userId = userId,
                        badgeKey = "badge_quiz_master",
                        badgeName = "เกียรติบัตรนักตอบคำถาม 🏆",
                        badgeIcon = "🎓"
                    )
                }
            }
        }
    }

    private fun restartQuiz() {
        currentQuestion = 0
        score = 0
        startTime = System.currentTimeMillis()
        binding.tvScoreBadge.text = "⭐ ๐"
        loadNewQuestion()
    }

    override fun onDestroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
}
