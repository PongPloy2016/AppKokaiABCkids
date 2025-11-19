package com.pongploydev.priyankvex.paintapp.ads

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false

    companion object {
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3279793802315716/6154288923"
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    fun loadInterstitialAd(
        onAdLoaded: () -> Unit = {},
        onAdFailed: @Composable () -> Unit = {}
    ) {
        if (isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    onAdLoaded()
                }


            }
        )
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onAdDismissed()
                    // โหลดโฆษณาใหม่สำหรับครั้งถัดไป
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdFailed()
                    loadInterstitialAd()
                }
            }
            ad.show(activity)
        } ?: run {
            onAdFailed()
            loadInterstitialAd()
        }
    }

    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }
}