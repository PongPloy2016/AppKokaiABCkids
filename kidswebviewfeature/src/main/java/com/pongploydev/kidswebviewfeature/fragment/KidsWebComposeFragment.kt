package com.pongploydev.kidswebviewfeature.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.pongploydev.kidswebviewfeature.compose.KidsWebViewScreen
import com.pongploydev.kidswebviewfeature.compose.KidsWebViewTheme

class KidsWebComposeFragment : Fragment() {

    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
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
                    KidsWebViewTheme {
                        KidsWebViewScreen(
                            url = "https://www.youngciety.com/",
                            title = "ท่องเว็บสนุก (Youngciety)",
                            onBackClick = {
                                val act = activity
                                if (act != null && !act.isFinishing) {
                                    if (parentFragmentManager.backStackEntryCount > 0) {
                                        parentFragmentManager.popBackStack()
                                    } else {
                                        act.finish()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
