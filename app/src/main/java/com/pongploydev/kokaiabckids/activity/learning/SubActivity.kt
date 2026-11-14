package com.pongploydev.kokaiabckids.activity.learning

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.GridLayoutManager
import com.pongploydev.kokaiabckids.utils.ScreenUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pongploydev.kokaiabckids.BuildConfig
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.adapter.SubHomeAdapter
import com.pongploydev.kokaiabckids.confixdata.DataManager
import com.pongploydev.kokaiabckids.model.LearningDataModel
import com.pongploydev.videowatchfeaturesupabase.Constant
import com.pongploydev.videowatchfeaturesupabase.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.AdsPref
import com.pongploydev.videowatchfeaturesupabase.model.Video

import java.util.Locale

class SubActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        private const val REAL_INTERSTITIAL = "ca-app-pub-3279793802315716/8581063137"

        // TTS Constants
        private const val TTS_UTTERANCE_ID = "AutoReadTTS"
        private const val AUTO_READ_DELAY_MS = 2000L
        private const val AUTO_READ_INITIAL_DELAY_MS = 1000L

        // Ad show interval (show ad every 3 clicks)
        private const val AD_SHOW_INTERVAL = 3
    }

    // ============================================
    // UI COMPONENTS
    // ============================================
    var context: Context? = null
    var txtTitleSubHome: TextView? = null
    var rvSubHome: RecyclerView? = null

    // BGM Controls
    private var imgMusicToggle: android.widget.ImageView? = null
    private var btnMusicToggleContainer: View? = null

    // Auto Read UI Controls
    private lateinit var btnStartAutoRead: Button
    private var btnStopAutoRead: Button? = null
    private lateinit var layoutProgress: LinearLayout
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar

    // ============================================
    // DATA VARIABLES
    // ============================================
    var position: Int = 0
    var TYPE: Int = 0
    var learningDataModelArrayList: ArrayList<LearningDataModel?>? = null
    var subHomeAdapter: SubHomeAdapter? = null

    // ============================================
    // TTS & AUTO READ VARIABLES
    // ============================================
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsInitialized = false
    private var isAutoReading = false
    private var currentReadIndex = 0
    private var autoReadHandler = Handler(Looper.getMainLooper())
    private var autoReadRunnable: Runnable? = null
    private val adsPref by lazy { AdsPref(this) }

    // ============================================
    // ADS VARIABLES (INTERSTITIAL ONLY)
    // ============================================
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoaded = false
    private var adLoadRetryCount = 0
    private val MAX_RETRY_COUNT = 3
    private var userClickCount = 0 // Track user clicks for ad interval

    // Pending data
    private var pendingCategoryPosition: Int = -1
    private var pendingSelectedPosition: Int = -1
    private var pendingLearningData: LearningDataModel? = null

    private val currentAdUnitId: String
        get() = if (adsPref.isTestMode) TEST_INTERSTITIAL else REAL_INTERSTITIAL

    // ============================================
    // LIFECYCLE METHODS
    // ============================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)
        context = this

        // Initialize UI
        initUI()

        // Initialize TTS
        initializeTTS()

        // Setup Families Request Configuration
        setupFamiliesRequestConfiguration()

        // Initialize Ads (Interstitial only - NO BANNER)
        initializeAds()

        // Load data
        loadIntentData()
        prepareDataForLearning(position)
    }

    override fun onResume() {
        super.onResume()
        updateBgmButtonState()
        val isMusicEnabled = com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().isMusicEnabled(this)
        if (isMusicEnabled) {
            com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().startMusic(this)
        }
        // Resume TTS if needed
        if (isTtsInitialized && isAutoReading) {
            updateProgressUI()
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause auto-reading and music when activity pauses
        stopAutoRead()
        com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().pauseMusic()
    }

    // ============================================
    // SOUND VARIABLES
    // ============================================
    private var soundPool: android.media.SoundPool? = null
    private var soundClickId: Int = 0
    private var soundSuccessId: Int = 0
    private var isSoundLoaded = false

    override fun onDestroy() {
        // Shutdown TTS and stop auto-reading
        stopAutoRead()
        shutdownTTS()

        // Release SoundPool
        soundPool?.release()
        soundPool = null

        // Clear Ads
        interstitialAd = null
        isAdLoaded = false

        super.onDestroy()
    }

    // ============================================
    // UI INITIALIZATION
    // ============================================
    private fun initUI() {
        rvSubHome = findViewById(R.id.rvSubHome)
        txtTitleSubHome = findViewById(R.id.txtTitleSubHome)

        // Initialize Sounds
        initSounds()

        // Initialize BGM Controls
        initBgmControls()

        // Initialize Auto Read Controls
        initAutoReadControls()
    }

    private fun initSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes =
                    android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_GAME)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            soundPool =
                    android.media.SoundPool.Builder()
                            .setMaxStreams(5)
                            .setAudioAttributes(audioAttributes)
                            .build()
        } else {
            @Suppress("DEPRECATION")
            soundPool = android.media.SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0)
        }

        try {
            soundClickId = soundPool?.load(this, R.raw.button, 1) ?: 0
            soundSuccessId = soundPool?.load(this, R.raw.is_sound_success_sound, 1) ?: 0

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) isSoundLoaded = true
            }
        } catch (e: Exception) {
            Log.e("SubActivity", "Error loading sounds: ${e.message}")
        }
    }

    private fun playClickSound() {
        if (isSoundLoaded) {
            soundPool?.play(soundClickId, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun playSuccessSound() {
        if (isSoundLoaded) {
            soundPool?.play(soundSuccessId, 1f, 1f, 0, 0, 1f)
        }
    }

    /** Initialize BGM Controls */
    private fun initBgmControls() {
        imgMusicToggle = findViewById(R.id.imgMusicToggle)
        btnMusicToggleContainer = findViewById(R.id.btnMusicToggleContainer)

        updateBgmButtonState()

        btnMusicToggleContainer?.setOnClickListener {
            val isEnabled = com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().isMusicEnabled(this)
            val newStatus = !isEnabled
            com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().setMusicEnabled(this, newStatus)
            if (newStatus) {
                com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().startMusic(this)
                showToast("เปิดเสียงดนตรีประกอบ 🎵")
            } else {
                com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().stopMusic()
                showToast("ปิดเสียงดนตรีประกอบ 🔇")
            }
            updateBgmButtonState()
        }
    }

    private fun updateBgmButtonState() {
        val isEnabled = com.pongploydev.kokaiabckids.utils.MusicManager.getInstance().isMusicEnabled(this)
        if (isEnabled) {
            imgMusicToggle?.setImageResource(R.drawable.ic_music_on)
            imgMusicToggle?.alpha = 1.0f
        } else {
            imgMusicToggle?.setImageResource(R.drawable.ic_music_off)
            imgMusicToggle?.alpha = 0.5f
        }
    }

    /** Initialize Auto Read Controls */
    private fun initAutoReadControls() {
        btnStartAutoRead = findViewById(R.id.btn_start_auto_read)
        btnStopAutoRead = findViewById(R.id.btn_stop_auto_read)
        layoutProgress = findViewById(R.id.layout_progress)
        tvProgress = findViewById(R.id.tv_progress)
        progressBar = findViewById(R.id.progress_bar)

        // Setup Auto Read Listeners
        setupAutoReadListeners()

        // Initially update button text and state
        updateAutoReadButtons()
    }

    /** Setup Auto Read Listeners */
    private fun setupAutoReadListeners() {
        btnStartAutoRead.setOnClickListener {
            if (isAutoReading) {
                stopAutoRead()
            } else {
                startAutoRead()
            }
        }

        btnStopAutoRead?.setOnClickListener { stopAutoRead() }

        btnStartAutoRead.setOnLongClickListener {
            showAutoReadSettingsDialog()
            true
        }
    }

    private fun loadIntentData() {
        val intent = intent
        position = intent.getIntExtra("categoryPosition", 0)
        val category = intent.getStringExtra("Category") ?: "เรียนรู้"
        TYPE = intent.getIntExtra("Type", 0)

        txtTitleSubHome?.text = category
        com.pongploydev.kokaiabckids.log.AnalyticsLogger.logScreenView(this, "หน้าเรียนรู้_$category", "SubActivity")
    }

    // ============================================
    // TEXT-TO-SPEECH METHODS
    // ============================================

    /** Initialize Text-to-Speech Engine */
    private fun initializeTTS() {
        try {
            textToSpeech = TextToSpeech(this, this)

            textToSpeech.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            runOnUiThread {
                                Log.d("AutoRead", "Speech started for item: $currentReadIndex")
                            }
                        }

                        override fun onDone(utteranceId: String?) {
                            runOnUiThread {
                                Log.d("AutoRead", "Speech completed for item: $currentReadIndex")

                                if (isAutoReading && learningDataModelArrayList != null) {
                                    if (currentReadIndex < learningDataModelArrayList!!.size - 1) {
                                        currentReadIndex++
                                        scheduleNextItem()
                                    } else {
                                        onAutoReadComplete()
                                    }
                                }
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            runOnUiThread {
                                Log.e("AutoRead", "Speech error at item: $currentReadIndex")

                                if (isAutoReading && learningDataModelArrayList != null) {
                                    if (currentReadIndex < learningDataModelArrayList!!.size - 1) {
                                        currentReadIndex++
                                        scheduleNextItem()
                                    } else {
                                        onAutoReadComplete()
                                    }
                                }
                            }
                        }
                    }
            )
        } catch (e: Exception) {
            Log.e("AutoRead", "Error initializing TTS: ${e.message}")
            showToast("ระบบอ่านเสียงไม่พร้อมใช้งาน")
        }
    }

    /** TTS Initialization Callback */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("AutoRead", "Language not supported")
                showToast("ภาษาอังกฤษไม่รองรับ")
            } else {
                isTtsInitialized = true
                applyTtsSettings()

                Log.d("AutoRead", "TTS initialized successfully")

                runOnUiThread {
                    btnStartAutoRead.isEnabled = true
                    showToast("ระบบอ่านเสียงพร้อมใช้งาน! 🎉")
                }
            }
        } else {
            Log.e("AutoRead", "TTS initialization failed")
            showToast("ระบบอ่านเสียงล้มเหลว")
            runOnUiThread {
                btnStartAutoRead.isEnabled = false
                btnStartAutoRead.text = "🚫 ระบบอ่านเสียงไม่พร้อม"
            }
        }
    }

    /** Apply TTS settings */
    private fun applyTtsSettings() {
        if (isTtsInitialized) {
            textToSpeech.setSpeechRate(0.85f)

            val result = textToSpeech.setLanguage(Locale("th", "TH"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                textToSpeech.setLanguage(Locale.US)
            }

            textToSpeech.setPitch(1.0f)
        }
    }

    /** Speak a learning item */
    private fun speakItem(learningData: LearningDataModel, index: Int) {
        if (!isTtsInitialized) {
            showToast("ระบบอ่านเสียงไม่พร้อม")
            return
        }

        try {
            textToSpeech.stop()

            val textToSpeak = learningData.showTitle ?: "Item"
            val fullText =
                    if (index == 0) {
                        "Let's learn together! $textToSpeak"
                    } else {
                        textToSpeak
                    }

            highlightCurrentItem(index)
            updateProgress(index + 1)

            // เลื่อนหน้าจอตามตัวอักษรที่กำลังอ่านอัตโนมัติ
            rvSubHome?.smoothScrollToPosition(index)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(
                        fullText,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "$TTS_UTTERANCE_ID$index"
                )
            } else {
                @Suppress("DEPRECATION")
                textToSpeech.speak(fullText, TextToSpeech.QUEUE_FLUSH, null)
            }

            showToast("กำลังอ่าน: $textToSpeak")
        } catch (e: Exception) {
            Log.e("AutoRead", "Error speaking: ${e.message}")
        }
    }

    /** Shutdown TTS */
    private fun shutdownTTS() {
        if (isTtsInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
            isTtsInitialized = false
        }
    }

    // ============================================
    // AUTO READ METHODS
    // ============================================

    /** Start Auto Read from A-Z */
    private fun startAutoRead() {
        if (!isTtsInitialized) {
            showToast("กรุณารอระบบอ่านเสียงเตรียมตัว")
            return
        }

        if (learningDataModelArrayList.isNullOrEmpty()) {
            showToast("ไม่มีข้อมูลให้อ่าน")
            return
        }

        if (isAutoReading) {
            showToast("กำลังอ่านอยู่แล้ว")
            return
        }

        isAutoReading = true
        currentReadIndex = 0

        updateAutoReadButtons()
        layoutProgress.visibility = View.VISIBLE

        val startMsg = if (position == 0) "เริ่มอ่าน A-Z อัตโนมัติ! 🎯" else "เริ่มอ่านอัตโนมัติ! 🎯"
        showToast(startMsg)

        autoReadHandler.postDelayed(
                {
                    if (isAutoReading) {
                        speakItem(
                                learningDataModelArrayList!![currentReadIndex]!!,
                                currentReadIndex
                        )
                    }
                },
                AUTO_READ_INITIAL_DELAY_MS
        )
    }

    /** Stop Auto Read */
    private fun stopAutoRead() {
        if (!isAutoReading) return

        isAutoReading = false
        autoReadRunnable?.let { autoReadHandler.removeCallbacks(it) }

        resetItemHighlight()
        updateAutoReadButtons()
        showToast("หยุดอ่านแล้ว ⏹️")
    }

    /** Schedule next item for auto read */
    private fun scheduleNextItem() {
        if (!isAutoReading || learningDataModelArrayList.isNullOrEmpty()) return

        autoReadRunnable?.let { autoReadHandler.removeCallbacks(it) }

        autoReadRunnable = Runnable {
            if (isAutoReading && currentReadIndex < learningDataModelArrayList!!.size) {
                speakItem(learningDataModelArrayList!![currentReadIndex]!!, currentReadIndex)
            }
        }

        autoReadHandler.postDelayed(autoReadRunnable!!, AUTO_READ_DELAY_MS)
    }

    /** Called when auto read completes */
    private fun onAutoReadComplete() {
        isAutoReading = false
        resetItemHighlight()
        updateAutoReadButtons()

        // Play success sound
        playSuccessSound()

        val totalItems = learningDataModelArrayList?.size ?: 0

        // Show Success Dialog
        showSuccessDialog(totalItems)

        if (isTtsInitialized) {
            autoReadHandler.postDelayed(
                    {
                        textToSpeech.speak(
                                "Excellent! You've completed all letters!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "complete"
                        )
                    },
                    1000
            )
        }
    }

    private fun showSuccessDialog(totalItems: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("เก่งมาก! 🎉")
        builder.setMessage("หนูเก่งมากเลย อ่านครบ $totalItems คำแล้ว!\nมาฝึกอ่านกันบ่อยๆ นะ")
        builder.setPositiveButton("เย้! ตกลง") { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        // Style the button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let { button ->
            button.textSize = 20f
            button.setTextColor(resources.getColor(R.color.colorTheme))
        }
    }

    /** Highlight current item in RecyclerView */
    private fun highlightCurrentItem(index: Int) {
        rvSubHome?.smoothScrollToPosition(index)
        subHomeAdapter?.setCurrentReadingIndex(index)
    }

    /** Reset item highlight */
    private fun resetItemHighlight() {
        subHomeAdapter?.setCurrentReadingIndex(-1)
    }

    /** Update progress UI */
    private fun updateProgress(current: Int) {
        val total = learningDataModelArrayList?.size ?: 26

        runOnUiThread {
            tvProgress.text = "$current/$total"
            progressBar.max = total
            progressBar.progress = current

            val progressPercent = (current.toFloat() / total.toFloat()) * 100
            when {
                progressPercent < 33 -> {
                    tvProgress.setTextColor(resources.getColor(R.color.progress_low))
                }
                progressPercent < 66 -> {
                    tvProgress.setTextColor(resources.getColor(R.color.progress_medium))
                }
                else -> {
                    tvProgress.setTextColor(resources.getColor(R.color.progress_high))
                }
            }
        }
    }

    /** Update progress UI based on current state */
    private fun updateProgressUI() {
        updateProgress(currentReadIndex + 1)
    }

    /** Update auto read buttons state */
    private fun updateAutoReadButtons() {
        runOnUiThread {
            btnStartAutoRead.isEnabled = isTtsInitialized

            if (isAutoReading) {
                btnStartAutoRead.text = "⏹️ กำลังอ่าน... แตะเพื่อหยุด"
                btnStartAutoRead.setBackgroundResource(R.drawable.bg_auto_read_toggle_stop)
                layoutProgress.visibility = View.VISIBLE
            } else {
                val isAlphabet = (position == 0)
                btnStartAutoRead.text = if (isAlphabet) "🚀 เริ่มอ่าน A-Z อัตโนมัติ" else "▶️ เริ่มอ่านอัตโนมัติ"
                btnStartAutoRead.setBackgroundResource(R.drawable.bg_auto_read_toggle_play)
                layoutProgress.visibility = if (currentReadIndex > 0) View.VISIBLE else View.GONE
            }
        }
    }

    /** Show auto read settings dialog */
    private fun showAutoReadSettingsDialog() {
        AlertDialog.Builder(this)
                .setTitle("⚙️ ตั้งค่าการอ่านอัตโนมัติ")
                .setPositiveButton("ตกลง", null)
                .setNegativeButton("ยกเลิก", null)
                .setNeutralButton("ทดสอบเสียง") { _, _ ->
                    if (isTtsInitialized) {
                        textToSpeech.speak(
                                "This is a test sound",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "test"
                        )
                    }
                }
                .show()
    }

    // ============================================
    // ADS METHODS (INTERSTITIAL ONLY)
    // ============================================
    private fun initializeAds() {
        if (!isNetworkAvailable()) {
            showToast("No internet connection")
            return
        }

        MobileAds.initialize(this) { initializationStatus ->
            logDebug("=== ADS SDK INITIALIZATION ===")

            var hasError = false
            initializationStatus.adapterStatusMap.forEach { (adapter, status) ->
                logDebug("Adapter: $adapter | State: ${status.initializationState}")
                if (status.initializationState.toString() != "READY") {
                    hasError = true
                }
            }

            if (hasError) {
                logDebug("⚠️ Some adapters failed to initialize")
            } else {
                logDebug("✅ All adapters initialized successfully")
            }

            if (BuildConfig.DEBUG) {
                configureTestDevicesForDebug()
            }

            loadInterstitialAd()
        }
    }

    private fun configureTestDevicesForDebug() {
        try {
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            val testDeviceIds = mutableListOf<String>()
            testDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR)

            deviceId?.let {
                logDebug("Your Test Device ID: $it")
                testDeviceIds.add(it)
            }

            val requestConfiguration =
                    MobileAds.getRequestConfiguration()
                            .toBuilder()
                            .setTestDeviceIds(testDeviceIds)
                            .build()

            MobileAds.setRequestConfiguration(requestConfiguration)

            logDebug("Test devices configured: $testDeviceIds")
        } catch (e: Exception) {
            logDebug("Error configuring test devices: ${e.message}")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            @Suppress("DEPRECATION") val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    private fun loadInterstitialAd() {
        if (adLoadRetryCount >= MAX_RETRY_COUNT) {
            logDebug("Max retry count reached")
            return
        }

        adLoadRetryCount++
        logDebug("Loading interstitial ad (Attempt $adLoadRetryCount)")
        logDebug("Using Ad Unit ID: $currentAdUnitId")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
                this,
                currentAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        logDebug("✅ INTERSTITIAL AD LOADED SUCCESSFULLY!")
                        interstitialAd = ad
                        isAdLoaded = true
                        adLoadRetryCount = 0

                        ad.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        logDebug("Ad dismissed")
                                        interstitialAd = null
                                        isAdLoaded = false
                                        loadInterstitialAd()
                                        openFullScreenActivityAfterAd()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(
                                            adError: AdError
                                    ) {
                                        logDebug("Ad failed to show: ${adError.message}")
                                        interstitialAd = null
                                        isAdLoaded = false
                                        loadInterstitialAd()
                                        openFullScreenActivityAfterAd()
                                    }

                                    override fun onAdImpression() {
                                        logDebug("Ad impression")
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        logDebug("Ad showed")
                                    }
                                }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        logDebug("❌ Real Interstitial Ad Failed To Load: ${loadAdError.message} (Error Code: ${loadAdError.code})")
                        interstitialAd = null
                        isAdLoaded = false

                        if (adLoadRetryCount < MAX_RETRY_COUNT) {
                            adLoadRetryCount++
                            val delay = adLoadRetryCount * 3000L
                            rvSubHome?.postDelayed({ loadInterstitialAd() }, delay)
                        }
                    }
                }
        )
    }

    private fun setupFamiliesRequestConfiguration() {
        try {
            val builder = RequestConfiguration.Builder()

            if (adsPref.isFamiliesApp) {
                Log.d("FamiliesConfig", "Applying Families-specific configuration")
                builder.setTagForChildDirectedTreatment(
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                )
                builder.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                Log.d("FamiliesConfig", "✅ Families configuration applied")
            }

            if (adsPref.isTestMode) {
                builder.setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                Log.d("FamiliesConfig", "🧪 Test mode enabled")
            }

            MobileAds.setRequestConfiguration(builder.build())
            Log.d("FamiliesConfig", "RequestConfiguration set")
        } catch (e: Exception) {
            Log.e("FamiliesConfig", "❌ Error: ${e.message}")
        }
    }

    private fun showInterstitialAd() {
        if (isAdLoaded && interstitialAd != null) {
            logDebug("Showing interstitial ad...")
            interstitialAd?.show(this)
        } else {
            logDebug("No ad available, skipping...")
            openFullScreenActivityAfterAd()

            if (!isAdLoaded) {
                loadInterstitialAd()
            }
        }
    }

    // ============================================
    // DATA METHODS
    // ============================================
    fun prepareDataForLearning(categoryId: Int) {
        val dataList = DataManager.getDataByCategory(categoryId)
        learningDataModelArrayList = ArrayList()
        dataList.forEach { learningDataModelArrayList?.add(it) }

        progressBar.max = learningDataModelArrayList?.size ?: 26

        setRvAdapter()
    }

    private fun setRvAdapter() {
        val spanCount = ScreenUtils.getGridSpanCount(this)
        val gridLayoutManager = GridLayoutManager(this, spanCount, GridLayoutManager.VERTICAL, false)
        rvSubHome?.layoutManager = gridLayoutManager

        subHomeAdapter =
                SubHomeAdapter(
                        context = this,
                        learningDataList = learningDataModelArrayList?.filterNotNull()
                                        ?: emptyList(),
                        categoryPosition = position,
                        displayType = TYPE,
                        onItemClick = { categoryPos, selectedPos, learningData ->
                            handleItemClick(categoryPos, selectedPos, learningData)
                        },
                        onItemLongClick = { categoryPos, selectedPos, learningData ->
                            showOptionsMenu(learningData)
                            true
                        },
                        onItemFavoriteClick = { categoryPos, selectedPos, learningData ->
                            handleFavoriteClick(selectedPos, learningData)
                        }
                )

        rvSubHome?.adapter = subHomeAdapter
    }

    /** Handle item click */
    private fun handleItemClick(
            categoryPos: Int,
            selectedPos: Int,
            learningData: LearningDataModel
    ) {
        // Play click sound
        playClickSound()

        // Stop auto-reading if active
        if (isAutoReading) {
            stopAutoRead()
        }

        // Set pending data
        pendingCategoryPosition = categoryPos
        pendingSelectedPosition = selectedPos
        pendingLearningData = learningData

        // Speak the clicked item
        if (isTtsInitialized) {
            speakItem(learningData, selectedPos)
        }

        // Track learning progress & add star in Supabase
        val supabaseUser = SupabaseAuthManager.getInstance(this).getStoredUser()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = supabaseUser?.id ?: firebaseUser?.uid ?: "guest_user"
        if (!userId.isNullOrEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val dataManager = SupabaseDataManager.getInstance(this@SubActivity)
                dataManager.saveLearningProgress(
                    userId = userId,
                    categoryKey = "cat_$categoryPos",
                    itemKey = learningData.showTitle ?: learningData.speakTitle ?: "item_$selectedPos",
                    score = 100
                )
                dataManager.addStars(userId, 1)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    Toast.makeText(this@SubActivity, "บันทึกคะแนนสำเร็จแล้ว! ⭐ +1", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Track user clicks for ad interval
        userClickCount++

        // Show ad every AD_SHOW_INTERVAL clicks (e.g., every 3rd click)
        if (userClickCount >= AD_SHOW_INTERVAL && isAdLoaded) {
            userClickCount = 0
            showInterstitialAd()
        } else {
            // No ad shown, directly open activity
            openFullScreenActivityAfterAd()
        }
    }

    // ============================================
    // NAVIGATION METHODS
    // ============================================
    private fun openFullScreenActivityAfterAd() {
        pendingLearningData?.let { learningData ->
            val intent =
                    Intent(this, FullScreenActivity::class.java).apply {
                        putExtra("categoryPosition", pendingCategoryPosition)
                        putExtra("selectedPosition", pendingSelectedPosition)
                        putExtra("imageUrl", learningData.image)
                        putExtra("title", learningData.showTitle)
                    }
            startActivity(intent)
            resetPendingData()
        }
    }

    private fun resetPendingData() {
        pendingCategoryPosition = -1
        pendingSelectedPosition = -1
        pendingLearningData = null
    }

    // ============================================
    // UTILITY METHODS
    // ============================================
    private fun showOptionsMenu(learningData: LearningDataModel) {
        val options =
                arrayOf(
                        "🎬 ดูวิดีโอ (Watch Video)",
                        "🔊 อ่านรายการนี้",
                        "⭐ เพิ่มรายการโปรด",
                        "↗️ แชร์",
                        "ℹ️ ข้อมูล",
                        if (isAutoReading) "⏸️ หยุดอ่านชั่วคราว" else "🚀 อ่านทั้งหมดอัตโนมัติ"
                )

        AlertDialog.Builder(this)
                .setTitle("ตัวเลือก: ${learningData.showTitle}")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val index =
                                    learningDataModelArrayList?.indexOfFirst {
                                        it?.showTitle == learningData.showTitle
                                    }
                                            ?: -1
                            if (index != -1) {
                                startVideoActivity(index)
                            } else {
                                showToast("ไม่พบวิดีโอสำหรับรายการนี้")
                            }
                        }
                        1 -> {
                            if (isTtsInitialized) {
                                val index =
                                        learningDataModelArrayList?.indexOfFirst {
                                            it?.showTitle == learningData.showTitle
                                        }
                                                ?: -1

                                if (index != -1) {
                                    speakItem(learningData, index)
                                }
                            }
                        }
                        2 -> addToFavorite(learningData)
                        3 -> shareItem(learningData)
                        4 -> showInfo(learningData)
                        5 -> {
                            if (isAutoReading) {
                                stopAutoRead()
                            } else {
                                startAutoRead()
                            }
                        }
                    }
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
    }

    private fun handleFavoriteClick(position: Int, learningData: LearningDataModel) {
        subHomeAdapter?.toggleFavorite(position)
        showToast(
                "${learningData.showTitle} ${if (isFavorite(position)) "ลบออกจาก" else "เพิ่มเข้า"}รายการโปรด"
        )
    }

    private fun isFavorite(position: Int): Boolean = false

    private fun addToFavorite(learningData: LearningDataModel) {
        showToast("เพิ่มในรายการโปรด: ${learningData.showTitle}")
    }

    private fun shareItem(learningData: LearningDataModel) {
        val shareIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                            Intent.EXTRA_TEXT,
                            "เรียนรู้น่ารักกับ: ${learningData.showTitle}\nจากแอปเรียนรู้ของเรา!"
                    )
                    type = "text/plain"
                }
        startActivity(Intent.createChooser(shareIntent, "แชร์ผ่าน"))
    }

    private fun showInfo(learningData: LearningDataModel) {
        AlertDialog.Builder(this)
                .setTitle(learningData.showTitle)
                .setMessage(
                        "รายละเอียด: ${learningData.toString() ?: "ไม่มีรายละเอียดเพิ่มเติม"}\n\nคลิกปุ่ม 🔊 เพื่อฟังการออกเสียง"
                )
                .setPositiveButton("ตกลง", null)
                .setNeutralButton("🔊 อ่าน") { _, _ ->
                    if (isTtsInitialized) {
                        val index =
                                learningDataModelArrayList?.indexOfFirst {
                                    it?.showTitle == learningData.showTitle
                                }
                                        ?: -1

                        if (index != -1) {
                            speakItem(learningData, index)
                        }
                    }
                }
                .show()
    }

    fun onClickBack(view: View?) {
        stopAutoRead()
        finish()
    }

    // ============================================
    // HELPER METHODS
    // ============================================
    private fun startVideoActivity(index: Int) {
        try {
            // Assume category is Thai Consonants (k prefix)
            // Index 0 -> k1
            val resourceName = "k${index + 1}"

            // Construct Video object
            val video =
                    Video().apply {
                        video_type = "local"
                        video_id = resourceName
                        // URI: android.resource://<package>/raw/<name>
                        // Note: using /raw/ name is safer than ID across activities
                        video_url = "android.resource://${packageName}/raw/$resourceName"
                        video_title =
                                learningDataModelArrayList?.getOrNull(index)?.showTitle ?: "Video"
                        video_description = "Learn Thai Alphabet: $video_title"
                    }

            val intent = Intent(this, ActivityVideoAutoDetail::class.java)
            intent.putExtra(Constant.EXTRA_OBJC, video)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("SubActivity", "Error starting video: ${e.message}")
            showToast("ไม่สามารถเปิดวิดีโอได้")
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            println("DEBUG: $message")
        }
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }
}
