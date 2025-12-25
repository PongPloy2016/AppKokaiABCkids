package com.pongploydev.videowatchfeature.utils.ads

import android.content.Context
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

// ในคลาส AdsManager หรือคลาสจัดการโฆษณาของคุณ
class FamiliesAdManager(private val context: Context) {

    fun setupFamiliesAds() {
        // 1. ใช้เทสต์โฆษณาสำหรับ Family & Children
        val testDeviceIds = listOf("TEST_DEVICE_ID_HERE")

        // 2. ตั้งค่า RequestConfiguration
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
//            .setTagForChildDirectedTreatment(TagForChildDirectedTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
//            .setMaxAdContentRating(RequestConfiguration.MaxAdContentRating.G)
            .setTestDeviceIds(testDeviceIds)
            .build()

        MobileAds.setRequestConfiguration(requestConfiguration)

        // 3. ตั้งค่า AdView สำหรับครอบครัว
        val adView = AdView(context)
//        adView.adSize = AdSize.BANNER
//        adView.adUnitId = "YOUR_AD_UNIT_ID"
//
//        // 4. สร้าง AdRequest พิเศษสำหรับครอบครัว
//        val adRequest = AdRequest.Builder()
//            .tagForChildDirectedTreatment(TagForChildDirectedTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
//            .setMaxAdContentRating(MaxAdContentRating.G)
//            .build()
    }

    // ตรวจสอบว่าเป็นโฆษณาที่ผ่านการรับรองสำหรับครอบครัวหรือไม่
    fun isFamiliesCertifiedSdk(): Boolean {
        // ตรวจสอบ SDK ที่ใช้กับรายการที่ Google รับรอง
        // https://support.google.com/googleplay/android-developer/answer/9283445
        return true // เปลี่ยนตาม SDK ที่คุณใช้
    }
}