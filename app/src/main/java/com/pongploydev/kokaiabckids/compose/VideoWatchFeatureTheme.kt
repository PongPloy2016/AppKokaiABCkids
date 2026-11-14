package com.pongploydev.kokaiabckids.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color

@Composable
fun VideoWatchFeatureTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFFF6B8E),
            secondary = Color(0xFF4ECDC4),
            background = Color(0xFFF8F9FA)
        ),
        content = content
    )
}