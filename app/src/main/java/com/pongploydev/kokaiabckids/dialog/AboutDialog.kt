package com.pongploydev.kokaiabckids.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pongploydev.kokaiabckids.R

class AboutDialog(private val context: Context) {

    fun show() {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_about, null)

        builder.setView(dialogView)
        val dialog = builder.create()

        setupViews(dialogView, dialog)

        dialog.show()
    }

    private fun setupViews(dialogView: View, dialog: AlertDialog) {
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)
        val btnEmail = dialogView.findViewById<TextView>(R.id.btnSendFeedback)
        val btnRateApp = dialogView.findViewById<TextView>(R.id.btnRateApp)
        val btnShareApp = dialogView.findViewById<TextView>(R.id.btnShareApp)

        // ปุ่มปิด
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // ปุ่มส่งอีเมลติชม
        btnEmail.setOnClickListener {
            com.pongploydev.kokaiabckids.utils.FeedbackHelper.showFeedbackCategoryDialog(context)
            dialog.dismiss()
        }

        // ปุ่มให้คะแนนแอป
        btnRateApp.setOnClickListener {
            rateApp()
            dialog.dismiss()
        }

        // ปุ่มแชร์แอป
        btnShareApp.setOnClickListener {
            shareApp()
            dialog.dismiss()
        }
    }

    private fun rateApp() {
        try {
            val packageName = context.packageName
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // ถ้า Play Store ไม่ได้ติดตั้ง ให้เปิดในเบราว์เซอร์
            val packageName = context.packageName
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            }
            context.startActivity(intent)
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "แอปฝึกเขียนตัวอักษรสำหรับเด็ก")
                putExtra(Intent.EXTRA_TEXT,
                    "มาเรียนรู้การเขียนตัวอักษรกัน! 📚\n\n" +
                            "แอปพลิเคชันฝึกเขียนตัวอักษรไทยและอังกฤษสำหรับเด็ก\n" +
                            "พร้อมเสียงอ่านและภาพประกอบน่ารัก\n\n" +
                            "ดาวน์โหลดเลย: https://play.google.com/store/apps/details?id=${context.packageName}"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "แชร์แอปพลิเคชัน"))
        } catch (e: Exception) {
            showErrorDialog("ไม่สามารถแชร์แอปพลิเคชันได้")
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle("เกิดข้อผิดพลาด")
            .setMessage(message)
            .setPositiveButton("ตกลง", null)
            .show()
    }
}