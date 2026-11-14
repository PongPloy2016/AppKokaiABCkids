package com.pongploydev.kokaiabckids.log

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Helper class for logging Firebase Analytics events with screen name,
 * device information (Model, Manufacturer, Android Version), and user clicks.
 */
object AnalyticsLogger {

    private const val TAG = "AnalyticsLogger"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    private fun getInstance(context: Context): FirebaseAnalytics? {
        if (firebaseAnalytics == null) {
            try {
                firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
                firebaseAnalytics?.setAnalyticsCollectionEnabled(true)

                // บันทึก User Properties สำหรับเครื่องโทรศัพท์ยี่ห้อ/รุ่น เพื่อให้ดูได้ง่ายในทุกๆ รายงานของ Firebase Console
                firebaseAnalytics?.setUserProperty("device_model", Build.MODEL)
                firebaseAnalytics?.setUserProperty("device_manufacturer", Build.MANUFACTURER)
                firebaseAnalytics?.setUserProperty("android_version", Build.VERSION.RELEASE)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing FirebaseAnalytics", e)
            }
        }
        return firebaseAnalytics
    }

    /**
     * บันทึกเมื่อ User เปิดเข้าหน้าจอต่าง ๆ (Screen View Track)
     * @param context Context ของหน้าจอ
     * @param screenName ชื่อหน้าจอภาษาไทยหรืออังกฤษ (เช่น "หน้าแรก", "หน้าวิดีโอ", "หน้าฝึกเขียน ก ไก่")
     * @param screenClass ชื่อ Class ของ Activity/Fragment (เช่น "MainActivity", "VideoListFragment")
     */
    @JvmStatic
    fun logScreenView(context: Context?, screenName: String, screenClass: String? = null) {
        if (context == null) return
        val analytics = getInstance(context) ?: return

        try {
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
                putString("page_title", screenName)
                putString("screen_title", screenName)
                putString("device_model", Build.MODEL)
                putString("device_manufacturer", Build.MANUFACTURER)
                putString("android_version", Build.VERSION.RELEASE)
                putLong("sdk_int", Build.VERSION.SDK_INT.toLong())
            }

            // 1. ส่ง Standard Firebase Screen View Event
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)

            // 2. ส่ง Custom Event "page_view_custom" เพื่อให้ค้นหาใน Dashboard / Custom Reports ได้ง่ายขึ้น
            analytics.logEvent("page_view_custom", params)

            Log.d(TAG, "Logged ScreenView: screenName=$screenName, device=${Build.MANUFACTURER} ${Build.MODEL}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log screen view", e)
        }
    }

    /**
     * บันทึกเมื่อ User คลิกปุ่มหรือเลือกเมนูต่าง ๆ (User Click Track)
     * @param context Context ของหน้าจอ
     * @param itemName ชื่อปุ่มหรือเมนูที่กด (เช่น "ปุ่ม_ฝึกเขียน_กไก่", "เลือกวิดีโอ_เพลงกไก่")
     * @param screenName ชื่อหน้าจอที่กด (เช่น "หน้าแรก", "หน้าวิดีโอ")
     */
    @JvmStatic
    fun logClickEvent(context: Context?, itemName: String, screenName: String) {
        if (context == null) return
        val analytics = getInstance(context) ?: return

        try {
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, itemName)
                putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString("page_title", screenName)
                putString("device_model", Build.MODEL)
                putString("device_manufacturer", Build.MANUFACTURER)
                putString("android_version", Build.VERSION.RELEASE)
            }

            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params)
            analytics.logEvent("user_click_navigation", params)

            Log.d(TAG, "Logged ClickEvent: itemName=$itemName, screenName=$screenName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log click event", e)
        }
    }

    /**
     * บันทึก Key Event พิเศษสำหรับการติดตามเฉพาะกิจกรรมหลักในแอป
     */
    @JvmStatic
    fun logKeyEvent(context: Context?, eventName: String, screenName: String, extraInfo: String? = null) {
        if (context == null) return
        val analytics = getInstance(context) ?: return

        try {
            val params = Bundle().apply {
                putString("key_event_name", eventName)
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString("page_title", screenName)
                putString("device_model", Build.MODEL)
                putString("device_manufacturer", Build.MANUFACTURER)
                putString("android_version", Build.VERSION.RELEASE)
                extraInfo?.let { putString("extra_info", it) }
            }

            analytics.logEvent(eventName, params)
            analytics.logEvent("key_navigation_event", params)

            Log.d(TAG, "Logged KeyEvent: eventName=$eventName, screenName=$screenName, device=${Build.MANUFACTURER} ${Build.MODEL}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log key event", e)
        }
    }

    /**
     * เมธอดเดิมสำหรับรองรับโค้ดเก่า
     */
    fun logDeviceSpecificEvent(activityName: String) {
        // No-op or call logScreenView
    }
}