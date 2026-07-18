package com.pongploydev.videowatchfeature.activity

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.AsyncTask
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
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
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
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetail
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import com.pongploydev.videowatchfeature.utils.Tools
import com.pongploydev.videowatchfeature.viewmodel.VideoDetailViewModel
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityVideoAutoDetail : AppCompatActivity() {

    private val viewModel: VideoDetailViewModel by viewModels()

    private var callbackCall: Call<CallbackVideoDetail>? = null
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
    private lateinit var webView: WebView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytSuggested: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPref: SharedPref
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var adsPref: AdsPref
    private lateinit var tools: Tools

    // YouTube Player Variables
    private lateinit var youTubePlayerView: YouTubePlayerView
    private var youTubePlayer: YouTubePlayer? = null
    private lateinit var lytThumbnailContainer: RelativeLayout
    private lateinit var progressBar: ProgressBar

    // SeekBar and Control Variables
    private lateinit var seekBar: YouTubePlayerSeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalDuration: TextView
    private var isPlaying = false
    private val seekBarHandler = Handler(Looper.getMainLooper())
    private var updateSeekBarRunnable: Runnable? = null
    private var videoDuration = 0f

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

    private val seekBarTracker = SeekBarTracker()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_auto_detail)

        // Ensure video data is present
        if (!intent.hasExtra(Constant.KEY_VID)) {
            finish()
            return
        }
        videoId = intent.getStringExtra(Constant.KEY_VID) ?: intent.getIntExtra(Constant.KEY_VID, -1).toString()

        tools = Tools(this)
        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)

        adsPref.isTestMode = false
        adsPref.isFamiliesApp = true

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary)
        swipeRefreshLayout.isRefreshing = false

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
        lytMainContent = findViewById(R.id.lyt_main_content)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        txtTitle = findViewById(R.id.video_title)
        videoCategoryBadge = findViewById(R.id.video_category_badge)
        txtDuration = findViewById(R.id.video_duration)
        webView = findViewById(R.id.video_description)
        txtTotalViews = findViewById(R.id.total_views)
        txtDateTime = findViewById(R.id.date_time)
        lytView = findViewById(R.id.lyt_view_count)
        lytDate = findViewById(R.id.lyt_date)
        lytSuggested = findViewById(R.id.lyt_suggested)

        // Initialize Player Views
        youTubePlayerView = findViewById(R.id.youtube_player_view)
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

        btnFavorite = findViewById(R.id.img_favorite)
        btnShare = findViewById(R.id.btn_share)

        setupViewModelObservers()
        setupFavorite()
        setupListeners()

        requestAction()

        swipeRefreshLayout.setOnRefreshListener {
            requestAction()
        }

        handleOnBackPressed()
        initYouTubePlayer()
    }

    private fun startPulseAnimation() {
        YoYo.with(Techniques.Pulse)
            .duration(1000)
            .repeat(YoYo.INFINITE)
            .playOn(liveIndicatorDot)
    }

    private fun setupListeners() {
        btnShare.setOnClickListener { video?.let { v -> Tools.shareContentDetail(this, v) } }
        viewPanel.setOnClickListener { toggleYouTubeControlsVisibility() }
        findViewById<View>(R.id.icon_play).setOnClickListener { 
            video?.let { v -> 
                if (v.video_type == "youtube") {
                    youTubePlayer?.loadVideo(v.video_id, 0f)
                } else {
                    setupLocalVideo()
                }
            }
        }
        
        findViewById<Button>(R.id.btnRefreshStream).setOnClickListener {
            requestAction()
            Toast.makeText(this, "กำลังรีเฟรชสัญญาณ...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFavorite() {
        btnFavorite.setOnClickListener { addToFavorite() }
        viewModel.checkFavoriteStatus(videoId)
        viewModel.isFavorite.observe(this) { isFav -> updateFavoriteIcon(isFav) }
    }

    private fun updateFavoriteIcon(isFav: Boolean) {
        if (isFav) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled)
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline)
        }
    }

    private fun initYouTubePlayer() {
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull player: YouTubePlayer) {
                youTubePlayer = player
                video?.let { playOrSetupVideo(it, player) }
            }

            override fun onStateChange(@NonNull youTubePlayer: YouTubePlayer, @NonNull state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> {
                        isPlaying = true
                        startSeekBarUpdates()
                        lytThumbnailContainer.visibility = View.GONE
                        youTubePlayerView.visibility = View.VISIBLE
                    }
                    PlayerConstants.PlayerState.PAUSED, PlayerConstants.PlayerState.ENDED -> {
                        isPlaying = false
                        stopSeekBarUpdates()
                    }
                    else -> {}
                }
            }

            override fun onCurrentSecond(@NonNull youTubePlayer: YouTubePlayer, second: Float) {
                updateCurrentTime(second)
            }

            override fun onVideoDuration(@NonNull youTubePlayer: YouTubePlayer, duration: Float) {
                videoDuration = duration
                updateTotalDuration(duration)
            }

            override fun onError(@NonNull youTubePlayer: YouTubePlayer, @NonNull error: PlayerConstants.PlayerError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ActivityVideoAutoDetail, "เกิดข้อผิดพลาดในการเล่นวิดีโอ", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleFullscreen() {
        if (isFullScreen) {
            // EXIT FULL SCREEN
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

            val params = lytYoutubeCardContainer.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt(),
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt(),
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt(),
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
            )
            params.width = 0
            params.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
            lytYoutubeCardContainer.layoutParams = params
            lytYoutubeCardContainer.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)

            findViewById<View>(R.id.cardHeader).visibility = View.VISIBLE
            findViewById<View>(R.id.cardInfo).visibility = View.VISIBLE
            isFullScreen = false
        } else {
            // ENTER FULL SCREEN
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

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
    }

    private fun toggleYouTubeControlsVisibility() {
        if (findViewById<View>(R.id.custom_controls_container).visibility == View.VISIBLE) {
            hideYouTubeControls()
        } else {
            showYouTubeControls()
            startYouTubeControlsHideTimer()
        }
    }

    private fun showYouTubeControls() {
        findViewById<View>(R.id.custom_controls_container).visibility = View.VISIBLE
        youtubeControlsHideHandler.removeCallbacks(hideYouTubeControlsRunnable)
    }

    private fun hideYouTubeControls() {
        if (isPlaying) {
            findViewById<View>(R.id.custom_controls_container).visibility = View.GONE
        }
    }

    private fun startYouTubeControlsHideTimer() {
        youtubeControlsHideHandler.removeCallbacks(hideYouTubeControlsRunnable)
        youtubeControlsHideHandler.postDelayed(hideYouTubeControlsRunnable, 3000)
    }

    private fun updateCurrentTime(currentSecond: Float) {
        txtCurrentTime.text = formatTime(currentSecond.toInt())
        if (videoDuration > 0) {
            val progress = ((currentSecond / videoDuration) * 1000).toInt()
            seekBarTracker.seekTo(progress.toFloat())
        }
    }

    private fun updateTotalDuration(duration: Float) {
        txtTotalDuration.text = formatTime(duration.toInt())
    }

    private inner class SeekBarTracker : YouTubePlayerSeekBarListener {
        private var currentProgress = 0f
        override fun seekTo(time: Float) {
            if (videoDuration > 0) {
                currentProgress = (time / videoDuration) * 1000
            }
        }
        val progress: Float get() = currentProgress
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, secs) else String.format("%d:%02d", minutes, secs)
    }

    private fun startSeekBarUpdates() {
        stopSeekBarUpdates()
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                if (youTubePlayer != null && isPlaying) {
                    seekBarHandler.postDelayed(this, 1000)
                }
            }
        }
        updateSeekBarRunnable?.let { seekBarHandler.post(it) }
    }

    private fun stopSeekBarUpdates() {
        updateSeekBarRunnable?.let { seekBarHandler.removeCallbacks(it) }
        updateSeekBarRunnable = null
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullScreen) toggleFullscreen() else finish()
            }
        })
    }

    private fun playOrSetupVideo(video: Video, player: YouTubePlayer) {
        if (video.video_type != null && video.video_type.equals("local", ignoreCase = true)) {
            setupLocalVideo()
        } else if (video.video_type != null && video.video_type.equals("youtube", ignoreCase = true) && !video.video_id.isNullOrEmpty()) {
            progressBar.visibility = View.GONE
            player.loadVideo(video.video_id, 0f)
            isPlaying = true
            lytThumbnailContainer.visibility = View.GONE
            youTubePlayerView.visibility = View.VISIBLE
            startSeekBarUpdates()
        }
    }

    private fun setupViewModelObservers() {
        viewModel.videoDetail.observe(this) { responseHome ->
            val updatedVideo = responseHome?.post ?: responseHome?.posts?.firstOrNull()
            updatedVideo?.let { v ->
                this.video = v
                displayAllData(responseHome!!)
                youTubePlayer?.let { playOrSetupVideo(v, it) }
            }
        }
        viewModel.isLoading.observe(this) { isLoading -> swipeProgress(isLoading) }
        viewModel.error.observe(this) { errorMsg -> if (errorMsg != null) onFailRequest() }
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
        val responseVideo = responseHome.post ?: responseHome.posts.firstOrNull()
        responseVideo?.let {
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
            } catch (e: Exception) { e.printStackTrace() }
        }

        txtTitle.text = video.video_title
        txtDuration.text = video.video_duration

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

        var thumbnailUrl = video.video_thumbnail
        if (video.video_type != null && video.video_type.equals("youtube", ignoreCase = true) && thumbnailUrl.isEmpty()) {
            thumbnailUrl = Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ
        } else if (thumbnailUrl.startsWith("http")) {
            thumbnailUrl = Uri.parse(thumbnailUrl).toString()
        } else {
            val base = if (sharedPref.baseUrl.endsWith("/")) sharedPref.baseUrl.substring(0, sharedPref.baseUrl.length - 1) else sharedPref.baseUrl
            thumbnailUrl = base + "/video_image/" + Uri.encode(thumbnailUrl)
        }

        Glide.with(this).load(thumbnailUrl).placeholder(R.drawable.ic_thumbnail).diskCacheStrategy(DiskCacheStrategy.ALL).into(videoThumbnail)

        Tools.displayPostDescription(this, webView, video.video_description)
        Handler(Looper.getMainLooper()).postDelayed({ lytSuggested.visibility = View.VISIBLE }, 1000)
    }

    fun addToFavorite() { video?.let { viewModel.toggleFavorite(it) } }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun loadViewed() { MyTask().execute(sharedPref.baseUrl + "/api/get_total_views/?id=" + videoId) }

    @Suppress("DEPRECATION")
    private class MyTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String { return Tools.getJSONString(params[0]) }
        override fun onPostExecute(result: String) {
            if (result.isNullOrEmpty()) return
            try {
                val json = JSONObject(result)
                val arr = json.getJSONArray("result")
                if (arr.length() > 0) { val obj = arr.getJSONObject(0) }
            } catch (e: JSONException) { e.printStackTrace() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shimmerFrameLayout.stopShimmer()
        stopSeekBarUpdates()
        youTubePlayerView.release()
    }

    override fun onPause() { super.onPause(); stopSeekBarUpdates() }
    override fun onResume() { super.onResume(); if (isPlaying) startSeekBarUpdates() }

    private fun setupLocalVideo() {
        youTubePlayerView.visibility = View.GONE
        lytThumbnailContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        lytLocalVideoContainer.visibility = View.VISIBLE

        try {
            val videoUri = if (video!!.video_url != null && video!!.video_url.startsWith("android.resource")) Uri.parse(video!!.video_url) else Uri.parse("android.resource://${packageName}/${video!!.video_id}")
            localVideoView.setVideoURI(videoUri)
            localVideoView.setOnPreparedListener { mp ->
                progressBarLocal.visibility = View.GONE
                localVideoView.start()
                isPlaying = true
                
                val videoRatio = mp.videoWidth / mp.videoHeight.toFloat()
                val screenRatio = localVideoView.width / localVideoView.height.toFloat()
                if (videoRatio >= screenRatio) {
                    localVideoView.scaleX = 1f
                    localVideoView.scaleY = screenRatio / videoRatio
                } else {
                    localVideoView.scaleX = videoRatio / screenRatio
                    localVideoView.scaleY = 1f
                }
            }
            localVideoView.setOnCompletionListener { isPlaying = false }
            localVideoView.setOnErrorListener { _, _, _ ->
                progressBarLocal.visibility = View.GONE
                Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var isVideoPlaying = isPlaying
            if (::localVideoView.isInitialized && video?.video_type == "local") isVideoPlaying = localVideoView.isPlaying
            if (isVideoPlaying) {
                val aspectRatio = Rational(16, 9)
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
            lytYoutubeCardContainer.radius = 0f
            (lytYoutubeCardContainer.layoutParams as ConstraintLayout.LayoutParams).setMargins(0, 0, 0, 0)
            (lytYoutubeCardContainer.layoutParams as ConstraintLayout.LayoutParams).height = ConstraintLayout.LayoutParams.MATCH_PARENT
        } else {
            if (!isFullScreen) {
                findViewById<View>(R.id.cardHeader).visibility = View.VISIBLE
                findViewById<View>(R.id.cardInfo).visibility = View.VISIBLE
                lytYoutubeCardContainer.radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)
                val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                val params = lytYoutubeCardContainer.layoutParams as ConstraintLayout.LayoutParams
                params.setMargins(margin, margin, margin, margin)
                params.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics).toInt()
                lytYoutubeCardContainer.layoutParams = params
            }
        }
    }

    private fun shouldShowAdsForFamilies(): Boolean = adsPref.isAdEnabled && adsPref.isBannerAdEnabled
}
