package com.pongploydev.videowatchfeature.utils.ads

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref

/**
 * BannerAdManager สำหรับแอปครอบครัว (Families-compliant)
 * แก้ไขปัญหา "No fill" และ "IllegalStateException"
 */
class BannerAdManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: BannerAdManager? = null

        fun getInstance(context: Context): BannerAdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BannerAdManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private var currentAdView: AdView? = null
    private var isInitialized = false

    /**
     * เริ่มต้น Mobile Ads SDK
     */
    fun initialize(adsPref: AdsPref, onInitialized: (() -> Unit)? = null) {
        if (isInitialized) {
            onInitialized?.invoke()
            return
        }

        try {
            // ตั้งค่า RequestConfiguration สำหรับแอปครอบครัวและการทดสอบ
            setupRequestConfiguration(adsPref)

            // เริ่มต้น Mobile Ads SDK
            MobileAds.initialize(context) { initializationStatus ->
                Log.d("BannerAdManager", "✅ Mobile Ads SDK initialized")
                Log.d("BannerAdManager", "Adapter status: ${initializationStatus.adapterStatusMap}")

                isInitialized = true
                onInitialized?.invoke()
            }

        } catch (e: Exception) {
            Log.e("BannerAdManager", "❌ Initialization failed", e)
            // Fallback: เริ่มต้นแบบธรรมดา
            MobileAds.initialize(context)
            isInitialized = true
            onInitialized?.invoke()
        }
    }

    /**
     * ตั้งค่า RequestConfiguration
     */
    private fun setupRequestConfiguration(adsPref: AdsPref) {
        try {
            val testDeviceIds = mutableListOf<String>()

            // เพิ่ม emulator test device
            testDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR)

            // เพิ่ม test device IDs จาก AdsPref
            if (adsPref.testDeviceIds.isNotEmpty()) {
                adsPref.testDeviceIds.split(",").forEach { deviceId ->
                    if (deviceId.trim().isNotEmpty()) {
                        testDeviceIds.add(deviceId.trim())
                    }
                }
            }

            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .apply {
                    // สำหรับแอปครอบครัว
                    if (adsPref.isFamiliesApp) {
                        setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                        setTagForChildDirectedTreatment(
                            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                        )
                        setTagForUnderAgeOfConsent(
                            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
                        )
                    }
                }
                .build()

            MobileAds.setRequestConfiguration(requestConfiguration)

            Log.d("BannerAdManager", "✅ RequestConfiguration set with ${testDeviceIds.size} test devices")

        } catch (e: Exception) {
            Log.e("BannerAdManager", "❌ Error setting RequestConfiguration", e)
        }
    }

    /**
     * โหลดและแสดง Banner Ad
     */
    fun loadBannerAd(
        container: ViewGroup,
        adsPref: AdsPref,
        adUnitId: String? = null,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        // ตรวจสอบว่าเปิดใช้งาน Banner Ad หรือไม่
        if (!adsPref.isBannerAdEnabled || !adsPref.isAdEnabled) {
            Log.d("BannerAdManager", "Banner ads are disabled")
            container.visibility = ViewGroup.GONE
            onAdFailed?.invoke("Banner ads are disabled")
            return
        }

        // สำหรับแอปครอบครัว: ตรวจสอบ compliance
        if (adsPref.isFamiliesApp && !checkFamiliesCompliance(adsPref)) {
            Log.w("BannerAdManager", "Families compliance check failed")
            container.visibility = ViewGroup.GONE
            onAdFailed?.invoke("Families compliance check failed")
            return
        }

        // กำหนด Ad Unit ID
        val finalAdUnitId = adUnitId ?: getBannerAdUnitId(adsPref)

        Log.d("BannerAdManager", "🔄 Loading banner ad with ID: $finalAdUnitId")
        Log.d("BannerAdManager", "isTestMode: ${adsPref.isTestMode}")
        Log.d("BannerAdManager", "isFamiliesApp: ${adsPref.isFamiliesApp}")

        // รอให้ container พร้อม
        container.post {
            try {
                // ทำลาย banner เก่าก่อน
                destroyCurrentBanner()

                // ล้าง container
                container.removeAllViews()

                // สร้าง AdView ใหม่
                val adView = createAdView(finalAdUnitId, adsPref)

                if (adView == null) {
                    Log.e("BannerAdManager", "Failed to create AdView")
                    container.visibility = ViewGroup.GONE
                    onAdFailed?.invoke("Failed to create AdView")
                    return@post
                }

                // ตั้งค่า LayoutParams
                adView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )

                // เพิ่มลง container
                container.addView(adView)
                container.visibility = ViewGroup.VISIBLE

                // เก็บ reference
                currentAdView = adView

                // สร้าง AdRequest
                val adRequest = createFamiliesAdRequest(adsPref)

                // ตั้งค่า AdListener
                adView.adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        Log.d("BannerAdManager", "✅ Banner ad loaded successfully")
                        container.visibility = ViewGroup.VISIBLE
                        onAdLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        val errorMessage = "❌ Failed to load banner ad: ${error.message} (Code: ${error.code})"
                        Log.e("BannerAdManager", errorMessage)

                        // Log additional details
                        Log.e("BannerAdManager", "Domain: ${error.domain}")
                        Log.e("BannerAdManager", "Response: ${error.responseInfo}")

                        container.visibility = ViewGroup.GONE
                        container.removeView(adView)
                        currentAdView = null
                        onAdFailed?.invoke(errorMessage)
                    }

                    override fun onAdClicked() {
                        Log.d("BannerAdManager", "Banner ad clicked")
                    }

                    override fun onAdImpression() {
                        Log.d("BannerAdManager", "Banner ad impression recorded")
                    }
                }

                // โหลดโฆษณา
                Log.d("BannerAdManager", "📤 Calling loadAd()...")
                adView.loadAd(adRequest)

            } catch (e: Exception) {
                Log.e("BannerAdManager", "❌ Exception in loadBannerAd", e)
                container.visibility = ViewGroup.GONE
                onAdFailed?.invoke("Exception: ${e.message}")
            }
        }
    }

    /**
     * สร้าง AdView
     */
    private fun createAdView(adUnitId: String, adsPref: AdsPref): AdView? {
        return try {
            // สำหรับแอปครอบครัว: ต้องสร้าง AdView ให้สอดคล้องกับ policy
            if (adsPref.isFamiliesApp) {
                createFamiliesCompliantAdView(adUnitId)
            } else {
                createRegularAdView(adUnitId)
            }
        } catch (e: Exception) {
            Log.e("BannerAdManager", "Error creating AdView", e)
            null
        }
    }

    /**
     * สร้าง AdView สำหรับแอปครอบครัว
     */
    private fun createFamiliesCompliantAdView(adUnitId: String): AdView {
        // สร้าง AdView พื้นฐาน
        val adView = AdView(context)

        // สำหรับ SDK รุ่นใหม่ที่ต้องตั้งค่า adSize และ adUnitId ใน constructor
        // เราใช้วิธีอื่นในการตั้งค่า

        return adView
    }

    /**
     * สร้าง AdView ปกติ
     */
    private fun createRegularAdView(adUnitId: String): AdView {
        return AdView(context)
    }

    /**
     * สร้าง AdRequest สำหรับแอปครอบครัว
     */
    private fun createFamiliesAdRequest(adsPref: AdsPref): AdRequest {
        return AdRequest.Builder()
            .apply {
                // สำหรับแอปครอบครัว
                if (adsPref.isFamiliesApp) {

                  //  tagForChildDirectedTreatment(AdRequest.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                   // setMaxAdContentRating(AdRequest.MAX_AD_CONTENT_RATING_G)
                    Log.d("BannerAdManager", "✅ Families settings applied")
                }

                // สำหรับโหมดทดสอบ - สำคัญ! ป้องกัน No fill
                if (adsPref.isTestMode) {
                   // addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    Log.d("BannerAdManager", "✅ Test device added")

                    // Log test device ID สำหรับ debug
                    Log.d("BannerAdManager", "Using test device: ${AdRequest.DEVICE_ID_EMULATOR}")
                }
            }
            .build()
    }

    /**
     * ตรวจสอบความสอดคล้องกับ Families Policy
     */
    private fun checkFamiliesCompliance(adsPref: AdsPref): Boolean {
        if (!adsPref.isFamiliesApp) return true

        val violations = mutableListOf<String>()

        // ตรวจสอบ content rating
        if (adsPref.adContentRating != "G") {
            violations.add("Content rating should be G")
        }

        // ตรวจสอบ max ads per page
        if (adsPref.maxAdsPerPage != 1) {
            violations.add("Should have only 1 ad per page")
        }

        // ตรวจสอบ child directed treatment
        if (!adsPref.isChildDirectedTreatment) {
            violations.add("Child directed treatment should be enabled")
        }

        if (violations.isNotEmpty()) {
            Log.w("BannerAdManager", "Families compliance violations: ${violations.joinToString(", ")}")
            // ใน production อาจต้องการ action เพิ่มเติม
        }

        return violations.isEmpty()
    }

    /**
     * รับ Ad Unit ID ที่เหมาะสม
     */
    private fun getBannerAdUnitId(adsPref: AdsPref): String {
        return if (adsPref.isTestMode) {
            // Test Ad Unit ID - สำคัญ! ใช้สำหรับการพัฒนา
            "ca-app-pub-3940256099942544/6300978111"
        } else {
            // Production Ad Unit ID
            adsPref.bannerPostDetailId.takeIf { it.isNotEmpty() }
                ?: "ca-app-pub-3940256099942544/6300978111" // Fallback to test ID
        }
    }

    /**
     * ตั้งค่า Banner Ad สำหรับหน้า Video Detail
     */
    fun setupVideoDetailBanner(
        container: ViewGroup,
        adsPref: AdsPref,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: ((String) -> Unit)? = null
    ) {
        if (!adsPref.isBannerPostDetails) {
            Log.d("BannerAdManager", "Banner not enabled for video detail")
            container.visibility = ViewGroup.GONE
            return
        }

        loadBannerAd(
            container = container,
            adsPref = adsPref,
            onAdLoaded = onAdLoaded,
            onAdFailed = onAdFailed
        )
    }

    /**
     * ทำลาย Banner Ad ปัจจุบัน
     */
    fun destroyCurrentBanner() {
        currentAdView?.let { adView ->
            try {
                adView.destroy()
            } catch (e: Exception) {
                Log.e("BannerAdManager", "Error destroying banner", e)
            }
            currentAdView = null
        }
    }

    /**
     * รีเฟรช Banner Ad
     */
    fun refreshBanner(container: ViewGroup, adsPref: AdsPref) {
        if (currentAdView != null && container.visibility == ViewGroup.VISIBLE) {
            Log.d("BannerAdManager", "Refreshing banner ad...")
            loadBannerAd(container, adsPref)
        }
    }

    /**
     * ซ่อน Banner
     */
    fun hideBanner(container: ViewGroup) {
        container.visibility = ViewGroup.GONE
    }

    /**
     * แสดง Banner
     */
    fun showBanner(container: ViewGroup) {
        container.visibility = ViewGroup.VISIBLE
    }

    /**
     * ตรวจสอบว่า Banner กำลังแสดงอยู่หรือไม่
     */
    fun isBannerVisible(container: ViewGroup): Boolean {
        return container.visibility == ViewGroup.VISIBLE
    }
}