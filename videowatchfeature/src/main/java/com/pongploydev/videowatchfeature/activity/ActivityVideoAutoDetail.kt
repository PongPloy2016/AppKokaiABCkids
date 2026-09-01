package com.pongploydev.videowatchfeature.activity

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.speech.tts.TextToSpeech
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.facebook.shimmer.ShimmerFrameLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pongploydev.videowatchfeature.Constant
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.adapter.AdapterVideo
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.utils.Tools
import com.pongploydev.videowatchfeature.viewmodel.VideoDetailViewModel
import java.util.Locale

class ActivityVideoAutoDetail : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val viewModel: VideoDetailViewModel by viewModels()

    private lateinit var lytMainContent: LinearLayout
    private var video: Video? = null
    private lateinit var videoId: String

    private lateinit var txtTitle: TextView
    private lateinit var videoCategoryBadge: TextView
    private lateinit var txtDuration: TextView
    private lateinit var txtTotalViews: TextView
    private lateinit var txtDateTime: TextView
    private lateinit var lytView: LinearLayout
    private lateinit var lytDate: LinearLayout
    private lateinit var videoThumbnail: ImageView
    private lateinit var webViewDescription: WebView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytSuggested: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPref: SharedPref
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var adsPref: AdsPref
    
    private lateinit var rvSuggested: RecyclerView
    private var adapterSuggested: AdapterVideo? = null

    // TTS
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    // YouTube Player Variables
    private lateinit var youTubePlayerView: YouTubePlayerView
    private var youTubePlayer: YouTubePlayer? = null
    private lateinit var lytThumbnailContainer: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var playerWebView: WebView

    // SeekBar and Control Variables
    private lateinit var seekBar: YouTubePlayerSeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalDuration: TextView
    private var isPlaying = false
    private val seekBarHandler = Handler(Looper.getMainLooper())
    private var updateSeekBarRunnable: Runnable? = null
    private var videoDurationFloat = 0f

    // Local Video Variables
    private lateinit var lytLocalVideoContainer: RelativeLayout
    private lateinit var localVideoView: VideoView
    private lateinit var progressBarLocal: ProgressBar
    private var isFullScreen = false

    // Player Card Container
    private lateinit var lytYoutubeCardContainer: CardView
    private lateinit var lytCustomControls: RelativeLayout
    private lateinit var viewPanel: View
    private val youtubeControlsHideHandler = Handler(Looper.getMainLooper())
    private val hideYouTubeControlsRunnable = Runnable { hideYouTubeControls() }

    // Pulse Animation
    private lateinit var liveIndicatorDot: View

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_auto_detail)

        if (!intent.hasExtra(Constant.KEY_VID)) {
            finish()
            return
        }
        videoId = intent.getStringExtra(Constant.KEY_VID) ?: intent.getIntExtra(Constant.KEY_VID, -1).toString()

        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)

        textToSpeech = TextToSpeech(this, this)

        initViews()
        setupViewModelObservers()
        setupFavorite()
        setupListeners()

        requestAction()
        viewModel.loadRandomVideos()

        handleOnBackPressed()
    }

    private fun initViews() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.kids_accent)
        swipeRefreshLayout.setOnRefreshListener {
            requestAction()
            viewModel.loadRandomVideos()
        }

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
        lytMainContent = findViewById(R.id.lyt_main_content)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        txtTitle = findViewById(R.id.video_title)
        videoCategoryBadge = findViewById(R.id.video_category_badge)
        txtDuration = findViewById(R.id.video_duration)
        webViewDescription = findViewById(R.id.video_description)
        txtTotalViews = findViewById(R.id.total_views)
        txtDateTime = findViewById(R.id.date_time)
        lytView = findViewById(R.id.lyt_view_count)
        lytDate = findViewById(R.id.lyt_date)
        lytSuggested = findViewById(R.id.lyt_suggested)
        rvSuggested = findViewById(R.id.rv_suggested)
        
        btnFavorite = findViewById(R.id.img_favorite)
        btnShare = findViewById(R.id.btn_share)

        // Initialize Player Views
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        playerWebView = findViewById(R.id.player_web_view)
        lytThumbnailContainer = findViewById(R.id.lyt_thumbnail_container)
        progressBar = findViewById(R.id.progress_bar)
        lytYoutubeCardContainer = findViewById(R.id.lyt_youtube_card_container)
        lytCustomControls = findViewById(R.id.lyt_custom_controls)
        viewPanel = findViewById(R.id.panel_view)

        // Initialize Controls
        seekBar = findViewById(R.id.youtube_player_seekbar)
        txtCurrentTime = findViewById(R.id.txt_current_time)
        txtTotalDuration = findViewById(R.id.txt_duration)

        // Init Local Video Views
        lytLocalVideoContainer = findViewById(R.id.lyt_local_video_container)
        localVideoView = findViewById(R.id.local_video_view)
        progressBarLocal = findViewById(R.id.progress_bar_local)

        // Pulse Indicator
        liveIndicatorDot = findViewById(R.id.liveIndicatorDot)
        startPulseAnimation()

        setupSuggestedRecyclerView()
        initYouTubePlayer()
    }

    private fun startPulseAnimation() {
        YoYo.with(Techniques.Pulse)
            .duration(1000)
            .repeat(YoYo.INFINITE)
            .playOn(liveIndicatorDot)

        YoYo.with(Techniques.BounceInDown).duration(1200).playOn(findViewById(R.id.cardHeader))
        YoYo.with(Techniques.FadeInUp).duration(1000).playOn(txtTitle)
    }

    private fun setupListeners() {
        btnShare.setOnClickListener { 
            video?.let { v -> 
                YoYo.with(Techniques.Tada).duration(500).playOn(btnShare)
                Tools.shareContentDetail(this, v) 
            } 
        }
        
        viewPanel.setOnClickListener { toggleYouTubeControlsVisibility() }
        
        findViewById<View>(R.id.icon_play).setOnClickListener { 
            video?.let { v -> 
                YoYo.with(Techniques.Bounce).duration(400).playOn(it)
                playVideoInternal(v)
            }
        }
        
        findViewById<Button>(R.id.btnRefreshStream).setOnClickListener {
            YoYo.with(Techniques.RotateIn).duration(500).playOn(it)
            requestAction()
        }
    }

    private fun playVideoInternal(v: Video) {
        var videoUrl = if (v.video_type?.equals("Upload", ignoreCase = true) == true) sharedPref.baseUrl + "/upload/video/" + v.video_url else v.video_url
        
        if (v.video_type?.lowercase() == "youtube") {
            youTubePlayer?.loadVideo(v.video_id ?: "", 0f)
            return
        } else if (v.video_type?.lowercase() == "local") {
            setupLocalVideo()
            return
        }

        if (!videoUrl.isNullOrEmpty()) {
            // ปรับปรุง URL สำหรับ Google Drive
            if (videoUrl.contains("drive.google.com") && videoUrl.contains("/view")) {
                videoUrl = videoUrl.replace("/view", "/preview")
            }

            if (isMediaUrl(videoUrl)) {
                val intent = Intent(this, ActivityVideoDetail::class.java)
                intent.putExtra(Constant.KEY_VID, v.vid)
                startActivity(intent)
                finish()
            } else {
                setupPlayerWebView(videoUrl)
            }
        }
    }

    private val browserUserAgent: String
        get() = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private fun isMediaUrl(url: String): Boolean {
        val lower = url.lowercase()
        if (lower.contains("/s/") || lower.contains("/sharing/")) return false
        return lower.contains(".mkv") || lower.contains(".mp4") || lower.contains(".webm") ||
                lower.contains("sta_ft=mkv") || lower.contains("sta_ft=mp4") ||
                lower.contains("freeterabox.com/file/") || lower.contains("d8.freeterabox.com")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupPlayerWebView(url: String) {
        playerWebView.visibility = View.VISIBLE
        youTubePlayerView.visibility = View.GONE
        lytLocalVideoContainer.visibility = View.GONE
        lytThumbnailContainer.visibility = View.GONE

        val settings = playerWebView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.allowFileAccess = true
        settings.userAgentString = browserUserAgent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        playerWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                
                val js = """
                    (function() {
                        function forceClick(el) {
                            if (!el) return;
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
                            var vids = document.getElementsByTagName('video');
                            for (var i = 0; i < vids.length; i++) {
                                if (vids[i].paused) vids[i].play().catch(function(e){});
                                vids[i].muted = false;
                            }
                            var playSelectors = ['.ytp-large-play-button', '.play-button', '[aria-label*="Play"]', '[aria-label*="เล่น"]', '.ytp-cued-thumbnail-overlay'];
                            playSelectors.forEach(function(s) {
                                var elms = document.querySelectorAll(s);
                                for (var j = 0; j < elms.length; j++) forceClick(elms[j]);
                            });
                            var centerEl = document.elementFromPoint(window.innerWidth/2, window.innerHeight/2);
                            if (centerEl && (centerEl.tagName === 'DIV' || centerEl.tagName === 'SVG')) forceClick(centerEl);
                        }
                        var count = 0;
                        var timer = setInterval(function() {
                            tryPlay();
                            count++;
                            if (count > 15) clearInterval(timer);
                        }, 1000);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(js, null)
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.startsWith("http://") || url.startsWith("https://")) return false
                    return true
                }
                return true
            }
        }

        val extraHeaders = HashMap<String, String>()
        extraHeaders["Referer"] = "https://freeterabox.com/"
        playerWebView.loadUrl(url, extraHeaders)
    }

    private fun initYouTubePlayer() {
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull player: YouTubePlayer) {
                youTubePlayer = player
                player.addListener(seekBar)
                seekBar.youtubePlayerSeekBarListener = object : YouTubePlayerSeekBarListener {
                    override fun seekTo(time: Float) {
                        player.seekTo(time)
                    }
                }
                video?.let { playOrSetupVideo(it, player) }
            }

            override fun onStateChange(@NonNull youTubePlayer: YouTubePlayer, @NonNull state: PlayerConstants.PlayerState) {
                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> {
                        isPlaying = true
                        lytThumbnailContainer.visibility = View.GONE
                        youTubePlayerView.visibility = View.VISIBLE
                    }
                    PlayerConstants.PlayerState.PAUSED, PlayerConstants.PlayerState.ENDED -> {
                        isPlaying = false
                    }
                    else -> {}
                }
            }

            override fun onCurrentSecond(@NonNull youTubePlayer: YouTubePlayer, second: Float) {
                txtCurrentTime.text = formatTime(second.toInt())
            }

            override fun onVideoDuration(@NonNull youTubePlayer: YouTubePlayer, duration: Float) {
                videoDurationFloat = duration
                txtTotalDuration.text = formatTime(duration.toInt())
            }

            override fun onError(@NonNull youTubePlayer: YouTubePlayer, @NonNull error: PlayerConstants.PlayerError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ActivityVideoAutoDetail, "เกิดข้อผิดพลาดในการเล่นวิดีโอ", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun playOrSetupVideo(video: Video, player: YouTubePlayer) {
        if (video.video_type?.lowercase() == "youtube" && !video.video_id.isNullOrEmpty()) {
            player.loadVideo(video.video_id ?: "", 0f)
            isPlaying = true
        } else if (video.video_type?.lowercase() == "local") {
            setupLocalVideo()
        } else {
            playVideoInternal(video)
        }
    }

    private fun setupLocalVideo() {
        youTubePlayerView.visibility = View.GONE
        lytThumbnailContainer.visibility = View.GONE
        lytLocalVideoContainer.visibility = View.VISIBLE
        progressBarLocal.visibility = View.VISIBLE

        try {
            val videoUrl = video?.video_url
            if (!videoUrl.isNullOrEmpty()) {
                localVideoView.setVideoPath(videoUrl)
                localVideoView.setOnPreparedListener { mp ->
                    progressBarLocal.visibility = View.GONE
                    localVideoView.start()
                    isPlaying = true
                }
                localVideoView.setOnCompletionListener { isPlaying = false }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun setupViewModelObservers() {
        viewModel.videoDetail.observe(this) { responseHome ->
            val v = responseHome?.post ?: responseHome?.posts?.firstOrNull()
            v?.let {
                this.video = it
                displayData(it)
                youTubePlayer?.let { p -> playOrSetupVideo(it, p) }
            }
        }
        viewModel.randomVideos.observe(this) { videos ->
            if (videos.isNotEmpty()) {
                lytSuggested.visibility = View.VISIBLE
                adapterSuggested?.updateData(videos)
            }
        }
        viewModel.isLoading.observe(this) { isLoading -> swipeProgress(isLoading) }
    }

    private fun displayData(v: Video) {
        txtTitle.text = v.video_title
        txtDuration.text = v.video_duration

        // อ่านชื่อวิดีโออัตโนมัติ (Auto Read Title)
        v.video_title?.let { title ->
            if (isTtsReady) {
                textToSpeech?.speak(title, TextToSpeech.QUEUE_FLUSH, null, "video_title_tts")
            }
        }

        if (v.category_name.isNullOrBlank()) {
            videoCategoryBadge.visibility = View.GONE
        } else {
            videoCategoryBadge.visibility = View.VISIBLE
            videoCategoryBadge.text = "🌟 " + v.category_name
            try {
                val colorStr = v.getCategoryColor() ?: "#4CAF50"
                videoCategoryBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStr))
            } catch (e: Exception) {}
        }

        if (AppConfig.ENABLE_VIEW_COUNT) {
            txtTotalViews.text = "👀 " + Tools.withSuffix(v.total_views) + " " + getString(R.string.views_count)
        }
        
        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txtDateTime.text = "📅 " + (if (AppConfig.DISPLAY_DATE_AS_TIME_AGO) Tools.getTimeAgo(v.date_time) else Tools.getFormatedDateSimple(v.date_time))
        }

        var thumbnailUrl = v.video_thumbnail
        if (v.video_type?.lowercase() == "youtube" && thumbnailUrl.isEmpty()) {
            thumbnailUrl = Constant.YOUTUBE_IMAGE_FRONT + v.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ
        } else if (!thumbnailUrl.startsWith("http")) {
            val base = if (sharedPref.baseUrl.endsWith("/")) sharedPref.baseUrl.substring(0, sharedPref.baseUrl.length - 1) else sharedPref.baseUrl
            thumbnailUrl = base + "/video_image/" + Uri.encode(thumbnailUrl)
        }

        Glide.with(this).load(thumbnailUrl).placeholder(R.drawable.ic_thumbnail).diskCacheStrategy(DiskCacheStrategy.ALL).into(videoThumbnail)
        Tools.displayPostDescription(this, webViewDescription, v.video_description)
        
        lytMainContent.visibility = View.VISIBLE
        YoYo.with(Techniques.FadeInUp).duration(800).playOn(lytMainContent)
    }

    private fun setupFavorite() {
        btnFavorite.setOnClickListener { 
            video?.let { 
                YoYo.with(Techniques.StandUp).duration(500).playOn(btnFavorite)
                viewModel.toggleFavorite(it) 
            }
        }
        viewModel.checkFavoriteStatus(videoId)
        viewModel.isFavorite.observe(this) { isFav ->
            btnFavorite.setImageResource(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        }
    }

    private fun setupSuggestedRecyclerView() {
        rvSuggested.layoutManager = LinearLayoutManager(this)
        adapterSuggested = AdapterVideo(this, rvSuggested, mutableListOf())
        rvSuggested.adapter = adapterSuggested
        adapterSuggested?.setOnItemClickListener { _, video, _ ->
            val intent = Intent(this, if (video.video_type?.lowercase() == "youtube") ActivityVideoAutoDetail::class.java else ActivityVideoDetail::class.java)
            intent.putExtra(Constant.KEY_VID, video.vid)
            startActivity(intent)
            finish()
        }
    }

    private fun requestAction() {
        viewModel.loadVideoDetail(videoId, isRefresh = true)
    }

    private fun swipeProgress(show: Boolean) {
        swipeRefreshLayout.isRefreshing = show
        if (!show) {
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
        } else {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
            lytMainContent.visibility = View.GONE
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }

    private fun toggleYouTubeControlsVisibility() {
        if (findViewById<View>(R.id.custom_controls_container).visibility == View.VISIBLE) {
            hideYouTubeControls()
        } else {
            findViewById<View>(R.id.custom_controls_container).visibility = View.VISIBLE
            youtubeControlsHideHandler.removeCallbacks(hideYouTubeControlsRunnable)
            youtubeControlsHideHandler.postDelayed(hideYouTubeControlsRunnable, 3000)
        }
    }

    private fun hideYouTubeControls() {
        if (isPlaying) findViewById<View>(R.id.custom_controls_container).visibility = View.GONE
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullScreen) toggleFullscreen() else finish()
            }
        })
    }

    private fun toggleFullscreen() {
        if (isFullScreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            isFullScreen = false
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            isFullScreen = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            enterFullScreenUI()
        } else {
            exitFullScreenUI()
        }
    }

    private fun enterFullScreenUI() {
        val params = lytYoutubeCardContainer.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(0, 0, 0, 0)
        params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        lytYoutubeCardContainer.layoutParams = params
        lytYoutubeCardContainer.radius = 0f
        findViewById<View>(R.id.cardHeader).visibility = View.GONE
        findViewById<View>(R.id.cardInfo).visibility = View.GONE
        isFullScreen = true
    }

    private fun exitFullScreenUI() {
        val params = lytYoutubeCardContainer.layoutParams as ConstraintLayout.LayoutParams
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        params.setMargins(margin, margin, margin, margin)
        params.width = 0
        params.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
        lytYoutubeCardContainer.layoutParams = params
        lytYoutubeCardContainer.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)
        findViewById<View>(R.id.cardHeader).visibility = View.VISIBLE
        findViewById<View>(R.id.cardInfo).visibility = View.VISIBLE
        isFullScreen = false
    }

    override fun onPause() { super.onPause() }
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        youTubePlayerView.release()
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.ERROR) {
            textToSpeech?.language = Locale("th", "TH")
            textToSpeech?.setSpeechRate(0.7f)
            isTtsReady = true
            
            // ถ้าโหลดวิดีโอเสร็จก่อน TTS พร้อม ให้สั่งอ่านที่นี่
            video?.video_title?.let { title ->
                textToSpeech?.speak(title, TextToSpeech.QUEUE_FLUSH, null, "video_title_tts")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
