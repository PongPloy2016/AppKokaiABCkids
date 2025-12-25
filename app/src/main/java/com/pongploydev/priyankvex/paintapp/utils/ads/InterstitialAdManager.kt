package com.pongploydev.priyankvex.paintapp.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import android.util.Log // เพิ่มสำหรับ logging
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.pongploydev.priyankvex.paintapp.BuildConfig


class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false

    companion object {
        // ID โฆษณาสำหรับแอปเด็กโดยเฉพาะ
        // คุณต้องสร้าง Ad Unit ใหม่ใน AdMob ที่ตั้งค่าเป็น "Child-directed treatment"
        private const val CHILD_FRIENDLY_AD_UNIT_ID = "ca-app-pub-3279793802315716/8581063137" // ใช้ ID จริงของคุณ

        private const val TAG = "InterstitialAdManager"

        // ตั้งค่าสำหรับโหมดทดสอบ
        private  var USE_TEST_ADS = BuildConfig.DEBUG // ใช้ BuildConfig.DEBUG แทนการกำหนดเอง
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

        // ใช้ Ad Unit ID ที่ตั้งค่าไว้สำหรับเด็ก
        val adUnitId = if (USE_TEST_ADS) {
            // ใช้ Test Ad Unit ID ที่ Google จัดเตรียมสำหรับการทดสอบ
            Log.d(TAG, "Loading Test Ad for children.")
            CHILD_FRIENDLY_AD_UNIT_ID
        } else {
            Log.d(TAG, "Loading Real Child-Friendly Ad.")
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
                    Log.i(TAG, "Child-friendly ad loaded successfully.")

                    // ตั้งค่า Additional Content Settings
                    ad.setOnPaidEventListener { adValue ->
                        // จัดการข้อมูลค่าตอบแทน (ถ้ามี)
                        logAdValue(adValue)
                    }

                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.e(TAG, "Child-friendly ad failed to load: ${adError.message}")
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
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i(TAG, "Child-friendly ad dismissed.")
                    interstitialAd = null
                    onAdDismissed()

                    // โหลดโฆษณาใหม่ล่วงหน้าสำหรับครั้งต่อไป
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
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
    private fun logAdValue(adValue: com.google.android.gms.ads.AdValue) {
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