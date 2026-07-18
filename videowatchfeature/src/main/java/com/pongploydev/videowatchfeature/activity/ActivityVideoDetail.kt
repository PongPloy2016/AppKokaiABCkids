package com.pongploydev.videowatchfeature.activity

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.pongploydev.videowatchfeature.Constant
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetail
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.utils.Tools
import com.pongploydev.videowatchfeature.viewmodel.VideoDetailViewModel
import retrofit2.Call

class ActivityVideoDetail : AppCompatActivity() {

    private val viewModel: VideoDetailViewModel by viewModels()
    private var callbackCall: Call<CallbackVideoDetail>? = null
    private lateinit var lytMainContent: LinearLayout
    private var video: Video? = null
    private lateinit var videoId: String
    private lateinit var txtTitle: TextView
    private lateinit var videoCategoryBadge: TextView
    private lateinit var txtTotalViews: TextView
    private lateinit var txtDateTime: TextView
    private lateinit var lytView: LinearLayout
    private lateinit var lytDate: LinearLayout
    private lateinit var videoThumbnail: ImageView
    private lateinit var webView: WebView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytSuggested: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPref: SharedPref
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var adsPref: AdsPref
    private lateinit var tools: Tools

    // ExoPlayer variables
    private var exoPlayer: ExoPlayer? = null
    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var dataSourceFactory: DataSource.Factory
    private var isPlayerInitialized = false
    private lateinit var progressBar: ProgressBar
    private lateinit var iconPlay: ImageView

    // Local Video Variables
    private lateinit var lytLocalVideoContainer: RelativeLayout
    private lateinit var localVideoView: VideoView
    private lateinit var btnLocalPlayPause: ImageButton
    private lateinit var btnLocalReplay: ImageButton
    private lateinit var btnLocalBack: ImageButton
    private lateinit var progressBarLocal: ProgressBar
    private var isPlaying = false

    // Pulse Animation
    private lateinit var liveIndicatorDot: View

