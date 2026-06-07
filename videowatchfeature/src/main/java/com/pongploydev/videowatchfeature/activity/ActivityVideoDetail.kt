package com.pongploydev.videowatchfeature.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.interactivemedia.v3.api.AdsManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.pongploydev.videowatchfeature.Constant
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetail
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import com.pongploydev.videowatchfeature.utils.Tools
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityVideoDetail : AppCompatActivity() {

    private var callbackCall: Call<CallbackVideoDetail>? = null
    private lateinit var lytMainContent: LinearLayout
    private lateinit var video: Video
    private lateinit var txtTitle: TextView
    private lateinit var txtCategory: TextView
    private lateinit var txtDuration: TextView
    private lateinit var txtTotalViews: TextView
    private lateinit var txtDateTime: TextView
    private lateinit var lytView: LinearLayout
    private lateinit var lytDate: LinearLayout
    private lateinit var videoThumbnail: ImageView
    private lateinit var webView: WebView
    //    private lateinit var dbFavorite: DbFavorite
    private lateinit var parentView: CoordinatorLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytSuggested: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPref: SharedPref
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var adsPref: AdsPref
    private lateinit var adsManager: AdsManager
    private lateinit var tools: Tools
    //    private lateinit var onBackPressedDispatcher: OnBackPressedDispatcher

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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.getTheme(this)
        setContentView(R.layout.activity_video_detail)

        video = intent.getParcelableExtra(Constant.EXTRA_OBJC)!!

        // Tools.setNavigation(this)

        tools = Tools(this)
        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary)
        swipeRefreshLayout.isRefreshing = false

        lytMainContent = findViewById(R.id.lyt_main_content)
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
        // parentView = findViewById(R.id.lyt_content)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        txtTitle = findViewById(R.id.video_title)
        txtCategory = findViewById(R.id.category_name)
        txtDuration = findViewById(R.id.video_duration)
        webView = findViewById(R.id.video_description)
        txtTotalViews = findViewById(R.id.total_views)
        txtDateTime = findViewById(R.id.date_time)
        lytView = findViewById(R.id.lyt_view_count)
        lytDate = findViewById(R.id.lyt_date)
        btnFavorite = findViewById(R.id.img_favorite)
        btnShare = findViewById(R.id.btn_share)

        lytSuggested = findViewById(R.id.lyt_suggested)

        // Initialize ExoPlayer
        initExoPlayer()

        requestAction()

        swipeRefreshLayout.setOnRefreshListener {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
            lytMainContent.visibility = View.GONE
            requestAction()
        }

        initToolbar()
        initLocalVideoViews()
        handleOnBackPressed()
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
        // Find Views
        styledPlayerView = findViewById(R.id.exoPlayerView)
        progressBar = findViewById(R.id.progressBar)
        iconPlay = findViewById(R.id.icon_play)

        // Create DataSource Factory
        val httpDataSourceFactory =
                DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .setUserAgent(userAgent)
        dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        // Create Player
        val loadControl: LoadControl = DefaultLoadControl()
        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, trackSelectionFactory)

        exoPlayer =
                ExoPlayer.Builder(this)
                        .setTrackSelector(trackSelector)
                        .setLoadControl(loadControl)
                        .build()

        styledPlayerView.player = exoPlayer
        styledPlayerView.useController = true
        styledPlayerView.requestFocus()

        // Set Listener
        exoPlayer?.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                progressBar.visibility = View.GONE
                                styledPlayerView.visibility = View.VISIBLE
                                videoThumbnail.visibility = View.GONE
                                iconPlay.visibility = View.GONE

                                // Auto play when ready
                                if (!exoPlayer!!.isPlaying) {
                                    exoPlayer!!.playWhenReady = true
                                }
                            }
                            Player.STATE_BUFFERING -> {
                                progressBar.visibility = View.VISIBLE
                            }
                            Player.STATE_ENDED -> {
                                // When finished, show thumbnail again
                                styledPlayerView.visibility = View.GONE
                                videoThumbnail.visibility = View.VISIBLE
                                iconPlay.visibility = View.VISIBLE
                            }
                            Player.STATE_IDLE -> {
                                // Do nothing
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        progressBar.visibility = View.GONE
                        Log.e("ExoPlayer", "Error: ${error.message}")

                        // If cannot play, show thumbnail and use old player
                        styledPlayerView.visibility = View.GONE
                        videoThumbnail.visibility = View.VISIBLE
                        iconPlay.visibility = View.VISIBLE

                        Toast.makeText(
                                        this@ActivityVideoDetail,
                                        "กำลังใช้โหมดเล่นแบบเดิม...",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                }
        )

        // Set custom control buttons (if any)
        val icFFwD10S = styledPlayerView.findViewById<ImageView>(R.id.ic_ffwd_10s)
        val icRewind10S = styledPlayerView.findViewById<ImageView>(R.id.ic_rew_10s)

        icFFwD10S?.setOnClickListener { exoPlayer?.seekTo(exoPlayer!!.currentPosition + 10000) }

        icRewind10S?.setOnClickListener { exoPlayer?.seekTo(exoPlayer!!.currentPosition - 10000) }
    }

    private val userAgent: String
        get() {
            val result = StringBuilder(64)
            result.append("Dalvik/")
            result.append(System.getProperty("java.vm.version"))
            result.append(" (Linux; U; Android ")

            val version = Build.VERSION.RELEASE
            result.append(if (version.isNotEmpty()) version else "1.0")

            if ("REL" == Build.VERSION.CODENAME) {
                val model = Build.MODEL
                if (model.isNotEmpty()) {
                    result.append("; ")
                    result.append(model)
                }
            }

            val id = Build.ID
            if (id.isNotEmpty()) {
                result.append(" Build/")
                result.append(id)
            }

            result.append(")")
            return result.toString()
        }

    private fun handleOnBackPressed() {
        //        onBackPressedDispatcher = onBackPressedDispatcher
        //        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        //            override fun handleOnBackPressed() {
        //                finish()
        //                adsManager.destroyBannerAd()
        //            }
        //        })
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        Handler(Looper.getMainLooper()).postDelayed({ requestPostData() }, 200)
    }

    private fun requestPostData() {
        callbackCall = RestAdapter.createAPI(sharedPref.baseUrl).getWatchVideoDetail(video.vid)
        callbackCall?.enqueue(
                object : Callback<CallbackVideoDetail> {
                    override fun onResponse(
                            call: Call<CallbackVideoDetail>,
                            response: Response<CallbackVideoDetail>
                    ) {
                        val responseHome = response.body()
                        if (responseHome == null || responseHome.status != "ok") {
                            onFailRequest()
                            return
                        }
                        displayAllData(responseHome)
                        swipeProgress(false)
                        lytMainContent.visibility = View.VISIBLE
                    }

                    override fun onFailure(call: Call<CallbackVideoDetail>, t: Throwable) {
                        if (!call.isCanceled) {
                            onFailRequest()
                        }
                    }
                }
        )
    }

    private fun onFailRequest() {
        swipeProgress(false)
        lytMainContent.visibility = View.GONE
        if (Tools.isConnect(this@ActivityVideoDetail)) {
            showFailedView(true, getString(R.string.failed_text))
        } else {
            showFailedView(true, getString(R.string.failed_text))
        }
    }

    private fun showFailedView(show: Boolean, message: String) {
        val lytFailed = findViewById<View>(R.id.lyt_failed_home)
        findViewById<TextView>(R.id.failed_message).text = message
        lytFailed.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.failed_retry).setOnClickListener { requestAction() }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipeRefreshLayout.isRefreshing = show
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
            lytMainContent.visibility = View.VISIBLE
            return
        }
        lytMainContent.visibility = View.GONE
    }

    private fun displayAllData(responseHome: CallbackVideoDetail) {
        displayData(responseHome.post)
        // displaySuggested(responseHome.suggested)
    }

    fun displayData(video: Video) {
        txtTitle.text = video.video_title
        txtDuration.text = video.video_duration

        if (AppConfig.ENABLE_VIEW_COUNT) {
            txtTotalViews.text =
                    Tools.withSuffix(video.total_views) + " " + getString(R.string.views_count)
        } else {
            lytView.visibility = View.GONE
        }

        if (AppConfig.ENABLE_DATE_DISPLAY && AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            txtDateTime.text = Tools.getTimeAgo(video.date_time)
        } else if (AppConfig.ENABLE_DATE_DISPLAY && !AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
            txtDateTime.text = Tools.getFormatedDateSimple(video.date_time)
        } else {
            lytDate.visibility = View.GONE
        }

        // Load thumbnail
        if (video.video_type != null && video.video_type == "youtube") {
            if (video.video_thumbnail.isEmpty()) {
                Glide.with(this)
                        .load(
                                Constant.YOUTUBE_IMAGE_FRONT +
                                        video.video_id +
                                        Constant.YOUTUBE_IMAGE_BACK_HQ
                        )
                        .placeholder(R.drawable.ic_thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(videoThumbnail)
            } else {
                Glide.with(this)
                        .load(sharedPref.baseUrl + "/upload/video_image/" + video.video_thumbnail)
                        .placeholder(R.drawable.ic_thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(videoThumbnail)
            }
        } else {
            Glide.with(this)
                    .load(sharedPref.baseUrl + "/upload/video_image/" + video.video_thumbnail)
                    .placeholder(R.drawable.ic_thumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(videoThumbnail)
        }

        // When clicking thumbnail, play video
        videoThumbnail.setOnClickListener {
            playVideo(video)
            showInterstitialAd()
        }

        // When clicking play icon, play video
        iconPlay.setOnClickListener {
            playVideo(video)
            showInterstitialAd()
        }

        Tools.displayPostDescription(this, webView, video.video_description)

        btnShare.setOnClickListener { Tools.shareContentDetail(this, video) }

        // addToFavorite()

        // Auto play video after loading data
        Handler(Looper.getMainLooper())
                .postDelayed(
                        {
                            if (!isPlayerInitialized && shouldAutoPlay()) {
                                playVideo(video)
                                isPlayerInitialized = true
                            }
                            lytSuggested.visibility = View.VISIBLE
                        },
                        1000
                )
    }

    private fun shouldAutoPlay(): Boolean {
        // Check settings or conditions for auto play
        return true // or according to user settings
    }

    private fun playVideo(video: Video) {
        // Local Video Check
        if (video.video_url != null &&
                        (video.video_url.startsWith("android.resource") ||
                                video.video_type == "local")
        ) {
            setupLocalVideo()
            return
        }

        if (Tools.isNetworkAvailable(this@ActivityVideoDetail)) {
            val videoUrl = getVideoUrl(video)

            if (videoUrl != null) {
                try {
                    val mediaSource = buildMediaSource(Uri.parse(videoUrl))
                    exoPlayer?.setMediaSource(mediaSource)
                    exoPlayer?.prepare()
                    exoPlayer?.playWhenReady = true

                    loadViewed()
                } catch (e: Exception) {
                    Log.e("ExoPlayer", "Error playing video: ${e.message}")
                    // If cannot play, use old method
                    openVideoPlayer(video)
                }
            } else {
                // For YouTube, open specific Activity
                openVideoPlayer(video)
            }
        } else {
            Toast.makeText(this, getString(R.string.network_required), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVideoUrl(video: Video): String? {
        return if (video.video_type != null && video.video_type == "youtube") {
            // For YouTube, still use specific Activity
            null
        } else if (video.video_type != null && video.video_type == "Upload") {
            sharedPref.baseUrl + "/upload/video/" + video.video_url
        } else {
            video.video_url
        }
    }

    private fun openVideoPlayer(video: Video) {
        //        val intent = when {
        //            video.video_type != null && video.video_type == "youtube" -> {
        //                Intent(applicationContext, ActivityYoutubePlayer::class.java).apply {
        //                    putExtra(Constant.KEY_VIDEO_ID, video.video_id)
        //                }
        //            }
        //            video.video_type != null && video.video_type == "Upload" -> {
        //                Intent(applicationContext, ActivityVideoPlayer::class.java).apply {
        //                    putExtra("url", sharedPref.baseUrl + "/upload/video/" +
        // video.video_url)
        //                }
        //            }
        //            else -> {
        //                Intent(applicationContext, ActivityVideoPlayer::class.java).apply {
        //                    putExtra("url", video.video_url)
        //                }
        //            }
        //        }
        //        startActivity(intent)
    }

    @SuppressLint("SwitchIntDef")
    private fun buildMediaSource(uri: Uri): MediaSource {
        val mediaItem = MediaItem.fromUri(uri)
        val type = Util.inferContentType(uri)

        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_HLS ->
                    HlsMediaSource.Factory(dataSourceFactory)
                            .setAllowChunklessPreparation(true)
                            .createMediaSource(mediaItem)
            C.TYPE_OTHER ->
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_RTSP -> RtspMediaSource.Factory().createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }

    private fun showInterstitialAd() {
        if (adsPref.counter >= adsPref.interstitialAdInterval) {
            adsPref.saveCounter(1)
            // adsManager.showInterstitialAd()
        } else {
            adsPref.saveCounter(adsPref.counter + 1)
        }
    }

    private fun displaySuggested(videos: List<Video>) {
        //        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_suggested)
        //        recyclerView.layoutManager = LinearLayoutManager(this@ActivityVideoDetail)
        //        val adapterSuggested = AdapterSuggested(this@ActivityVideoDetail, recyclerView,
        // videos)
        //        recyclerView.adapter = adapterSuggested
        //        recyclerView.isNestedScrollingEnabled = false

        //        adapterSuggested.setOnItemClickListener { view, obj, position ->
        //            val intent = Intent(applicationContext, ActivityVideoDetail::class.java)
        //            intent.putExtra(Constant.EXTRA_OBJC, obj)
        //            startActivity(intent)
        //                //   adsManager.destroyBannerAd()
        //        }

        // adapterSuggested.setOnItemOverflowClickListener { view, obj, position ->
        //  tools.showBottomSheetDialog(this@ActivityVideoDetail, parentView, obj)
        // }

        val txtSuggested = findViewById<TextView>(R.id.txt_suggested)
        txtSuggested.text =
                if (videos.isNotEmpty()) {
                    getString(R.string.txt_suggested)
                } else {
                    ""
                }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // setSupportActionBar(toolbar)

        toolbar.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        if (sharedPref.isDarkTheme) R.color.color_dark_primary
                        else R.color.color_light_primary
                )
        )

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = video.category_name
        }

        txtCategory.text =
                if (video.category_name.isBlank() || video.category_name.isEmpty()) {
                    "-"
                } else {
                    video.category_name
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //    fun addToFavorite() {
    //        val data = dbFavorite.getFavRow(video.vid)
    //        if (data.isEmpty()) {
    //            btnFavorite.setImageResource(R.drawable.ic_fav_outline)
    //        } else {
    //            if (data[0].vid == video.vid) {
    //                btnFavorite.setImageResource(R.drawable.ic_fav)
    //            }
    //        }
    //
    //        btnFavorite.setOnClickListener {
    //            val data1 = dbFavorite.getFavRow(video.vid)
    //            if (data1.isEmpty()) {
    //                dbFavorite.addToFavorite(
    //                    Video(
    //                        video.category_name,
    //                        video.vid,
    //                        video.video_title,
    //                        video.video_url,
    //                        video.video_id,
    //                        video.video_thumbnail,
    //                        video.video_duration,
    //                        video.video_description,
    //                        video.video_type,
    //                        video.total_views,
    //                        video.date_time
    //                    )
    //                )
    //                Snackbar.make(lytSuggested, R.string.msg_favorite_added,
    // Snackbar.LENGTH_SHORT).show()
    //                btnFavorite.setImageResource(R.drawable.ic_fav)
    //                btnFavorite.setBackgroundColor(ContextCompat.getColor(applicationContext,
    // R.color.selectedColor))
    //            } else {
    //                if (data1[0].vid == video.vid) {
    //                    dbFavorite.RemoveFav(Video(video.vid))
    //                    Snackbar.make(lytSuggested, R.string.msg_favorite_removed,
    // Snackbar.LENGTH_SHORT).show()
    //                    btnFavorite.setImageResource(R.drawable.ic_fav_outline)
    //                }
    //            }
    //        }
    //    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
            }
            else -> return super.onOptionsItemSelected(menuItem)
        }
        return true
    }

    @Suppress("DEPRECATION")
    private fun loadViewed() {
        // Using coroutines or other modern approach would be better
        // For now, keeping the async task
        val url = "${sharedPref.baseUrl}/api/get_total_views/?id=${video.vid}"
        // You should replace this with a modern approach like Coroutines
        // For now, just making the network call
        Thread {
                    try {
                        Tools.getJSONString(url)
                    } catch (e: Exception) {
                        Log.e("ActivityVideoDetail", "Error loading viewed count: ${e.message}")
                    }
                }
                .start()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        // adsManager.resumeBannerAd(adsPref.isBannerPostDetails)
        if (exoPlayer != null && isPlayerInitialized) {
            exoPlayer?.playWhenReady = true
        }

        if (isPlaying && ::localVideoView.isInitialized) {
            localVideoView.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (callbackCall?.isCanceled == false) {
            callbackCall?.cancel()
        }
        shimmerFrameLayout.stopShimmer()
        // adsManager.destroyBannerAd()

        exoPlayer?.release()
        exoPlayer = null
    }

    // ============================================
    // LOCAL VIDEO METHODS
    // ============================================

    private fun setupLocalVideo() {
        // Hide ExoPlayer UI
        styledPlayerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        videoThumbnail.visibility = View.GONE
        iconPlay.visibility = View.GONE

        // Show Local UI
        lytLocalVideoContainer.visibility = View.VISIBLE

        try {
            val videoUri = Uri.parse(video.video_url)

            localVideoView.setVideoURI(videoUri)

            localVideoView.setOnPreparedListener { mp ->
                progressBarLocal.visibility = View.GONE
                localVideoView.start()
                isPlaying = true
                updateLocalPlayPauseIcon()

                // Scale video to fit (Simple fit logic)
                /*
                   Note: VideoView default behavior usually respects aspect ratio or fits center.
                   Complex scaling logic might be needed if specific crop is desired,
                   but for now we rely on layout params (center).
                */
            }

            localVideoView.setOnCompletionListener {
                isPlaying = false
                updateLocalPlayPauseIcon()
                YoYo.with(Techniques.Tada).duration(1000).playOn(btnLocalReplay)
            }

            localVideoView.setOnErrorListener { _, _, _ ->
                progressBarLocal.visibility = View.GONE
                Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show()
                true
            }

            // Setup Controls
            btnLocalPlayPause.setOnClickListener {
                YoYo.with(Techniques.Pulse).duration(200).playOn(it)
                if (isPlaying) {
                    localVideoView.pause()
                } else {
                    localVideoView.start()
                }
                isPlaying = !isPlaying
                updateLocalPlayPauseIcon()
            }

            btnLocalReplay.setOnClickListener {
                YoYo.with(Techniques.RotateIn).duration(500).playOn(it)
                localVideoView.seekTo(0)
                localVideoView.start()
                isPlaying = true
                updateLocalPlayPauseIcon()
            }

            btnLocalBack.setOnClickListener {
                // Restore UI state if going back? or just finish
                // Usually logic mimics onBackPressed
                onBackPressedDispatcher.onBackPressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocalPlayPauseIcon() {
        if (isPlaying) {
            btnLocalPlayPause.setImageResource(R.drawable.ic_exo_pause)
        } else {
            btnLocalPlayPause.setImageResource(R.drawable.ic_exo_play)
        }
    }
    // ============================================
    // PIP Support
    // ============================================

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params =
                    android.app.PictureInPictureParams.Builder()
                            .setAspectRatio(android.util.Rational(16, 9))
                            .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
            isInPictureInPictureMode: Boolean,
            newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // General UI Hiding
            findViewById<View>(R.id.toolbar).visibility = View.GONE
            shimmerFrameLayout.visibility = View.GONE
            swipeRefreshLayout.isEnabled = false

            // LOCAL VIDEO CASE
            if (::lytLocalVideoContainer.isInitialized &&
                            lytLocalVideoContainer.visibility == View.VISIBLE
            ) {
                lytMainContent.visibility = View.GONE
                findViewById<LinearLayout>(R.id.lyt_local_controls).visibility = View.GONE
                findViewById<ImageButton>(R.id.btn_local_back).visibility = View.GONE

                // Remove margins/radius for PiP
                if (lytLocalVideoContainer.childCount > 0 &&
                                lytLocalVideoContainer.getChildAt(0) is
                                        androidx.cardview.widget.CardView
                ) {
                    val card =
                            lytLocalVideoContainer.getChildAt(0) as
                                    androidx.cardview.widget.CardView
                    card.radius = 0f
                    val cardParams = card.layoutParams as android.widget.RelativeLayout.LayoutParams
                    cardParams.setMargins(0, 0, 0, 0)
                    card.layoutParams = cardParams
                }
            } else {
                // EXO PLAYER CASE (YouTube/Upload)
                lytMainContent.visibility = View.VISIBLE

                // HIDE SIBLINGS
                findViewById<View>(R.id.lyt_head)?.visibility = View.GONE
                findViewById<View>(R.id.lyt_description_container)?.visibility = View.GONE
                findViewById<View>(R.id.lyt_suggested)?.visibility = View.GONE

                // Resize Container
                val playerParent =
                        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(
                                R.id.lyt_player_parent
                        )
                val playerContainer = findViewById<RelativeLayout>(R.id.lyt_player_container)

                if (playerParent != null) {
                    val params = playerParent.layoutParams
                    params.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    playerParent.layoutParams = params
                }

                if (playerContainer != null) {
                    val params =
                            playerContainer.layoutParams as
                                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    params.width =
                            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                                    .MATCH_PARENT
                    params.height =
                            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                                    .MATCH_PARENT
                    params.dimensionRatio = null // Clear aspect ratio constraint
                    playerContainer.layoutParams = params
                }

                styledPlayerView.useController = false
            }
        } else {
            // RESTORE UI
            findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
            swipeRefreshLayout.isEnabled = true

            if (::lytLocalVideoContainer.isInitialized &&
                            lytLocalVideoContainer.visibility == View.VISIBLE
            ) {
                lytMainContent.visibility = View.GONE // Ensure hidden for local video
                findViewById<LinearLayout>(R.id.lyt_local_controls).visibility = View.VISIBLE
                findViewById<ImageButton>(R.id.btn_local_back).visibility = View.VISIBLE

                // Restore margins/radius
                if (lytLocalVideoContainer.childCount > 0 &&
                                lytLocalVideoContainer.getChildAt(0) is
                                        androidx.cardview.widget.CardView
                ) {
                    val card =
                            lytLocalVideoContainer.getChildAt(0) as
                                    androidx.cardview.widget.CardView
                    card.radius =
                            android.util.TypedValue.applyDimension(
                                    android.util.TypedValue.COMPLEX_UNIT_DIP,
                                    16f,
                                    resources.displayMetrics
                            )
                    val marginPx =
                            android.util.TypedValue.applyDimension(
                                            android.util.TypedValue.COMPLEX_UNIT_DIP,
                                            16f,
                                            resources.displayMetrics
                                    )
                                    .toInt()
                    val cardParams = card.layoutParams as android.widget.RelativeLayout.LayoutParams
                    cardParams.setMargins(marginPx, marginPx, marginPx, marginPx)
                    card.layoutParams = cardParams
                }
            } else {
                // EXO PLAYER RESTORE
                lytMainContent.visibility = View.VISIBLE
                findViewById<View>(R.id.lyt_head)?.visibility = View.VISIBLE
                findViewById<View>(R.id.lyt_description_container)?.visibility = View.VISIBLE
                findViewById<View>(R.id.lyt_suggested)?.visibility = View.VISIBLE

                val playerParent =
                        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(
                                R.id.lyt_player_parent
                        )
                val playerContainer = findViewById<RelativeLayout>(R.id.lyt_player_container)

                if (playerParent != null) {
                    val params = playerParent.layoutParams
                    params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    playerParent.layoutParams = params
                }

                if (playerContainer != null) {
                    val params =
                            playerContainer.layoutParams as
                                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    params.width = 0
                    params.height = 0
                    params.dimensionRatio = "H,2:1.1" // Restore aspect ratio
                    playerContainer.layoutParams = params
                }

                styledPlayerView.useController = true
                styledPlayerView.showController()
            }
        }
    }
}
