package com.pongploydev.priyankvex.paintapp.log

import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Helper class for logging custom Firebase Analytics events,
 * especially those involving device-specific parameters.
 */
class AnalyticsLogger(private val firebaseAnalytics: FirebaseAnalytics) {

    /**
     * Logs a custom event containing device and application status information.
     * * @param activityName The name of the Activity/Screen where the event is logged.
     */
    fun logDeviceSpecificEvent(activityName: String) {

        // **ชื่อ Event ที่ชัดเจน (ใช้ตัวอักษรเล็กและขีดล่าง)**
        val eventName = "coloring_session_start"

        // 1. สร้าง Bundle สำหรับ Parameters
        val params = Bundle().apply {

            // 1.1 ข้อมูลอุปกรณ์พื้นฐาน
            putString("device_model", Build.MODEL)
            putString("device_manufacturer", Build.MANUFACTURER)
            putString("android_version", Build.VERSION.RELEASE)

            // ใช้ toLong() สำหรับ Parameters ที่เป็นตัวเลขเพื่อความเข้ากันได้
            putLong("sdk_int", Build.VERSION.SDK_INT.toLong())

            // 1.2 ข้อมูลสถานะแอปพลิเคชัน/กิจกรรม
            putString("current_activity", activityName)

            // NOTE: ถ้าต้องการใช้ BuildConfig.VERSION_NAME,
            // ต้องเพิ่มโค้ดเพื่อเข้าถึง Context หรือส่งค่าเข้ามาใน Constructor/Method
            // เช่น: putString("app_version", getAppVersion(context))
        }

        // 2. ส่ง Custom Event โดยใช้ logEvent (จาก Bundle)
        // นี่คือวิธีที่ถูกต้องในการส่ง Bundle ที่สร้างไว้ล่วงหน้า
        firebaseAnalytics.logEvent(eventName, params)

        // ❌ ไม่จำเป็นต้อง Log ซ้ำซ้อนด้วย KTX block
        // ❌ Toast ถูกลบออกเนื่องจากไม่มี Context ใน Class นี้
    }

    // คุณสามารถเพิ่มเมธอด Log อื่นๆ ได้ที่นี่
//    fun logLevelComplete(levelId: Int) {
//        firebaseAnalytics.logEvent("level_completed") {
//            param("level_id", levelId.toLong())
//        }
//    }
}