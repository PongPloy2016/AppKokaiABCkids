package com.pongploydev.videowatchfeaturesupabase.utils.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError

class AdMobManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private var adLoadCallback: (() -> Unit)? = null
    private var adFailedCallback: (() -> Unit)? = null
    private var adRewardCallback: (() -> Unit)? = null
    private var adDismissedCallback: (() -> Unit)? = null

    companion object {
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3279793802315716/8324446251"
        private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // สำหรับทดสอบ
    }

    fun loadRewardedAd(
        onAdLoaded: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        adLoadCallback = onAdLoaded
        adFailedCallback = onAdFailed

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID, // ใช้ test ID ก่อนสำหรับทดสอบ
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    adLoadCallback?.invoke()
                    setupFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    adFailedCallback?.invoke()
                }
            }
        )
    }

    private fun setupFullScreenContentCallback() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // โฆษณาถูกปิด
                adDismissedCallback?.invoke()
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // โฆษณาแสดงไม่สำเร็จ
                adFailedCallback?.invoke()
                rewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                // โฆษณาแสดงแล้ว
                rewardedAd = null
            }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit = {},
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        adRewardCallback = onUserEarnedReward
        adDismissedCallback = onAdDismissed

        if (isAdLoaded()) {
            rewardedAd?.show(activity, OnUserEarnedRewardListener { rewardItem ->
                // ผู้ใช้ได้รับรางวัล
                adRewardCallback?.invoke()
            })
        } else {
            onAdFailed()
        }
    }

    fun isAdLoaded(): Boolean {
        return rewardedAd != null
    }
}