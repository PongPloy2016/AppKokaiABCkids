package com.pongploydev.priyankvex.paintapp.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import android.util.Log // เพิ่มสำหรับ logging

class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false

    companion object {
        // ID โฆษณาจริงของคุณ (Real Ad Unit ID)
        //ca-app-pub-3279793802315716/6154288923
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3279793802315716/6154288923"
        // ID โฆษณาทดสอบของ Google (Test Ad Unit ID)
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        private const val TAG = "InterstitialAdManager"
    }

    /**
     * โหลดโฆษณาคั่นระหว่างหน้า
     * ใช้ Ad Unit ID จริงเมื่อแอปอยู่ในโหมด Release และใช้ Test ID เมื่ออยู่ในโหมด Debug
     */
    fun loadInterstitialAd(
        onAdLoaded: () -> Unit = {},
        onAdFailed: (LoadAdError) -> Unit = {} // ปรับปรุงให้รับ LoadAdError สำหรับ Debug
    ) {
        if (isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        // *** การแก้ไขที่สำคัญ: เลือกใช้ Ad Unit ID ตามโหมดการ Build ***
        val adUnitId = if (false) {
            Log.d(TAG, "Loading Test Ad.")
            TEST_INTERSTITIAL_AD_UNIT_ID
        } else {
            Log.d(TAG, "Loading Real Ad.")
            INTERSTITIAL_AD_UNIT_ID
        }
        // *******************************************************************

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.i(TAG, "Ad loaded successfully.")
                    onAdLoaded()
                }

                // *** การแก้ไขที่สำคัญ: ต้อง Override เมธอดนี้เพื่อจัดการความล้มเหลว ***
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.e(TAG, "Ad failed to load: ${adError.message}")
                    onAdFailed(adError)
                }
            }
        )
    }

    /**
     * แสดงโฆษณาคั่นระหว่างหน้าหากโหลดเสร็จแล้ว
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i(TAG, "Ad dismissed.")
                    // ล้างโฆษณาปัจจุบันและโหลดใหม่สำหรับการเรียกครั้งถัดไป
                    interstitialAd = null
                    onAdDismissed()
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    // ล้างโฆษณาปัจจุบันและโหลดใหม่
                    interstitialAd = null
                    onAdFailed()
                    loadInterstitialAd()
                }
            }
            ad.show(activity)
        } ?: run {
            Log.w(TAG, "Ad not ready to show.")
            onAdFailed()
            loadInterstitialAd() // พยายามโหลดอีกครั้งในกรณีที่โฆษณาเป็น null
        }
    }

    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }
}