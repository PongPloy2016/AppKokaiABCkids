package com.pongploydev.videowatchfeature.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pongploydev.videowatchfeature.Constant
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.utils.Tools
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetail
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.rests.RestAdapter

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class ActivityVideoAutoDetail : AppCompatActivity() {

    private var callbackCall: Call<CallbackVideoDetail>? = null
    private lateinit var lytMainContent: LinearLayout
    private lateinit var video: Video

    private lateinit var txtTitle: TextView
    private lateinit var txtCategory: TextView
    private lateinit var txtDuration: TextView
    private lateinit var txtTotalViews: TextView
    private lateinit var txtDateTime: TextView
    private lateinit var lytView: LinearLayout
    private lateinit var lytDate: LinearLayout
    private lateinit var videoThumbnail: ImageView
    private lateinit var webView: WebView
    private lateinit var parentView: CoordinatorLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytSuggested: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPref: SharedPref
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var adsPref: AdsPref
    private lateinit var tools: Tools

    // ตัวแปรสำหรับ YouTube Player
    private lateinit var youTubePlayerView: YouTubePlayerView
    private var youTubePlayer: YouTubePlayer? = null
    private lateinit var lytThumbnailContainer: RelativeLayout
    private lateinit var progressBar: ProgressBar

    // ตัวแปรสำหรับ SeekBar และการควบคุม
    private lateinit var seekBar: YouTubePlayerSeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalDuration: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnRew10s: ImageButton
    private lateinit var btnFfwd10s: ImageButton
    private var isPlaying = false
    private val seekBarHandler = Handler()
    private var updateSeekBarRunnable: Runnable? = null
    private var videoDuration = 0f

    // ตัวแปรสำหรับ Banner Ad
//    private lateinit var adView: AdView
    private lateinit var bannerContainer: RelativeLayout
    private var isBannerLoading = false
    private var bannerLoadAttempts = 0
    private val maxBannerRetries = 3

    private val seekBarTracker = SeekBarTracker()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_auto_detail)

        video = intent.getParcelableExtra(Constant.EXTRA_OBJC)!!

        tools = Tools(this)
        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)

        // ตั้งค่าโหมดทดสอบ (ระหว่างพัฒนา)
        adsPref.isTestMode = false
        adsPref.isFamiliesApp = true // สำหรับแอปครอบครัว

        // ซ่อน Native Ad สำหรับแอปครอบครัว
//        val nativeAdView = findViewById<LinearLayout>(R.id.native_ad_view)
//        nativeAdView?.visibility = View.GONE

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary)
        swipeRefreshLayout.isRefreshing = false

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
        lytMainContent = findViewById(R.id.lyt_main_content)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        txtTitle = findViewById(R.id.video_title)
        txtCategory = findViewById(R.id.category_name)
        txtDuration = findViewById(R.id.video_duration)
        webView = findViewById(R.id.video_description)
        txtTotalViews = findViewById(R.id.total_views)
        txtDateTime = findViewById(R.id.date_time)
        lytView = findViewById(R.id.lyt_view_count)
        lytDate = findViewById(R.id.lyt_date)
        btnFavorite = findViewById(R.id.img_favorite)
        btnShare = findViewById(R.id.btn_share)
        lytSuggested = findViewById(R.id.lyt_suggested)

        // กำหนดค่าให้กับตัวแปรใหม่
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        lytThumbnailContainer = findViewById(R.id.lyt_thumbnail_container)
        progressBar = findViewById(R.id.progress_bar)

        // กำหนดค่าให้กับตัวแปร SeekBar และการควบคุม
        seekBar = findViewById(R.id.youtube_player_seekbar)
        txtCurrentTime = findViewById(R.id.txt_current_time)
        txtTotalDuration = findViewById(R.id.txt_duration)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnRew10s = findViewById(R.id.btn_rew_10s)
        btnFfwd10s = findViewById(R.id.btn_ffwd_10s)

        // หา Banner Ad View จาก XML
//        adView = findViewById(R.id.adView_banner)
//        bannerContainer = findViewById(R.id.banner_container)

        // ตรวจสอบว่าเป็นแอปครอบครัวหรือไม่
//        if (adsPref.isFamiliesApp) {
//            // สำหรับแอปครอบครัว: ตรวจสอบว่ามีโฆษณาเพียง 1 ตัวต่อหน้า
//            checkFamiliesCompliance()
//        }

        // เริ่มต้น Mobile Ads SDK สำหรับแอปครอบครัว
//        initializeMobileAdsForFamilies()

        requestAction()

        swipeRefreshLayout.setOnRefreshListener {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
            lytMainContent.visibility = View.GONE
            requestAction()
        }

        initToolbar()
        handleOnBackPressed()

        // เริ่มต้น YouTube Player
        initYouTubePlayer()

        // ตั้งค่า SeekBar และการควบคุม
        setupControls()

