package com.pongploydev.newsfeature.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object NewsColors {
    val Background = Color(0xFF0A1020)
    val Surface = Color(0xFF111A2E)
    val SurfaceHigh = Color(0xFF18243F)
    val Outline = Color(0xFF263552)
    val Accent = Color(0xFF14B8A6)
    val TextPrimary = Color(0xFFEAF0FA)
    val TextMuted = Color(0xFF8EA0BD)
    val Error = Color(0xFFF87171)

    private val badgePalette = listOf(
        Color(0xFF16A34A), Color(0xFFD946EF), Color(0xFFEF4444), Color(0xFF2563EB),
        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF0891B2), Color(0xFFE11D48)
    )

    /** สี badge ของหมวดข่าว คงที่ตามชื่อหมวด */
    fun badgeColor(category: String): Color =
        badgePalette[(category.hashCode() and Int.MAX_VALUE) % badgePalette.size]
}

@Composable
fun NewsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NewsColors.Accent,
            background = NewsColors.Background,
            surface = NewsColors.Surface,
            onBackground = NewsColors.TextPrimary,
            onSurface = NewsColors.TextPrimary
        ),
        content = content
    )
}

private val bangkok: TimeZone = TimeZone.getTimeZone("Asia/Bangkok")

private fun thaiFormat(pattern: String) =
    SimpleDateFormat(pattern, Locale("th", "TH")).apply { timeZone = bangkok }

/** เวลาแบบสัมพัทธ์ เช่น "5 ชั่วโมงที่แล้ว" */
fun formatRelativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    if (millis <= 0L) return ""
    val minutes = (now - millis) / 60_000L
    return when {
        minutes < 1 -> "เมื่อสักครู่"
        minutes < 60 -> "$minutes นาทีที่แล้ว"
        minutes < 60 * 24 -> "${minutes / 60} ชั่วโมงที่แล้ว"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)} วันที่แล้ว"
        else -> thaiFormat("d MMM yyyy").format(millis)
    }
}

/** วันเวลาเต็ม เช่น "5 พฤศจิกายน 2569 19:56 น." */
fun formatFullDate(millis: Long): String {
    if (millis <= 0L) return ""
    return thaiFormat("d MMMM yyyy HH:mm").format(millis) + " น."
}

