package com.pongploydev.kokaiabckids.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object FeedbackHelper {

    private const val SUPPORT_EMAIL = "popodev@outlook.com"

    fun sendFeedbackEmail(context: Context, category: String = "ทั่วไป") {
        // ดึงข้อมูลเวอร์ชันแอป
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: Exception) {
            null
        }

        val appVersionName = packageInfo?.versionName ?: "Unknown"
        val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: 0
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode ?: 0
        }

        // จัดรูปแบบ Body
        val subject = "[$category] ข้อเสนอแนะการใช้งานแอปพลิเคชัน"
        val body = buildString {
            append("กรุณาระบุรายละเอียดข้อเสนอแนะ / ปัญหาที่พบด้านล่าง:\n")
            append("------------------------------------\n\n\n")
            append("------------------------------------\n")
            append("[System Information]\n")
            append("- App Version: $appVersionName ($appVersionCode)\n")
            append("- Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("- Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
        }

        // สร้าง Intent สำหรับ mailto:
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            // ใช้ Intent.createChooser เพื่อให้ผู้ใช้เลือกแอป Mail ได้สะดวก
            val chooserIntent = Intent.createChooser(emailIntent, "เลือกแอปสำหรับส่งอีเมล")
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            // กรณีไม่มีแอปอีเมลในเครื่อง
            showFallbackDialog(context)
        }
    }

    fun showFeedbackCategoryDialog(context: Context) {
        val categories = arrayOf(
            "แจ้งปัญหาการใช้งาน (Bug Report)",
            "เสนอแนะฟีเจอร์ใหม่ (Feature Request)",
            "ข้อเสนอแนะทั่วไป (General)"
        )
        val categoryKeys = arrayOf("Bug Report", "Feature Request", "General")

        AlertDialog.Builder(context)
            .setTitle("เลือกประเภทข้อเสนอแนะ")
            .setItems(categories) { _, which ->
                sendFeedbackEmail(context, category = categoryKeys[which])
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    fun showFallbackDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("ไม่สามารถเปิดแอปอีเมลได้")
            .setMessage("ไม่พบแอปอีเมลในอุปกรณ์ของคุณ สามารถส่งข้อความมาที่:\n\n$SUPPORT_EMAIL")
            .setPositiveButton("คัดลอกอีเมล") { dialog, _ ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Support Email", SUPPORT_EMAIL)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "คัดลอกอีเมลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("ปิด") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
