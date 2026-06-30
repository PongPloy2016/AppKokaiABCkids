package com.pongploydev.priyankvex.paintapp.fragment

import android.os.Bundle
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
import com.pongploydev.priyankvex.paintapp.compose.QuizComposeScreen

class QuizComposeFragment : Fragment() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides OverscrollConfiguration(
                        drawPadding = PaddingValues(0.dp)
                    )
                ) {
                    QuizComposeScreen()
                }
            }
        }
    }
}
