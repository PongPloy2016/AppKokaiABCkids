package com.pongploydev.newsfeature.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pongploydev.newsfeature.NewsDetailActivity
import com.pongploydev.newsfeature.ui.NewsScreen
import com.pongploydev.newsfeature.ui.NewsTheme
import com.pongploydev.newsfeature.ui.NewsViewModel

/** แท็บ "ข่าวสาร" แสดงข่าวสำหรับเด็กจาก Google News RSS แยกตามหัวข้อ */
class NewsComposeFragment : Fragment() {

    private val viewModel: NewsViewModel by viewModels()
    private var previousLightStatusBars: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NewsTheme {
                    val feed = viewModel.selectedFeed
                    NewsScreen(
                        feeds = viewModel.feeds,
                        selectedFeed = feed,
                        state = viewModel.stateOf(feed),
                        onSelectFeed = viewModel::select,
                        onRefresh = viewModel::refresh,
                        onArticleClick = { article ->
                            startActivity(NewsDetailActivity.createIntent(requireContext(), article))
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // พื้นหลังหน้าข่าวเป็นสีเข้ม -> ให้ไอคอน status bar เป็นสีขาวขณะอยู่หน้านี้
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (previousLightStatusBars == null) {
                previousLightStatusBars = controller.isAppearanceLightStatusBars
            }
            controller.isAppearanceLightStatusBars = false
        }
        viewModel.refreshIfStale()
    }

    override fun onPause() {
        super.onPause()
        val previous = previousLightStatusBars
        if (previous != null) {
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = previous
            }
            previousLightStatusBars = null
        }
    }
}

