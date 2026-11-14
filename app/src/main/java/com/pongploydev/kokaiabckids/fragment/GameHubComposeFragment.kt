package com.pongploydev.kokaiabckids.fragment

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.activity.GameHubApp
import java.util.Locale

class GameHubComposeFragment : Fragment(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tts = TextToSpeech(requireContext(), this)

        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides OverscrollConfiguration(
                        drawPadding = PaddingValues(0.dp)
                    )
                ) {
                    GameHubApp(
                        onBack = {
                            val vp = activity?.findViewById<ViewPager>(R.id.viewpager)
                            val nav = activity?.findViewById<BottomNavigationView>(R.id.navigation)
                            if (vp != null && nav != null && vp.currentItem == 4) {
                                vp.currentItem = 0
                                nav.selectedItemId = R.id.navigation_home
                            }
                        },
                        onSpeak = { text ->
                            if (isTtsReady) {
                                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GameHubTTS")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("th"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    override fun onDestroyView() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroyView()
    }
}
