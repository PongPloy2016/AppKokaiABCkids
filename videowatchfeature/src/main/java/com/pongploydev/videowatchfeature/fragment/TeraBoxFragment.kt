package com.pongploydev.videowatchfeature.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.pongploydev.videowatchfeature.R

class TeraBoxFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var playerView: StyledPlayerView

    private var exoPlayer: ExoPlayer? = null
    private var targetUrl: String = DEFAULT_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            targetUrl = it.getString(ARG_URL, DEFAULT_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_terabox, container, false)
        initViews(view)

        // ปรับปรุง URL สำหรับ Google Drive ให้เป็น /preview เพื่อให้เล่นง่ายขึ้น
        var finalUrl = targetUrl
        if (finalUrl.contains("drive.google.com") && finalUrl.contains("/view")) {
            finalUrl = finalUrl.replace("/view", "/preview")
        }

        if (isMediaUrl(finalUrl)) {
            playerView.visibility = View.VISIBLE
            swipeRefresh.visibility = View.GONE
            setupExoPlayer(finalUrl)
        } else {
            playerView.visibility = View.GONE
            swipeRefresh.visibility = View.VISIBLE
            setupWebView()
            setupSwipeRefresh()
            loadUrl(finalUrl)
        }

        return view
    }

    private fun initViews(view: View) {
        webView = view.findViewById(R.id.web_view)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        playerView = view.findViewById(R.id.player_view)
    }

    private fun isMediaUrl(url: String): Boolean {
        val lower = url.lowercase()
        if (lower.contains("/s/") || lower.contains("/sharing/")) {
            return false
        }
        return lower.contains(".mkv") ||
                lower.contains(".mp4") ||
                lower.contains(".webm") ||
                lower.contains("sta_ft=mkv") ||
                lower.contains("sta_ft=mp4") ||
                lower.contains("freeterabox.com/file/") ||
                lower.contains("d8.freeterabox.com")
    }

    private fun setupExoPlayer(url: String) {
        val userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        val defaultHeaders = mapOf(
            "User-Agent" to userAgent,
            "Referer" to "https://freeterabox.com/",
            "Accept" to "*/*"
        )
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(userAgent)
            .setDefaultRequestProperties(defaultHeaders)
        val dataSourceFactory = DefaultDataSource.Factory(requireContext(), httpDataSourceFactory)

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> progressBar.visibility = View.VISIBLE
                        Player.STATE_READY -> progressBar.visibility = View.GONE
                        Player.STATE_ENDED -> progressBar.visibility = View.GONE
                        else -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    progressBar.visibility = View.GONE
                    playerView.visibility = View.GONE
                    swipeRefresh.visibility = View.VISIBLE
                    setupWebView()
                    setupSwipeRefresh()
                    loadUrl()
                }
            })
        }

        playerView.player = exoPlayer
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        @Suppress("DEPRECATION")
        settings.databaseEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.allowFileAccess = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                
                // ฉีด JavaScript เพื่อคลิกปุ่มเล่นอัตโนมัติ (แก้ไขให้ปลอดภัยขึ้น ไม่ไปกดปุ่มลบ)
                val js = """
                    (function() {
                        function forceClick(el) {
                            if (!el) return;
                            // ตรวจสอบความปลอดภัย: ไม่คลิกถ้าเป็นปุ่มอันตราย
                            var text = (el.innerText || el.getAttribute('aria-label') || '').toLowerCase();
                            var dangerWords = ['ลบ', 'นำออก', 'delete', 'remove', 'trash', 'ถังขยะ'];
                            for (var word of dangerWords) {
                                if (text.indexOf(word) > -1) return;
                            }

                            var events = ['mousedown', 'mouseup', 'click'];
                            events.forEach(function(evt) {
                                try {
                                    var e = document.createEvent('MouseEvent');
                                    e.initMouseEvent(evt, true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
                                    el.dispatchEvent(e);
                                } catch(err) {}
                            });
                        }

                        function tryPlay() {
                            // 1. เล่นวิดีโอโดยตรง
                            var vids = document.getElementsByTagName('video');
                            for (var i = 0; i < vids.length; i++) {
                                if (vids[i].paused) vids[i].play().catch(function(e){});
                                vids[i].muted = false;
                            }
                            
                            // 2. คลิกเฉพาะปุ่มที่เกี่ยวกับ "เล่น" หรือ "Play" เท่านั้น
                            var playSelectors = [
                                '.ytp-large-play-button', '.play-button', 
                                '[aria-label*="Play"]', '[aria-label*="เล่น"]',
                                '.ytp-cued-thumbnail-overlay'
                            ];
                            playSelectors.forEach(function(s) {
                                var elms = document.querySelectorAll(s);
                                for (var j = 0; j < elms.length; j++) forceClick(elms[j]);
                            });
                            
                            // 3. คลิกตรงกลาง (เฉพาะถ้าเจอ Overlay ของวิดีโอ)
                            var centerEl = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);
                            if (centerEl && (centerEl.tagName === 'DIV' || centerEl.tagName === 'SVG')) {
                                // ตรวจสอบว่าไม่ใช่ปุ่มเมนูหรือปุ่มนำออก
                                forceClick(centerEl);
                            }
                        }

                        var count = 0;
                        var timer = setInterval(function() {
                            tryPlay();
                            count++;
                            if (count > 10) clearInterval(timer); // ลดจำนวนครั้งลงเพื่อความปลอดภัย
                        }, 1000);
                    })();
                """.trimIndent()

                view?.evaluateJavascript(js, null)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    // ป้องกันข้อผิดพลาด ERR_UNKNOWN_URL_SCHEME โดยการกรองเปิดเฉพาะ http และ https
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        if (isMediaUrl(url)) {
                            swipeRefresh.visibility = View.GONE
                            playerView.visibility = View.VISIBLE
                            setupExoPlayer(url)
                            return true
                        }
                        return false // ให้ WebView โหลดต่อปกติ
                    } else {
                        // ถ้าเป็น dubox:// หรือเทอร์โบลิงก์อื่นๆ ให้บล็อกไว้ไม่ให้หน้าขาว (หรือเปิดแอปถ้าติดตั้งไว้)
                        try {
                            if (url.startsWith("dubox://") || url.startsWith("terabox://")) {
                                // บล็อกไว้เพื่อให้ user ดูผ่านเว็บในแอปเราต่อไปได้
                                return true 
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true // บล็อก scheme อื่นๆ ที่ WebView ไม่รู้จัก
                    }
                }
                return true
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
    }

    private fun loadUrl(url: String = targetUrl) {
        val extraHeaders = HashMap<String, String>()
        extraHeaders["Referer"] = "https://freeterabox.com/"
        webView.loadUrl(url, extraHeaders)
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }

    override fun onDestroyView() {
        exoPlayer?.release()
        exoPlayer = null
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroyView()
    }

    companion object {
        private const val ARG_URL = "arg_terabox_url"
        private const val DEFAULT_URL = "https://drive.google.com/file/d/1A5LzbVDCIMkBLZzptAynE97GgKop5WUh/view?usp=drive_link"
        fun newInstance(url: String = DEFAULT_URL): TeraBoxFragment {
            val fragment = TeraBoxFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
            fragment.arguments = args
            return fragment
        }
    }
}
