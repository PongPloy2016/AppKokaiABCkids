package com.pongploydev.priyankvex.paintapp.activity.learning

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.adapter.LearningPagerAdapter
import com.pongploydev.priyankvex.paintapp.confixdata.*
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel
import com.pongploydev.priyankvex.paintapp.utils.AppControl
import com.pongploydev.priyankvex.paintapp.utils.Constant

import com.pongploydev.priyankvex.paintapp.utils.NonSwipeAbleViewPager
import com.pongploydev.priyankvex.paintapp.utils.Utils
import java.util.*

class FullScreenActivity : AppCompatActivity() {

    private lateinit var context: Context
    private lateinit var viewPager: NonSwipeAbleViewPager
    private lateinit var tvItemName: TextView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var tvProgress: TextView
    private lateinit var imgBtnNext: ImageView
    private lateinit var imgBtnPrev: ImageView
    private lateinit var imgSoundToggle: ImageView
    private lateinit var dotsLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var llAdView: RelativeLayout
    private lateinit var llAdViewFacebook: LinearLayout

    private var categoryPosition = 0
    private var selectedPosition = 0
    private lateinit var currentData: List<LearningDataModel>
    private lateinit var textToSpeech: TextToSpeech
    private var isSoundOn = true
    private val favorites = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)
        supportActionBar?.hide()

        context = this
        initViews()
        setupTextToSpeech()
        handleIntent()
        setupViewPager()
        setupDotsIndicator()
        updateProgress()
        updateSoundButton()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tvItemName = findViewById(R.id.tvItemName)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        tvProgress = findViewById(R.id.tvProgress)
        imgBtnNext = findViewById(R.id.imgBtnNext)
        imgBtnPrev = findViewById(R.id.imgBtnPrev)
        imgSoundToggle = findViewById(R.id.imgSoundToggle)
        dotsLayout = findViewById(R.id.dotsLayout)
        progressBar = findViewById(R.id.progressBar)
        llAdView = findViewById(R.id.llAdView)
        llAdViewFacebook = findViewById(R.id.llAdViewFacebook)

        // Load ads if needed
        // Utils.loadBannerAd(this, llAdView, llAdViewFacebook)

        // Set initial sound state from preferences
        isSoundOn = Utils.getPref(Constant.SOUND, true)
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
        loadCategoryData(categoryPosition)
        updateCategoryTitle()
    }

    private fun loadCategoryData(categoryIndex: Int) {
        currentData = when (categoryIndex) {
            0 -> AlphabetData.getData()
            1 -> NumberData.getData()
            2 -> ColorData.getData()
            3 -> ShapeData.getData()
            4 -> AnimalData.getData()
            5 -> BirdData.getData()
            6 -> FlowerData.getData()
            7 -> FruitData.getData()
            8 -> MonthData.getData()
            9 -> VegetableData.getData()
            10 -> BodyPartData.getData()
            11 -> ClothesData.getData()
            12 -> CountryData.getData()
            13 -> FoodData.getData()
            14 -> GeometryData.getData()
            15 -> HouseItemData.getData()
            16 -> ProfessionData.getData()
            17 -> SchoolItemData.getData()
            18 -> SportData.getData()
            19 -> VehicleData.getData()
            else -> AlphabetData.getData()
        }
    }

    private fun updateCategoryTitle() {
        val categoryTitle = when (categoryPosition) {
            0 -> "Alphabet Fun 🎵"
            1 -> "Numbers Counting 🔢"
            2 -> "Color World 🎨"
            3 -> "Shapes Adventure 🔷"
            4 -> "Animal Kingdom 🐾"
            5 -> "Bird Paradise 🦜"
            6 -> "Flower Garden 🌸"
            7 -> "Fruit Market 🍎"
            8 -> "Months of Year 📅"
            9 -> "Vegetable Farm 🥦"
            10 -> "My Body 👶"
            11 -> "Clothing Store 👕"
            12 -> "World Tour 🌍"
            13 -> "Food Fun 🍕"
            14 -> "Geometry World 📐"
            15 -> "House Items 🏠"
            16 -> "Professions 👨‍⚕️"
            17 -> "School Time 🎒"
            18 -> "Sports Game ⚽"
            19 -> "Vehicles World 🚗"
            else -> "Learning Fun! 🎓"
        }
        tvCategoryTitle.text = categoryTitle
    }

    private fun setupViewPager() {
        val adapter = LearningPagerAdapter(context, currentData)
        viewPager.adapter = adapter
        viewPager.currentItem = selectedPosition

        updateUI(selectedPosition)
        setupViewPagerListener()
    }

    private fun setupDotsIndicator() {
        dotsLayout.removeAllViews()

        for (i in currentData.indices) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.dot_size),
                resources.getDimensionPixelSize(R.dimen.dot_size)
            )
            params.setMargins(
                resources.getDimensionPixelSize(R.dimen.dot_margin),
                0,
                resources.getDimensionPixelSize(R.dimen.dot_margin),
                0
            )
            imageView.layoutParams = params
            imageView.setImageResource(
                if (i == selectedPosition) R.drawable.dot_active else R.drawable.dot_inactive
            )
            dotsLayout.addView(imageView)
        }
    }

    private fun updateDots(position: Int) {
        for (i in 0 until dotsLayout.childCount) {
            val imageView = dotsLayout.getChildAt(i) as ImageView
            imageView.setImageResource(
                if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }

    private fun updateUI(position: Int) {
        tvItemName.text = currentData[position].showTitle
        tvItemName.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotation))
        YoYo.with(Techniques.RubberBand).duration(1200).repeat(1).playOn(tvItemName)

        if (isSoundOn) {
            speakText(currentData[position].speakTitle)
        }

        // Update navigation buttons visibility
        updateNavigationButtons()
    }

    private fun updateProgress() {
        val current = viewPager.currentItem + 1
        val total = currentData.size
        tvProgress.text = "$current/$total"
        progressBar.max = total
        progressBar.progress = current
    }

    private fun updateNavigationButtons() {
        // Show/hide previous button
        imgBtnPrev.alpha = if (viewPager.currentItem > 0) 1.0f else 0.3f
        imgBtnPrev.isEnabled = viewPager.currentItem > 0

        // Show/hide next button
        imgBtnNext.alpha = if (viewPager.currentItem < currentData.size - 1) 1.0f else 0.3f
        imgBtnNext.isEnabled = viewPager.currentItem < currentData.size - 1
    }

    private fun updateSoundButton() {
        val soundIcon = if (isSoundOn) R.drawable.ic_sound_on else R.drawable.ic_sound_off
        imgSoundToggle.setImageResource(soundIcon)
    }

    private fun setupViewPagerListener() {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Optional: Handle page scroll if needed
            }

            override fun onPageSelected(position: Int) {
                updateUI(position)
                updateDots(position)
                updateProgress()
                updateNavigationButtons()
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Optional: Handle state change if needed
            }
        })
    }

    private fun speakText(text: String) {
        if (isSoundOn) {
            AppControl.textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    //region Click Handlers

    fun onClickBack(view: View) {
        YoYo.with(Techniques.FadeOut).duration(300).playOn(view)
        finish()
    }

    fun onClickPrev(view: View) {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
            YoYo.with(Techniques.BounceIn).duration(500).playOn(view)
        }
    }

    fun onClickNext(view: View) {
        if (viewPager.currentItem < currentData.size - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
            YoYo.with(Techniques.BounceIn).duration(500).playOn(view)
        }
    }

    fun onClickSoundToggle(view: View) {
        isSoundOn = !isSoundOn
        updateSoundButton()
        Utils.setPref(Constant.SOUND, isSoundOn)

        val message = if (isSoundOn) "Sound ON 🔊" else "Sound OFF 🔇"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        YoYo.with(Techniques.Pulse).duration(300).playOn(view)
    }

    fun onClickRepeat(view: View) {
        val currentPosition = viewPager.currentItem
        speakText(currentData[currentPosition].speakTitle)

        YoYo.with(Techniques.Bounce).duration(500).playOn(view)
        Toast.makeText(this, "Repeating... 🔁", Toast.LENGTH_SHORT).show()
    }

    fun onClickStar(view: View) {
        val currentPosition = viewPager.currentItem
        val currentItem = currentData[currentPosition]

        // Toggle favorite state
        val isFavorite = toggleFavorite(currentItem)
        val starIcon = if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        (view as ImageButton).setImageResource(starIcon)

        val message = if (isFavorite) "Added to favorites ⭐" else "Removed from favorites"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        YoYo.with(Techniques.Pulse).duration(300).playOn(view)
    }

    fun onClickQuiz(view: View) {
        startQuizActivity()
        YoYo.with(Techniques.RubberBand).duration(500).playOn(view)
    }

    //endregion

    private fun toggleFavorite(item: LearningDataModel): Boolean {
        val itemId = item.image // Using image resource ID as unique identifier
        return if (favorites.contains(itemId)) {
            favorites.remove(itemId)
            false
        } else {
            favorites.add(itemId)
            true
        }
    }

    private fun startQuizActivity() {
        // You can implement quiz activity here
        Toast.makeText(this, "Quiz Time! 🎯\nComing Soon!", Toast.LENGTH_LONG).show()

        // Example: Start quiz activity
        /*
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("categoryPosition", categoryPosition)
            putExtra("categoryTitle", tvCategoryTitle.text.toString())
        }
        startActivity(intent)
        */
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    // Helper function to get category name
    private fun getCategoryName(position: Int): String {
        return when (position) {
            0 -> "Alphabet"
            1 -> "Numbers"
            2 -> "Colors"
            3 -> "Shapes"
            4 -> "Animals"
            5 -> "Birds"
            6 -> "Flowers"
            7 -> "Fruits"
            8 -> "Months"
            9 -> "Vegetables"
            10 -> "Body Parts"
            11 -> "Clothes"
            12 -> "Countries"
            13 -> "Foods"
            14 -> "Geometry"
            15 -> "House Items"
            16 -> "Professions"
            17 -> "School Items"
            18 -> "Sports"
            19 -> "Vehicles"
            else -> "Unknown"
        }
    }
}