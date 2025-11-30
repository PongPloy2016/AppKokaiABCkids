package com.pongploydev.priyankvex.paintapp.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.pongploydev.priyankvex.paintapp.compose.KidsWebViewScreen
import com.pongploydev.priyankvex.paintapp.compose.VideoWatchFeatureTheme

class KidsWebViewActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra("URL") ?: "https://www.youngciety.com/"
        val title = intent.getStringExtra("TITLE") ?: "วิดีโอสำหรับเด็ก"

        setContent {
            VideoWatchFeatureTheme {
                KidsWebViewScreen(
                    url = url,
                    title = title,
                    onBackClick = { finish() }
                )
            }
        }
    }
}