//        // ตั้งค่า Banner Ad สำหรับแอปครอบครัว (รอให้ Mobile Ads SDK พร้อม)
//        Handler(Looper.getMainLooper()).postDelayed({
//            setupBannerAdForFamilies()
//        }, 1000) // รอ 1 วินาทีให้ SDK พร้อม
    }

    /**
     * เริ่มต้น Mobile Ads SDK สำหรับแอปครอบครัว
     */
    private fun initializeMobileAdsForFamilies() {
        try {
            Log.d("FamiliesAds", "Initializing Mobile Ads SDK for Families app...")

            // ตั้งค่า RequestConfiguration สำหรับแอปครอบครัว
            val requestConfiguration = RequestConfiguration.Builder()
                .apply {
                    if (adsPref.isFamiliesApp) {
                        // ตั้งค่าสำหรับแอปครอบครัว (เด็กอายุต่ำกว่า 13 ปี)
                        setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)

                        // ตั้งค่าโฆษณาที่ปลอดภัยสำหรับครอบครัว
                        setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)

                        // ตั้งค่าระดับเนื้อหาสูงสุดเป็น G (สำหรับทุกวัย)
                        setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)

                        Log.d("FamiliesAds", "Families configuration applied: TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE, MAX_AD_CONTENT_RATING_G")
                    }

                    // เพิ่ม test devices สำหรับการพัฒนา
                    if (adsPref.isTestMode) {
                        // ใช้ test device ID ที่ได้จาก Logcat
                        setTestDeviceIds(listOf(
                            AdRequest.DEVICE_ID_EMULATOR, // Emulator
                            resources.getString(R.string.banner_ad_unit_id_test) // ใส่ Device ID จริงของคุณที่นี่
                        ))
                        Log.d("FamiliesAds", "Test mode enabled with test devices")
                    }
                }
                .build()

            MobileAds.setRequestConfiguration(requestConfiguration)

            // เริ่มต้น Mobile Ads SDK
            MobileAds.initialize(this) { initializationStatus ->
                Log.d("FamiliesAds", "✅ Mobile Ads SDK initialized successfully")

                // ตรวจสอบสถานะ initialization
                logInitializationStatus(initializationStatus)

                // ตรวจสอบ Adapter สำหรับ Families
                if (adsPref.isFamiliesApp) {
                    checkFamiliesAdapters(initializationStatus)
                }

                // เมื่อ SDK พร้อมแล้ว แสดง Banner Ad
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        setupBannerAdForFamilies()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("FamiliesAds", "❌ Failed to initialize Mobile Ads SDK: ${e.message}")
        }
    }

    /**
     * Log initialization status
     */
    private fun logInitializationStatus(initializationStatus: InitializationStatus) {
        val adapterStatusMap = initializationStatus.adapterStatusMap
        Log.d("FamiliesAds", "=== Adapter Status ===")

        for ((adapterClass, status) in adapterStatusMap) {
//            val state = when (status.initializationState) {
//                InitializationStatus.AdapterStatus.State.NOT_READY -> "NOT_READY"
//                InitializationStatus.AdapterStatus.State.READY -> "READY"
//                else -> "UNKNOWN"
//            }
//            Log.d("FamiliesAds", "$adapterClass: $state - ${status.description}")
        }
        Log.d("FamiliesAds", "=====================")
    }

    /**
     * ตรวจสอบ Adapter สำหรับแอปครอบครัว
     */
    private fun checkFamiliesAdapters(initializationStatus: InitializationStatus) {
        val adapterStatusMap = initializationStatus.adapterStatusMap
        var hasGoogleAdapters = false

        for ((adapterClass, _) in adapterStatusMap) {
            Log.d("FamiliesAdapters", "Found adapter: $adapterClass")

            // ตรวจสอบว่าเป็น Google/AdMob adapter หรือไม่
            if (adapterClass.contains("com.google.android.gms.ads") ||
                adapterClass.contains("admob", ignoreCase = true)) {
                hasGoogleAdapters = true
            }
        }

        if (hasGoogleAdapters) {
            Log.d("FamiliesAdapters", "✅ Google AdMob adapter found - Families support available")
        } else {
            Log.w("FamiliesAdapters", "⚠️ No Google AdMob adapter found - Families support may be limited")
        }
    }

    /**
     * ตรวจสอบความสอดคล้องกับ Families Policy
     */
    private fun checkFamiliesCompliance() {
        Log.d("FamiliesPolicy", "=== Families Policy Compliance Check ===")

        val complianceIssues = mutableListOf<String>()

        // 1. ตรวจสอบจำนวนโฆษณาต่อหน้า
        if (adsPref.maxAdsPerPage > 1) {
            complianceIssues.add("Max ads per page should be 1 (currently: ${adsPref.maxAdsPerPage})")
            adsPref.maxAdsPerPage = 1 // แก้ไขอัตโนมัติ
            Log.d("FamiliesPolicy", "Fixed: Set max ads per page to 1")
        }

        // 2. ตรวจสอบ Native Ad (ควรปิดสำหรับ Families)
        if (adsPref.isNativeAdPostDetails) {
            complianceIssues.add("Native ads should be disabled for Families app")
            adsPref.isNativeAdPostDetails = false // ปิดอัตโนมัติ
            Log.d("FamiliesPolicy", "Fixed: Disabled native ads")
        }

        // 3. ตรวจสอบ Interstitial Ad (ควรจำกัด)
        if (adsPref.interstitialAdInterval < 3) {
            complianceIssues.add("Interstitial ads should show less frequently (min interval: 3)")
            adsPref.interstitialAdInterval = 3
            Log.d("FamiliesPolicy", "Fixed: Set interstitial interval to 3")
        }

        // 4. ตรวจสอบ Ad Content Rating
        if (adsPref.adContentRating != "G") {
            complianceIssues.add("Ad content rating should be G (currently: ${adsPref.adContentRating})")
            adsPref.adContentRating = "G"
            Log.d("FamiliesPolicy", "Fixed: Set ad content rating to G")
        }

        // 5. ตรวจสอบ Banner Ad Unit ID
        val bannerAdUnitId = resources.getString(R.string.banner_ad_unit_id)
        if (!isValidFamiliesAdUnit(bannerAdUnitId)) {
            complianceIssues.add("Banner ad unit may not be approved for Families program")
            Log.w("FamiliesPolicy", "Check banner ad unit in AdMob console")
        }

        // Log ผลการตรวจสอบ
        if (complianceIssues.isNotEmpty()) {
            Log.w("FamiliesPolicy", "⚠️ Found ${complianceIssues.size} compliance issues:")
            complianceIssues.forEachIndexed { index, issue ->
                Log.w("FamiliesPolicy", "${index + 1}. $issue")
            }

            // แจ้งเตือน developer (เฉพาะโหมดทดสอบ)
            if (adsPref.isTestMode && !isFinishing) {
                runOnUiThread {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Families Policy: Fixed ${complianceIssues.size} issues",
                        Snackbar.LENGTH_LONG
                    ).setAction("OK") {}.show()
                }
            }
        } else {
            Log.d("FamiliesPolicy", "✅ All Families policy requirements are met")
        }

        Log.d("FamiliesPolicy", "========================================")
    }

    /**
     * ตรวจสอบว่า Ad Unit ID ถูกต้องสำหรับ Families
     */
    private fun isValidFamiliesAdUnit(adUnitId: String?): Boolean {
        if (adUnitId == null || adUnitId.isEmpty()) {
            return false
        }

        // Ad Unit ID ต้องเริ่มต้นด้วย ca-app-pub-
        val isValidFormat = adUnitId.startsWith("ca-app-pub-")

        // ไม่ควรเป็น test ID ใน production
        val isTestId = adUnitId.contains("test", ignoreCase = true) ||
                adUnitId.contains("sample", ignoreCase = true)

        // สำหรับโหมดทดสอบ อนุญาตให้ใช้ test ID ได้
        if (adsPref.isTestMode) {
            return true
        }

        return isValidFormat && !isTestId
    }

    /**
     * ตั้งค่า Banner Ad สำหรับแอปครอบครัว
     */
    private fun setupBannerAdForFamilies() {
        // ตรวจสอบว่า activity ยังไม่ถูกทำลาย
        if (isFinishing || isDestroyed) {
            Log.d("FamiliesBanner", "Activity is finishing, skipping banner setup")
            return
        }

        // ตรวจสอบว่าเปิดใช้งาน Banner Ad หรือไม่
        if (!adsPref.isBannerAdEnabled || !adsPref.isBannerPostDetails) {
            Log.d("FamiliesBanner", "Banner ads are disabled in settings")
            bannerContainer.visibility = View.GONE
            return
        }

        // สำหรับแอปครอบครัว: ตรวจสอบเงื่อนไขเพิ่มเติม
        if (adsPref.isFamiliesApp && !shouldShowAdsForFamilies()) {
            Log.d("FamiliesBanner", "Ads not allowed for Families app in current configuration")
            bannerContainer.visibility = View.GONE
            return
        }

        // ตรวจสอบว่า adView พบใน layout หรือไม่
//        if (adView == null) {
//            Log.e("FamiliesBanner", "AdView not found in layout")
//            return
//        }

        // ตรวจสอบว่าไม่ได้กำลังโหลดอยู่แล้ว
        if (isBannerLoading) {
            Log.d("FamiliesBanner", "Banner is already loading, skipping")
            return
        }

        // รีเซ็ตจำนวนครั้งที่ลองโหลด
        bannerLoadAttempts = 0

        Log.d("FamiliesBanner", "Setting up banner ad for Families app...")

        // แสดง container ก่อน
        runOnUiThread {
            bannerContainer.visibility = View.VISIBLE
            bannerContainer.alpha = 0.3f // ตั้งค่าโปร่งใสขณะโหลด
        }

        // โหลด Banner Ad
//        loadFamiliesBannerAd()
    }

    /**
     * โหลด Banner Ad สำหรับแอปครอบครัว
     */
