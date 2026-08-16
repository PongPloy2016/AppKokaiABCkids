package com.pongploydev.kidswebviewfeature

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.pongploydev.kidswebviewfeature.compose.KidsWebViewScreen
import com.pongploydev.kidswebviewfeature.compose.KidsWebViewTheme

class KidsWebViewActivity : ComponentActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"

        fun createIntent(
            context: Context,
            url: String = "https://www.youngciety.com/",
            title: String = "ท่องเว็บสนุก"
        ): Intent {
            return Intent(context, KidsWebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL)
            ?: intent.getStringExtra("URL")
            ?: "https://www.youngciety.com/"
        val title = intent.getStringExtra(EXTRA_TITLE)
            ?: intent.getStringExtra("TITLE")
            ?: "ท่องเว็บสนุก"

        setContent {
            KidsWebViewTheme {
                KidsWebViewScreen(
                    url = url,
                    title = title,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