    // Card Views
    private lateinit var cardPlayer: CardView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)

        videoId = intent.getStringExtra(Constant.KEY_VID) ?: intent.getIntExtra(Constant.KEY_VID, -1).toString()

        tools = Tools(this)
        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary)
        swipeRefreshLayout.isRefreshing = false

        lytMainContent = findViewById(R.id.lyt_main_content)
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        txtTitle = findViewById(R.id.video_title)
        videoCategoryBadge = findViewById(R.id.video_category_badge)
        webView = findViewById(R.id.video_description)
        txtTotalViews = findViewById(R.id.total_views)
        txtDateTime = findViewById(R.id.date_time)
        lytView = findViewById(R.id.lyt_view_count)
        lytDate = findViewById(R.id.lyt_date)
        btnFavorite = findViewById(R.id.img_favorite)
        btnShare = findViewById(R.id.btn_share)

        lytSuggested = findViewById(R.id.lyt_suggested)
        cardPlayer = findViewById(R.id.cardPlayer)
        
        // Pulse Indicator
        liveIndicatorDot = findViewById(R.id.liveIndicatorDot)
        startPulseAnimation()

        initExoPlayer()
        initLocalVideoViews()
        setupViewModelObservers()
        setupFavorite()
        setupListeners()

        requestAction()

        swipeRefreshLayout.setOnRefreshListener {
            requestAction()
        }
    }

    private fun startPulseAnimation() {
        YoYo.with(Techniques.Pulse)
            .duration(1000)
            .repeat(YoYo.INFINITE)
            .playOn(liveIndicatorDot)
    }

    private fun setupListeners() {
        btnShare.setOnClickListener { video?.let { v -> Tools.shareContentDetail(this, v) } }
        iconPlay.setOnClickListener { video?.let { v -> playVideo(v) } }
        findViewById<Button>(R.id.btnRefreshStream).setOnClickListener {
            requestAction()
            Toast.makeText(this, "กำลังรีเฟรชสัญญาณ...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initLocalVideoViews() {
        lytLocalVideoContainer = findViewById(R.id.lyt_local_video_container)
        localVideoView = findViewById(R.id.local_video_view)
        progressBarLocal = findViewById(R.id.progress_bar_local)
        btnLocalReplay = findViewById(R.id.btn_local_replay)
        btnLocalPlayPause = findViewById(R.id.btn_local_play_pause)
        btnLocalBack = findViewById(R.id.btn_local_back)
    }

    private fun initExoPlayer() {
        styledPlayerView = findViewById(R.id.exoPlayerView)
        progressBar = findViewById(R.id.progressBar)
        iconPlay = findViewById(R.id.icon_play)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).setUserAgent(userAgent)
        dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(this).setTrackSelector(DefaultTrackSelector(this, AdaptiveTrackSelection.Factory())).setLoadControl(DefaultLoadControl()).build()
        styledPlayerView.player = exoPlayer
        styledPlayerView.useController = true

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        progressBar.visibility = View.GONE
                        styledPlayerView.visibility = View.VISIBLE
                        findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
                        if (!exoPlayer!!.isPlaying) exoPlayer!!.playWhenReady = true
                    }
                    Player.STATE_BUFFERING -> progressBar.visibility = View.VISIBLE
                    Player.STATE_ENDED -> {
                        styledPlayerView.visibility = View.GONE
                        findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                progressBar.visibility = View.GONE
                styledPlayerView.visibility = View.GONE
                findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.VISIBLE
                Toast.makeText(this@ActivityVideoDetail, "Error playing video", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val userAgent: String get() = Util.getUserAgent(this, "PaintApp")

    private fun setupViewModelObservers() {
        viewModel.videoDetail.observe(this) { responseHome -> responseHome?.let { displayAllData(it) } }
        viewModel.isLoading.observe(this) { isLoading -> swipeProgress(isLoading) }
        viewModel.error.observe(this) { errorMsg -> if (errorMsg != null) onFailRequest() }
    }

    private fun setupFavorite() {
        btnFavorite.setOnClickListener { video?.let { viewModel.toggleFavorite(it) } }
        viewModel.checkFavoriteStatus(videoId)
        viewModel.isFavorite.observe(this) { isFav -> updateFavoriteIcon(isFav) }
    }

    private fun updateFavoriteIcon(isFav: Boolean) {
        btnFavorite.setImageResource(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
    }

    private fun requestAction() {
        showFailedView(false, "")
        viewModel.loadVideoDetail(videoId, isRefresh = true)
    }

    private fun onFailRequest() {
        swipeProgress(false)
        lytMainContent.visibility = View.GONE
        showFailedView(true, getString(R.string.failed_text))
    }

    private fun showFailedView(show: Boolean, message: String) {
        val lytFailed = findViewById<View>(R.id.lyt_failed_home)
        findViewById<TextView>(R.id.failed_message).text = message
        lytFailed.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.failed_retry).setOnClickListener { requestAction() }
    }

    private fun swipeProgress(show: Boolean) {
        swipeRefreshLayout.isRefreshing = show
        if (!show) {
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
            lytMainContent.visibility = View.VISIBLE
        } else {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
            lytMainContent.visibility = View.GONE
        }
    }

    private fun displayAllData(responseHome: CallbackVideoDetailV1) {
        val detailVideo = responseHome.post ?: responseHome.posts.firstOrNull()
        detailVideo?.let {
            this.video = it
            displayData(it)
        }
    }

    fun displayData(video: Video) {
        if (video.category_name.isNullOrBlank()) {
            videoCategoryBadge.visibility = View.GONE
        } else {
            videoCategoryBadge.visibility = View.VISIBLE
            videoCategoryBadge.text = video.category_name
            try {
                video.getCategoryColor()?.let { colorStr ->
                    videoCategoryBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStr))
                }
            } catch (e: Exception) {}
        }

        txtTitle.text = video.video_title
        if (AppConfig.ENABLE_VIEW_COUNT) {
            txtTotalViews.text = Tools.withSuffix(video.total_views) + " " + getString(R.string.views_count)
        } else {
            lytView.visibility = View.GONE
        }

        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txtDateTime.text = if (AppConfig.DISPLAY_DATE_AS_TIME_AGO) Tools.getTimeAgo(video.date_time) else Tools.getFormatedDateSimple(video.date_time)
        } else {
            lytDate.visibility = View.GONE
        }

        val thumbnailUrl = if (video.video_type != null && video.video_type.lowercase() == "youtube" && video.video_thumbnail.isEmpty()) {
            Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ
        } else if (video.video_thumbnail.startsWith("http")) {
            Uri.parse(video.video_thumbnail).toString()
        } else {
            val base = if (sharedPref.baseUrl.endsWith("/")) sharedPref.baseUrl.substring(0, sharedPref.baseUrl.length - 1) else sharedPref.baseUrl
            base + "/video_image/" + Uri.encode(video.video_thumbnail)
        }

        Glide.with(this).load(thumbnailUrl).placeholder(R.drawable.ic_thumbnail).diskCacheStrategy(DiskCacheStrategy.ALL).into(videoThumbnail)

        Tools.displayPostDescription(this, webView, video.video_description)
        
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isPlayerInitialized) { playVideo(video); isPlayerInitialized = true }
            lytSuggested.visibility = View.VISIBLE
        }, 1000)
    }

    private fun playVideo(video: Video) {
        if (video.video_type == "local" || (video.video_url != null && video.video_url.startsWith("android.resource"))) {
            setupLocalVideo()
            return
        }

        val videoUrl = if (video.video_type != null && video.video_type.equals("Upload", ignoreCase = true)) sharedPref.baseUrl + "/upload/video/" + video.video_url else video.video_url
        if (videoUrl != null && video.video_type != "youtube") {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = true
        }
    }

    override fun onPause() { super.onPause(); exoPlayer?.playWhenReady = false }
    override fun onResume() { super.onResume(); if (exoPlayer != null && isPlayerInitialized) exoPlayer?.playWhenReady = true }
    override fun onDestroy() { super.onDestroy(); exoPlayer?.release() }

    private fun setupLocalVideo() {
        styledPlayerView.visibility = View.GONE
        findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
        lytLocalVideoContainer.visibility = View.VISIBLE

        try {
            localVideoView.setVideoURI(Uri.parse(video!!.video_url))
            localVideoView.setOnPreparedListener { mp ->
                progressBarLocal.visibility = View.GONE
                localVideoView.start()
                isPlaying = true
            }
            localVideoView.setOnCompletionListener { isPlaying = false }
            btnLocalPlayPause.setOnClickListener {
                if (isPlaying) localVideoView.pause() else localVideoView.start()
                isPlaying = !isPlaying
            }
            btnLocalReplay.setOnClickListener { localVideoView.seekTo(0); localVideoView.start(); isPlaying = true }
            btnLocalBack.setOnClickListener { finish() }
        } catch (e: Exception) {}
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var isVideoPlaying = isPlaying
            if (::localVideoView.isInitialized && video?.video_type == "local") isVideoPlaying = localVideoView.isPlaying
            if (isVideoPlaying) {
                val params = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
                enterPictureInPictureMode(params)
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            findViewById<View>(R.id.cardHeader).visibility = View.GONE
            findViewById<View>(R.id.cardInfo).visibility = View.GONE
            cardPlayer.radius = 0f
            (cardPlayer.layoutParams as ConstraintLayout.LayoutParams).setMargins(0, 0, 0, 0)
            (cardPlayer.layoutParams as ConstraintLayout.LayoutParams).height = ConstraintLayout.LayoutParams.MATCH_PARENT
        } else {
            findViewById<View>(R.id.cardHeader).visibility = View.VISIBLE
            findViewById<View>(R.id.cardInfo).visibility = View.VISIBLE
            cardPlayer.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)
            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
            val params = cardPlayer.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(margin, margin, margin, margin)
            params.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
            cardPlayer.layoutParams = params
        }
    }
}