//    private fun loadFamiliesBannerAd() {
//        isBannerLoading = true
//        bannerLoadAttempts++
//
//        Log.d("FamiliesBanner", "Loading banner ad (attempt $bannerLoadAttempts/$maxBannerRetries)...")
//
//        runOnUiThread {
//            try {
//                // สร้าง AdRequest สำหรับแอปครอบครัว
//                val adRequestBuilder = AdRequest.Builder()
//
//                // สำหรับแอปครอบครัว
//                if (adsPref.isFamiliesApp) {
//                    Log.d("FamiliesBanner", "Applying Families-specific ad targeting")
//
//                    // ไม่ต้องใช้ tagForChildDirectedTreatment อีกต่อไป
//                    // SDK จะจัดการเองผ่าน RequestConfiguration
//                }
//
//                // สำหรับโหมดทดสอบ
//                if (adsPref.isTestMode) {
//                    Log.d("FamiliesBanner", "Test mode enabled - using test ads")
//
//                    // เพิ่ม test device (ใช้จาก Logcat)
//                   // adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//
//                    // ใช้ test ad unit ID ถ้าจำเป็น
////                    val currentAdUnitId = adView.adUnitId
////                    if (!isValidFamiliesAdUnit(currentAdUnitId)) {
////                        Log.w("FamiliesBanner", "Using test ad unit for development")
////                        // ใช้ test ad unit ID
////                        // adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test banner ID
////                    }
//                }
//
//                val adRequest = adRequestBuilder.build()
//
//                // ตั้งค่า AdListener
//                adView.adListener = object : com.google.android.gms.ads.AdListener() {
//                    override fun onAdLoaded() {
//                        isBannerLoading = false
//
//                        Log.d("FamiliesBanner", "✅ Banner ad loaded successfully")
//                        Log.d("FamiliesBanner", "Ad size: ${adView.adSize}, Ad unit: ${adView.adUnitId}")
//
//                        runOnUiThread {
//                            bannerContainer.visibility = View.VISIBLE
//                            bannerContainer.animate()
//                                .alpha(1f)
//                                .setDuration(500)
//                                .start()
//
//                            // แสดงข้อมูลเพิ่มเติมสำหรับ debugging
//                            if (adsPref.isTestMode) {
//                                Log.d("FamiliesBanner", "=== BANNER AD INFO ===")
//                                Log.d("FamiliesBanner", "Ad Unit ID: ${adView.adUnitId}")
//                                Log.d("FamiliesBanner", "Ad Size: ${adView.adSize}")
//                                Log.d("FamiliesBanner", "Visibility: ${adView.visibility}")
//                                Log.d("FamiliesBanner", "=========================")
//                            }
//                        }
//
//                        // บันทึกการโหลดสำเร็จ
//                        recordAdEvent("banner_loaded", "success")
//                    }
//
//                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
//                        isBannerLoading = false
//
//                        val errorCode = error.code
//                        val errorMessage = error.message
//                        val domain = error.domain
//                        val cause = error.cause?.message
//
//                        val detailedError = """
//                            ❌ Banner ad failed to load
//                            Code: $errorCode
//                            Message: $errorMessage
//                            Domain: $domain
//                            Cause: ${cause ?: "None"}
//                            Ad Unit: ${adView.adUnitId}
//                        """.trimIndent()
//
//                        Log.e("FamiliesBanner", detailedError)
//
//                        // ตรวจสอบ error เฉพาะสำหรับ Families
//                        when (errorCode) {
//                            0 -> { // ERROR_CODE_INTERNAL_ERROR
//                                Log.w("FamiliesBanner", "Internal error - SDK issue")
//                            }
//                            1 -> { // ERROR_CODE_INVALID_REQUEST
//                                Log.w("FamiliesBanner", "Invalid request - check ad unit ID")
//                            }
//                            2 -> { // ERROR_CODE_NETWORK_ERROR
//                                Log.w("FamiliesBanner", "Network error - check connectivity")
//                            }
//                            3 -> { // ERROR_CODE_NO_FILL
//                                Log.w("FamiliesBanner", "No fill - no inventory for Families app")
//
//                                // สำหรับ Families app อาจมี inventory น้อยกว่า
//                                if (adsPref.isFamiliesApp) {
//                                    Log.d("FamiliesBanner", "This is normal for Families apps with limited inventory")
//                                }
//                            }
//                            8 -> { // ERROR_CODE_MEDIATION_NO_FILL
//                                Log.w("FamiliesBanner", "Mediation no fill - no ad from mediation networks")
//                            }
//                            else -> {
//                                Log.w("FamiliesBanner", "Unknown error code: $errorCode")
//                            }
//                        }
//
//                        runOnUiThread {
//                            bannerContainer.visibility = View.GONE
//
//                            // ลองโหลดใหม่ถ้ายังไม่เกินจำนวนครั้งที่กำหนด
//                            if (bannerLoadAttempts < maxBannerRetries) {
//                                val retryDelay = bannerLoadAttempts * 3000L // เพิ่ม delay ตามจำนวนครั้ง
//
//                                Log.d("FamiliesBanner", "Retrying in ${retryDelay/1000} seconds...")
//
//                                Handler(Looper.getMainLooper()).postDelayed({
//                                    if (!isFinishing && !isDestroyed) {
//                                        loadFamiliesBannerAd()
//                                    }
//                                }, retryDelay)
//                            } else {
//                                Log.w("FamiliesBanner", "Max retries reached ($maxBannerRetries), giving up")
//                            }
//                        }
//
//                        // บันทึก error
//                        recordAdEvent("banner_failed", "code_$errorCode")
//                    }
//
//                    override fun onAdClicked() {
//                        Log.d("FamiliesBanner", "👆 Banner ad clicked")
//
//                        // สำหรับแอปครอบครัว: บันทึกการโต้ตอบที่ปลอดภัย
//                        if (adsPref.isFamiliesApp) {
//                            recordFamilySafeInteraction()
//                        }
//
//                        recordAdEvent("banner_clicked", "user_interaction")
//                    }
//
//                    override fun onAdImpression() {
//                        Log.d("FamiliesBanner", "📊 Banner ad impression recorded")
//                        recordAdEvent("banner_impression", "viewed")
//                    }
//
//                    override fun onAdOpened() {
//                        Log.d("FamiliesBanner", "Banner ad opened")
//                        // ตรวจสอบว่าโฆษณาปลอดภัยสำหรับครอบครัว
//                        checkAdSafety()
//                    }
//
//                    override fun onAdClosed() {
//                        Log.d("FamiliesBanner", "Banner ad closed")
//                    }
//                }
//
//                // โหลดโฆษณา
//                adView.loadAd(adRequest)
//
//                Log.d("FamiliesBanner", "Banner ad request sent to AdMob")
//
//            } catch (e: Exception) {
//                isBannerLoading = false
//                Log.e("FamiliesBanner", "Exception while loading banner: ${e.message}")
//                bannerContainer.visibility = View.GONE
//            }
//        }
//    }

    /**
     * ตรวจสอบความปลอดภัยของโฆษณา
     */
    private fun checkAdSafety() {
        if (adsPref.isFamiliesApp) {
            Log.d("FamiliesSafety", "Verifying ad safety for Families app...")

            // ตรวจสอบว่าเป็นโฆษณาจากแหล่งที่ปลอดภัย
            // Note: การตรวจสอบเชิงลึกต้องใช้ Ad Review API

            // สำหรับตอนนี้ แค่บันทึก log
            recordAdEvent("ad_safety_check", "performed")
        }
    }

    /**
     * บันทึกการโต้ตอบที่ปลอดภัยสำหรับครอบครัว
     */
    private fun recordFamilySafeInteraction() {
        Log.d("FamiliesSafety", "📝 Recording family-safe ad interaction")

        // สามารถบันทึกลง analytics หรือ Firebase ได้ที่นี่
        // ตัวอย่าง: FirebaseAnalytics.getInstance(this).logEvent("family_safe_ad_click", null)

        recordAdEvent("family_interaction", "safe_click")
    }

    /**
     * บันทึกเหตุการณ์โฆษณา
     */
    private fun recordAdEvent(eventName: String, eventValue: String) {
        // บันทึกสำหรับ debugging
        val timestamp = System.currentTimeMillis()
        val logMessage = "[$timestamp] Ad Event: $eventName = $eventValue"

        if (adsPref.isTestMode) {
            Log.d("AdEvents", logMessage)
        }

        // สามารถส่งไปยัง analytics service ได้ที่นี่
    }

    /**
     * ตรวจสอบว่าควรแสดงโฆษณาสำหรับแอปครอบครัวหรือไม่
     */
    private fun shouldShowAdsForFamilies(): Boolean {
        // เงื่อนไขสำหรับแสดงโฆษณาในแอปครอบครัว
        val conditions = mutableListOf<Boolean>()

        // 1. ต้องเปิดใช้งานโฆษณา
        conditions.add(adsPref.isAdEnabled)

        // 2. ต้องเปิดใช้งาน Banner Ad
        conditions.add(adsPref.isBannerAdEnabled)

        // 3. ต้องเปิดใช้งาน Banner ในหน้านี้
        conditions.add(adsPref.isBannerPostDetails)

        // 4. ต้องมีโฆษณาเพียง 1 ตัวต่อหน้า
        conditions.add(adsPref.maxAdsPerPage == 1)

        // 5. Content rating ต้องเป็น G
        conditions.add(adsPref.adContentRating == "G")

        // 6. ต้องอนุญาตเฉพาะโฆษณาที่ปลอดภัยสำหรับครอบครัว
        conditions.add(adsPref.allowFamilySafeAdsOnly)

        // ตรวจสอบทั้งหมด
        val shouldShow = conditions.all { it }

        if (!shouldShow && adsPref.isTestMode) {
            Log.d("FamiliesAds", "Ads not shown because:")
            conditions.forEachIndexed { index, condition ->
                if (!condition) {
                    val reasons = listOf(
                        "Ads disabled",
                        "Banner ads disabled",
                        "Banner not enabled for this page",
                        "Max ads per page != 1",
                        "Content rating != G",
                        "Family-safe ads not enforced"
                    )
                    if (index < reasons.size) {
                        Log.d("FamiliesAds", "  - ${reasons[index]}")
                    }
                }
            }
        }

        return shouldShow
    }

    /**
     * ลองโหลด Banner Ad อีกครั้ง
     */
    private fun retryBannerAd() {
        if (bannerLoadAttempts < maxBannerRetries) {
            Log.d("FamiliesBanner", "Retrying banner ad load...")
//            loadFamiliesBannerAd()
        } else {
            Log.d("FamiliesBanner", "Max retry attempts reached")
        }
    }

    private fun initYouTubePlayer() {
        // ตั้งค่าเริ่มต้นให้ YouTube Player
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull player: YouTubePlayer) {
                youTubePlayer = player

                // ถ้าเป็นวิดีโอ YouTube และโหลดข้อมูลเสร็จแล้ว ให้เล่นอัตโนมัติ
                if (video != null && video.video_type != null &&
                    video.video_type.equals("youtube", ignoreCase = true) &&
                    video.video_id != null && video.video_id.isNotEmpty()
                ) {

                    // ซ่อน progress bar เมื่อพร้อม
                    progressBar.visibility = View.GONE

                    // เล่นวิดีโออัตโนมัติ
                    player.loadVideo(video.video_id, 0f)
                    isPlaying = true
                    updatePlayPauseButton()

                    // ซ่อน thumbnail container และแสดง YouTube player
                    lytThumbnailContainer.visibility = View.GONE
                    youTubePlayerView.visibility = View.VISIBLE

                    // เริ่มอัพเดท SeekBar
                    startSeekBarUpdates()
                }
            }

            override fun onStateChange(
                @NonNull youTubePlayer: YouTubePlayer,
                @NonNull state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)

                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> {
                        isPlaying = true
                        startSeekBarUpdates()
                    }
                    PlayerConstants.PlayerState.PAUSED -> {
                        isPlaying = false
                        stopSeekBarUpdates()
                    }
                    PlayerConstants.PlayerState.ENDED -> {
                        isPlaying = false
                        stopSeekBarUpdates()
                    }
                    else -> {}
                }

                updatePlayPauseButton()
            }

            override fun onCurrentSecond(@NonNull youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                updateCurrentTime(second)
            }

            override fun onVideoDuration(@NonNull youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                videoDuration = duration
                updateTotalDuration(duration)
            }

            override fun onError(
                @NonNull youTubePlayer: YouTubePlayer,
                @NonNull error: PlayerConstants.PlayerError
            ) {
                super.onError(youTubePlayer, error)
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ActivityVideoAutoDetail,
                    "เกิดข้อผิดพลาดในการเล่นวิดีโอ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupControls() {
        // ตั้งค่าปุ่ม Play/Pause
        btnPlayPause.setOnClickListener {
            youTubePlayer?.let { player ->
                if (isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
                isPlaying = !isPlaying
                updatePlayPauseButton()
            }
        }

        // ตั้งค่าปุ่ม Rewind 10 วินาที
        btnRew10s.setOnClickListener {
            youTubePlayer?.let { player ->
                val currentProgress = seekBarTracker.progress
                val currentSecond = (currentProgress / 1000f) * videoDuration
                val newPosition = 0f.coerceAtLeast(currentSecond - 10)
                player.seekTo(newPosition)
            }
        }

        // ตั้งค่าปุ่ม Fast Forward 10 วินาที
        btnFfwd10s.setOnClickListener {
            youTubePlayer?.let { player ->
                val currentProgress = seekBarTracker.progress
                val currentSecond = (currentProgress / 1000f) * videoDuration
                val newPosition = videoDuration.coerceAtMost(currentSecond + 10)
                player.seekTo(newPosition)
            }
        }

        // ตั้งค่าปุ่ม Fullscreen
        val btnFullscreen = findViewById<ImageView>(R.id.btn_fullscreen)
        btnFullscreen.setOnClickListener {
            // youTubePlayer?.toggleFullscreen()
        }
    }

    private fun updatePlayPauseButton() {
        btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_exo_pause else R.drawable.ic_exo_play
        )
    }

    private fun updateCurrentTime(currentSecond: Float) {
        val time = formatTime(currentSecond.toInt())
        txtCurrentTime.text = time

        // อัพเดท SeekBar
        if (videoDuration > 0) {
            val progress = ((currentSecond / videoDuration) * 1000).toInt()
            seekBarTracker.seekTo(progress.toFloat())
        }
    }

    private fun updateTotalDuration(duration: Float) {
        val time = formatTime(duration.toInt())
        txtTotalDuration.text = time
    }

    private inner class SeekBarTracker : YouTubePlayerSeekBarListener {
        private var currentProgress = 0f

        override fun seekTo(time: Float) {
            // คำนวณ progress จาก time
            if (videoDuration > 0) {
                currentProgress = (time / videoDuration) * 1000
            }
        }

        val progress: Float
            get() = currentProgress

        val currentSecond: Float
            get() = (currentProgress / 1000f) * videoDuration
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }

    private fun startSeekBarUpdates() {
        stopSeekBarUpdates() // หยุดการอัพเดทก่อน (ถ้ามี)

        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                if (youTubePlayer != null && isPlaying) {
                    // อัพเดท SeekBar ทุก 1 วินาที
                    seekBarHandler.postDelayed(this, 1000)
                }
            }
        }

        updateSeekBarRunnable?.let { seekBarHandler.post(it) }
    }

    private fun stopSeekBarUpdates() {
        updateSeekBarRunnable?.let { seekBarHandler.removeCallbacks(it) }
        updateSeekBarRunnable = null
    }

    private fun handleOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        Handler(Looper.getMainLooper()).postDelayed({ requestPostData() }, 200)
    }

    private fun requestPostData() {
        callbackCall = RestAdapter.createAPI(sharedPref.baseUrl).getWatchVideoDetail(video.vid)
        callbackCall?.enqueue(object : Callback<CallbackVideoDetail> {
            override fun onResponse(
                @NonNull call: Call<CallbackVideoDetail>,
                @NonNull response: Response<CallbackVideoDetail>
            ) {
                val responseHome = response.body()
                if (responseHome == null || responseHome.status != "ok") {
                    onFailRequest()
                    return
                }
                displayAllData(responseHome)
                swipeProgress(false)
                lytMainContent.visibility = View.VISIBLE

                // เมื่อโหลดข้อมูลเสร็จแล้ว ให้เล่นวิดีโออัตโนมัติถ้าเป็น YouTube
                if (video != null && video.video_type != null &&
                    video.video_type.equals("youtube", ignoreCase = true) &&
                    video.video_id != null && video.video_id.isNotEmpty() &&
                    youTubePlayer != null
                ) {

                    youTubePlayer?.loadVideo(video.video_id, 0f)
                    isPlaying = true
                    updatePlayPauseButton()
                    lytThumbnailContainer.visibility = View.GONE
                    youTubePlayerView.visibility = View.VISIBLE
                    startSeekBarUpdates()
                }
            }

            override fun onFailure(@NonNull call: Call<CallbackVideoDetail>, @NonNull th: Throwable) {
                if (!call.isCanceled) {
                    onFailRequest()
                }
            }
        })
    }

    private fun onFailRequest() {
        swipeProgress(false)
        lytMainContent.visibility = View.GONE
        showFailedView(true, getString(R.string.failed_text))
    }

    private fun showFailedView(show: Boolean, message: String) {
        val lytFailed = findViewById<View>(R.id.lyt_failed_home)
        findViewById<TextView>(R.id.failed_message).text = message
        lytFailed.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.failed_retry).setOnClickListener { requestAction() }
    }

    private fun swipeProgress(show: Boolean) {
        swipeRefreshLayout.isRefreshing = show
        if (!show) {
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
            lytMainContent.visibility = View.VISIBLE
        } else {
            lytMainContent.visibility = View.GONE
        }
    }

    private fun displayAllData(responseHome: CallbackVideoDetail) {
        displayData(responseHome.post)
        // displaySuggested(responseHome.suggested)
    }

    fun displayData(video: Video) {
        txtTitle.text = video.video_title
        txtDuration.text = video.video_duration

        if (AppConfig.ENABLE_VIEW_COUNT) {
            txtTotalViews.text =
                Tools.withSuffix(video.total_views) + " " + getResources().getString(R.string.views_count)
        } else {
            lytView.visibility = View.GONE
        }

        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txtDateTime.text = if (AppConfig.DISPLAY_DATE_AS_TIME_AGO)
                Tools.getTimeAgo(video.date_time)
            else
                Tools.getFormatedDateSimple(video.date_time)
        } else {
            lytDate.visibility = View.GONE
        }

        // Load thumbnail
        var thumbnailUrl = video.video_thumbnail
        if (video.video_type != null && video.video_type.equals("youtube", ignoreCase = true) && thumbnailUrl.isEmpty()) {
            thumbnailUrl = Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ
        } else {
            thumbnailUrl = sharedPref.baseUrl + "/upload/" + thumbnailUrl
        }

        Glide.with(this)
            .load(thumbnailUrl)
            .placeholder(R.drawable.ic_thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(videoThumbnail)

        // 🎯 Click to play - สำหรับวิดีโอที่ไม่ใช่ YouTube
        videoThumbnail.setOnClickListener { view ->
            if (!video.video_type.equals("youtube", ignoreCase = true)) {
                Log.d("VideoClick", "User clicked video thumbnail")
                onVideoClick(video)
                showInterstitialAd()
            }
        }

        Tools.displayPostDescription(this, webView, video.video_description)

        btnShare.setOnClickListener { Tools.shareContentDetail(this, video) }

        addToFavorite()

        Handler(Looper.getMainLooper()).postDelayed({ lytSuggested.visibility = View.VISIBLE }, 1000)
    }

    private fun showInterstitialAd() {
        if (adsPref.counter >= adsPref.interstitialAdInterval) {
            adsPref.saveCounter(1)
            //adsManager.showInterstitialAd()
        } else {
            adsPref.saveCounter(adsPref.counter + 1)
        }
    }

    private fun onVideoClick(video: Video) {
        Log.d("VideoClick", "Type: " + video.video_type + ", ID/URL: " + video.video_id + " / " + video.video_url)

        if (!Tools.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.network_required), Toast.LENGTH_SHORT).show()
            return
        }

        if (video.video_type != null && video.video_type.equals("youtube", ignoreCase = true)) {
            // ไม่ต้องทำอะไรสำหรับ YouTube เพราะเล่นอัตโนมัติแล้ว
            return
        }

        Log.d("IntentLaunch", "Launching video activity")
        loadViewed()
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (sharedPref.isDarkTheme) R.color.color_dark_toolbar else R.color.light_orange
            )
        )

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = video.category_name
        }

        txtCategory.text = if (video.category_name == null || video.category_name.isEmpty()) "-" else video.category_name
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_video_sort, menu)
        return true
    }

    fun addToFavorite() {
        // ฟังก์ชันเพิ่ม/ลบ favorite
        btnFavorite.setOnClickListener { view ->
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            // btnFavorite.setImageResource(R.drawable.ic_fav)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressWarnings("deprecation")
    private fun loadViewed() {
        MyTask().execute(sharedPref.baseUrl + "/api/get_total_views/?id=" + video.vid)
    }

    @SuppressWarnings("deprecation")
    private class MyTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            return Tools.getJSONString(params[0])
        }

        override fun onPostExecute(result: String) {
            if (result == null || result.isEmpty()) {
                Log.d("Viewed", "No data returned")
                return
            }
            try {
                val json = JSONObject(result)
                val arr = json.getJSONArray("result")
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    Log.d("Viewed", "Updated views")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (callbackCall != null && !callbackCall!!.isCanceled) callbackCall!!.cancel()
        shimmerFrameLayout.stopShimmer()

        // Destroy Banner Ad
        //adView?.destroy()

        // หยุดการอัพเดท SeekBar
        stopSeekBarUpdates()

        // ลบ YouTube Player listener
        youTubePlayerView?.release()

        Log.d("FamiliesBanner", "Activity destroyed, banner ad cleaned up")
    }

    override fun onPause() {
        super.onPause()
        // หยุดการอัพเดท SeekBar เมื่อแอปถูก pause
        stopSeekBarUpdates()

        // Pause Banner Ad
        //adView?.pause()

        Log.d("FamiliesBanner", "Activity paused, banner ad paused")
    }

    override fun onResume() {
        super.onResume()
        // เริ่มอัพเดท SeekBar ใหม่เมื่อแอป resume
        if (isPlaying) {
            startSeekBarUpdates()
        }

        // Resume Banner Ad
        //adView?.resume()

        Log.d("FamiliesBanner", "Activity resumed, banner ad resumed")

        // รีเฟรช Banner Ad ถ้าจำเป็น
//        if (shouldRefreshBanner()) {
//            refreshBannerAd()
//        }
    }

    /**
     * ตรวจสอบว่าควรรีเฟรช Banner Ad หรือไม่
     */
    private fun shouldRefreshBanner(): Boolean {
        // รีเฟรชทุก 60 วินาที (1 นาที)
        // หรือเมื่อกลับมาจากหน้าอื่น
        return true // สำหรับตอนนี้ให้รีเฟรชเมื่อ resume
    }

    /**
     * รีเฟรช Banner Ad
     */
    private fun refreshBannerAd() {
        if (adsPref.isBannerAdEnabled &&
            !isBannerLoading &&
            bannerContainer.visibility == View.VISIBLE &&
            !isFinishing && !isDestroyed) {

            Log.d("FamiliesBanner", "Refreshing banner ad...")

            // รีโหลด banner
            //loadFamiliesBannerAd()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}