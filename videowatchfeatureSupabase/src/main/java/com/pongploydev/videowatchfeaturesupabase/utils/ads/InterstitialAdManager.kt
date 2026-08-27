package com.pongploydev.videowatchfeaturesupabase.utils.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import android.util.Log // เพิ่มสำหรับ logging
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration


class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false

    companion object {
        // ID โฆษณาสำหรับแอปเด็กโดยเฉพาะ
        private const val CHILD_FRIENDLY_AD_UNIT_ID = "ca-app-pub-9592898299992336/7978304153" // ใช้ ID จริงของคุณ
        private const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID จาก Google

        private const val TAG = "InterstitialAdManager"

        // ตั้งค่าสำหรับโหมดทดสอบ
        private var USE_TEST_ADS = false
    }

    init {
        setupChildDirectedTreatment()
    }

    /**
     * ตั้งค่า Mobile Ads SDK สำหรับเด็ก
     */
    private fun setupChildDirectedTreatment() {
        // ตั้งค่าให้เป็น child-directed
        val requestConfiguration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G) // ระดับ G เท่านั้น
            .build()

        MobileAds.setRequestConfiguration(requestConfiguration)

        // ตั้งค่าสำหรับการทดสอบ (ในโหมด Debug)
//        if (USE_TEST_ADS) {
//            val testDeviceIds = listOf("YOUR_TEST_DEVICE_ID") // ใส่ Device ID ของคุณ
//            MobileAds.setRequestConfiguration(
//                requestConfiguration.toBuilder()
//                    .setTestDeviceIds(testDeviceIds)
//                    .build()
//            )
//        }
    }

    /**
     * โหลดโฆษณาคั่นระหว่างหน้าที่เหมาะสมสำหรับเด็ก
     */
    fun loadInterstitialAd(
        onAdLoaded: () -> Unit = {},
        onAdFailed: (LoadAdError) -> Unit = {}
    ) {
        if (isAdLoading) return

        isAdLoading = true

        // สร้าง AdRequest ที่เหมาะสมสำหรับเด็ก
        val adRequestBuilder = AdRequest.Builder()
            //.tagForChildDirectedTreatment(true) // ระบุว่าเป็นแอปสำหรับเด็ก

        val adRequest = adRequestBuilder.build()

        // ใช้ Ad Unit ID ที่ตั้งค่าไว้
        val adUnitId = if (USE_TEST_ADS) {
            Log.d(TAG, "Loading Test Ad.")
            TEST_AD_UNIT_ID
        } else {
            Log.d(TAG, "Loading Real Child-Friendly Ad: $CHILD_FRIENDLY_AD_UNIT_ID")
            CHILD_FRIENDLY_AD_UNIT_ID
        }

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.i(TAG, "Ad loaded successfully: $adUnitId")

                    ad.setOnPaidEventListener { adValue ->
                        logAdValue(adValue)
                    }

                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.e(TAG, "❌ Real Ad failed to load: ${adError.message} (Code: ${adError.code})")
                    onAdFailed(adError)
                }
            }
        )
    }

    /**
     * แสดงโฆษณา (ต้องเป็นโฆษณาที่เหมาะสมสำหรับเด็ก)
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i(TAG, "Child-friendly ad dismissed.")
                    interstitialAd = null
                    onAdDismissed()

                    // โหลดโฆษณาใหม่ล่วงหน้าสำหรับครั้งต่อไป
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Child-friendly ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onAdFailed()
                    loadInterstitialAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.i(TAG, "Child-friendly ad showed successfully.")
                }
            }
            ad.show(activity)
        } ?: run {
            Log.w(TAG, "Child-friendly ad not ready to show.")
            onAdFailed()
            loadInterstitialAd()
        }
    }

    /**
     * ตรวจสอบว่ามีโฆษณาพร้อมแสดงหรือไม่
     */
    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }

    /**
     * บันทึกข้อมูล Ad Value สำหรับการวิเคราะห์
     */
    private fun logAdValue(adValue: AdValue) {
        Log.d(TAG, "Ad Value: ${adValue.valueMicros} micros, Currency: ${adValue.currencyCode}")
    }

    /**
     * ล้างโฆษณาปัจจุบัน
     */
    fun clearAd() {
        interstitialAd = null
        isAdLoading = false
    }
}