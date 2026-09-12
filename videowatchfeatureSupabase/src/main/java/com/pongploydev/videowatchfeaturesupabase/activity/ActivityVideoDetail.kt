package com.pongploydev.videowatchfeaturesupabase.activity

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.util.Rational
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.activity.viewModels
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
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.pongploydev.videowatchfeaturesupabase.Constant
import com.pongploydev.videowatchfeaturesupabase.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.videowatchfeaturesupabase.adapter.AdapterVideo
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetail
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.AdsPref
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.SharedPref
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.utils.Tools
import com.pongploydev.videowatchfeaturesupabase.viewmodel.VideoDetailViewModel
import retrofit2.Call
import java.util.Locale

class ActivityVideoDetail : AppCompatActivity(), TextToSpeech.OnInitListener {

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
    private lateinit var btnBack: ImageButton
    private lateinit var btnTtsVoice: ImageButton
    private lateinit var chipReadAloud: View
    private lateinit var btnReadDescVoice: ImageView
    private lateinit var iconTitleStar: ImageView
    private lateinit var adsPref: AdsPref
    private lateinit var tools: Tools

    private lateinit var rvSuggested: RecyclerView
    private var adapterSuggested: AdapterVideo? = null

    // ExoPlayer variables
    private var exoPlayer: ExoPlayer? = null
    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var playerWebView: WebView
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

    // TTS
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_video_detail)

        videoId = intent.getStringExtra(Constant.KEY_VID) ?: intent.getIntExtra(Constant.KEY_VID, -1).toString()

        tools = Tools(this)
        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)

        textToSpeech = TextToSpeech(this, this)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.kids_accent, R.color.kids_pink, R.color.kids_primary)
        swipeRefreshLayout.isRefreshing = false

        lytMainContent = findViewById(R.id.lyt_main_content)
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)

        videoThumbnail = findViewById(R.id.video_thumbnail)
        txtTitle = findViewById(R.id.video_title)
        iconTitleStar = findViewById(R.id.icon_title_star)
        videoCategoryBadge = findViewById(R.id.video_category_badge)
        webView = findViewById(R.id.video_description)
        txtTotalViews = findViewById(R.id.total_views)
        txtDateTime = findViewById(R.id.date_time)
        lytView = findViewById(R.id.lyt_view_count)
        lytDate = findViewById(R.id.lyt_date)
        btnFavorite = findViewById(R.id.img_favorite)
        btnShare = findViewById(R.id.btn_share)
        btnBack = findViewById(R.id.btn_back)
        btnTtsVoice = findViewById(R.id.btn_tts_voice)
        chipReadAloud = findViewById(R.id.chip_read_aloud)
        btnReadDescVoice = findViewById(R.id.btn_read_desc_voice)

        lytSuggested = findViewById(R.id.lyt_suggested)
        rvSuggested = findViewById(R.id.rv_suggested)
        cardPlayer = findViewById(R.id.cardPlayer)

        // Pulse Indicator
        liveIndicatorDot = findViewById(R.id.liveIndicatorDot)
        startPulseAnimation()
        setupSuggestedRecyclerView()

        initExoPlayer()
        initLocalVideoViews()
        setupViewModelObservers()
        setupFavorite()
        setupListeners()
        setupKidsReactions()

        requestAction()
        viewModel.loadRandomVideos()

        swipeRefreshLayout.setOnRefreshListener {
            requestAction()
            viewModel.loadRandomVideos()
        }
    }

    private fun startPulseAnimation() {
        YoYo.with(Techniques.Pulse)
            .duration(1000)
            .repeat(YoYo.INFINITE)
            .playOn(liveIndicatorDot)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            YoYo.with(Techniques.Bounce).duration(300).playOn(btnBack)
            finish()
        }
        btnShare.setOnClickListener {
            YoYo.with(Techniques.Tada).duration(500).playOn(btnShare)
            video?.let { v -> Tools.shareContentDetail(this, v) }
        }
        btnTtsVoice.setOnClickListener {
            YoYo.with(Techniques.RubberBand).duration(500).playOn(btnTtsVoice)
            speakVideoTitle()
        }
        chipReadAloud.setOnClickListener {
            YoYo.with(Techniques.Bounce).duration(500).playOn(chipReadAloud)
            speakVideoTitle()
        }
        btnReadDescVoice.setOnClickListener {
            YoYo.with(Techniques.Tada).duration(500).playOn(btnReadDescVoice)
            speakVideoDescription()
        }
        iconPlay.setOnClickListener {
            YoYo.with(Techniques.Pulse).duration(400).playOn(iconPlay)
            video?.let { v -> playVideo(v) }
        }
        findViewById<Button>(R.id.btnRefreshStream).setOnClickListener {
            YoYo.with(Techniques.RotateIn).duration(500).playOn(it)
            requestAction()
            Toast.makeText(this, "🔄 กำลังรีเฟรชสัญญาณ...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupKidsReactions() {
        findViewById<View>(R.id.btn_react_love)?.setOnClickListener {
            YoYo.with(Techniques.Bounce).duration(600).playOn(it)
            showReactionFeedback("💖 รักเลย! ขอบคุณสำหรับหัวใจนะจ๊ะคนเก่ง ✨")
        }
        findViewById<View>(R.id.btn_react_clap)?.setOnClickListener {
            YoYo.with(Techniques.Tada).duration(600).playOn(it)
            showReactionFeedback("👏 แปะๆๆ เก่งมากเลย! ปรบมือรัวๆ จ้า 🎉")
        }
        findViewById<View>(R.id.btn_react_fun)?.setOnClickListener {
            YoYo.with(Techniques.RubberBand).duration(600).playOn(it)
            showReactionFeedback("😆 สนุกสุดๆ เรียนรู้ไปด้วยกันนะจ๊ะ 🎈")
        }
        findViewById<View>(R.id.btn_react_star)?.setOnClickListener {
            YoYo.with(Techniques.StandUp).duration(600).playOn(it)
            showReactionFeedback("⭐ ยอดเยี่ยมมาก! รับดาวคนเก่งไปเลย 🌟")
        }
        findViewById<View>(R.id.btn_react_rocket)?.setOnClickListener {
            YoYo.with(Techniques.Wobble).duration(600).playOn(it)
            showReactionFeedback("🚀 ว้าวสุดยอด! ออกเดินทางท่องโลกความรู้กันเลย!")
        }
    }

    private fun showReactionFeedback(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun speakText(text: String, utteranceId: String) {
        if (isTtsReady && text.isNotBlank()) {
            val plainText = Html.fromHtml(text).toString()
                .replace("\n", " ")
                .replace("(?i)\\[video\\]".toRegex(), "")
                .trim()
            textToSpeech?.speak(plainText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    private fun speakVideoTitle() {
        video?.let { v ->
            val title = (v.video_title ?: "").replace("(?i)\\[video\\]".toRegex(), "").trim()
            val cat = v.category_name ?: ""
            val toSpeak = if (cat.isNotBlank()) "หมวด $cat เรื่อง $title" else title
            speakText(toSpeak, "video_title_tts")
            Toast.makeText(this, "🔊 กำลังอ่านชื่อเรื่องให้ฟังนะจ๊ะ...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakVideoDescription() {
        video?.let { v ->
            val desc = v.video_description ?: ""
            if (desc.isNotBlank()) {
                speakText(desc, "video_desc_tts")
                Toast.makeText(this, "🔊 กำลังอ่านสาระน่ารู้ให้ฟังนะจ๊ะ...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "ไม่มีข้อความสาระน่ารู้จ้า", Toast.LENGTH_SHORT).show()
            }
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

    private val browserUserAgent: String
        get() = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private fun initExoPlayer() {
        styledPlayerView = findViewById(R.id.exoPlayerView)
        playerWebView = findViewById(R.id.player_web_view)
        progressBar = findViewById(R.id.progressBar)
        iconPlay = findViewById(R.id.icon_play)

        val defaultHeaders = mapOf(
            "User-Agent" to browserUserAgent,
            "Referer" to "https://freeterabox.com/",
            "Accept" to "*/*"
        )
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(browserUserAgent)
            .setDefaultRequestProperties(defaultHeaders)

        dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(DefaultTrackSelector(this, AdaptiveTrackSelection.Factory()))
            .setLoadControl(DefaultLoadControl())
            .build()
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
                Log.e("ActivityVideoDetail", "ExoPlayer playback error: ${error.message}", error)
                val currentUrl = video?.video_url
                if (!currentUrl.isNullOrEmpty()) {
                    styledPlayerView.visibility = View.GONE
                    findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
                    setupPlayerWebView(currentUrl)
                } else {
                    progressBar.visibility = View.GONE
                    styledPlayerView.visibility = View.GONE
                    findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.VISIBLE
                    Toast.makeText(this@ActivityVideoDetail, "ไม่สามารถเล่นไฟล์วิดีโอนี้ได้", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private val userAgent: String get() = Util.getUserAgent(this, "PaintApp")

    private fun setupViewModelObservers() {
        viewModel.videoDetail.observe(this) { responseHome -> responseHome?.let { displayAllData(it) } }
        viewModel.randomVideos.observe(this) { videos ->
            if (videos.isNotEmpty()) {
                lytSuggested.visibility = View.VISIBLE
                adapterSuggested?.updateData(videos)
            } else {
                lytSuggested.visibility = View.GONE
            }
        }
        viewModel.isLoading.observe(this) { isLoading -> swipeProgress(isLoading) }
        viewModel.error.observe(this) { errorMsg -> if (errorMsg != null) onFailRequest() }
    }

    private fun setupSuggestedRecyclerView() {
        rvSuggested.layoutManager = LinearLayoutManager(this)
        adapterSuggested = AdapterVideo(this, rvSuggested, mutableListOf())
        rvSuggested.adapter = adapterSuggested

        adapterSuggested?.setOnItemClickListener { view, video, position ->
            val intent = Intent(this, if (video.video_type?.lowercase() == "youtube") ActivityVideoAutoDetail::class.java else ActivityVideoDetail::class.java)
            intent.putExtra(Constant.KEY_VID, video.vid)
            startActivity(intent)
            finish()
        }
    }

    private fun setupFavorite() {
        btnFavorite.setOnClickListener {
            YoYo.with(Techniques.RubberBand).duration(500).playOn(btnFavorite)
            video?.let { viewModel.toggleFavorite(it) }
        }
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
            videoCategoryBadge.text = "🌟 " + video.category_name
            try {
                video.getCategoryColor()?.let { colorStr ->
                    videoCategoryBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStr))
                }
            } catch (e: Exception) {}
        }

        // ล้างคำว่า [video] ออกเพื่อให้หัวเรื่องดูสะอาด สวยงาม เหมาะกับเด็ก
        val rawTitle = video.video_title ?: ""
        val cleanTitle = rawTitle.replace("(?i)\\[video\\]".toRegex(), "").trim()
        txtTitle.text = cleanTitle.ifEmpty { rawTitle }

        // แอนิเมชันรูปดาวข้างชื่อเรื่อง
        YoYo.with(Techniques.Bounce).duration(800).playOn(iconTitleStar)

        // บันทึกคะแนนและดาวสะสมจากการชมวิดีโอเข้า Supabase
        val supabaseUser = SupabaseAuthManager.getInstance(this).getStoredUser()
        val userId = supabaseUser?.id ?: "guest_user"
        if (userId.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val dataManager = SupabaseDataManager.getInstance(this@ActivityVideoDetail)
                val itemKey = if (!video.video_id.isNullOrEmpty()) video.video_id else video.id.toString()
                dataManager.saveLearningProgress(
                    userId = userId,
                    categoryKey = "video_watch",
                    itemKey = itemKey,
                    score = 100
                )
                dataManager.fetchUserProfile(userId).onSuccess { prof ->
                    dataManager.addStarsAndLevel(userId, prof.totalStars, prof.currentLevel, 1)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ActivityVideoDetail, "บันทึกคะแนนสำเร็จแล้ว! ⭐ +1 🎬 (จากการรับชมวิดีโอ)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // อ่านชื่อวิดีโออัตโนมัติ (Auto Read Title)
        if (isTtsReady) {
            textToSpeech?.speak(cleanTitle.ifEmpty { rawTitle }, TextToSpeech.QUEUE_FLUSH, null, "video_title_tts")
        }

        if (AppConfig.ENABLE_VIEW_COUNT) {
            txtTotalViews.text = Tools.withSuffix(video.total_views) + " " + getString(R.string.views_count)
            lytView.visibility = View.VISIBLE
        } else {
            lytView.visibility = View.GONE
        }

        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txtDateTime.text = if (AppConfig.DISPLAY_DATE_AS_TIME_AGO) Tools.getTimeAgo(video.date_time) else Tools.getFormatedDateSimple(video.date_time)
            lytDate.visibility = View.VISIBLE
        } else {
            lytDate.visibility = View.GONE
        }

        val thumbnailUrl = if (video.video_type != null && video.video_type.lowercase() == "youtube" && video.video_thumbnail.isNullOrEmpty()) {
            Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ
        } else if (!video.video_thumbnail.isNullOrEmpty() && video.video_thumbnail.startsWith("http")) {
            Uri.parse(video.video_thumbnail).toString()
        } else if (!video.video_thumbnail.isNullOrEmpty()) {
            val base = if (sharedPref.baseUrl.endsWith("/")) sharedPref.baseUrl.substring(0, sharedPref.baseUrl.length - 1) else sharedPref.baseUrl
            base + "/video_image/" + Uri.encode(video.video_thumbnail)
        } else {
            ""
        }

        Glide.with(this).load(thumbnailUrl).placeholder(R.drawable.ic_thumbnail).diskCacheStrategy(DiskCacheStrategy.ALL).into(videoThumbnail)

        // ล้างตัวอักษร \r\n ที่แสดงเป็นข้อความดิบออกเพื่อให้แสดงผลสวยงาม
        val cleanDescription = video.video_description
            ?.replace("\\r\\n", "<br>")
            ?.replace("\r\n", "<br>")
            ?.replace("\n", "<br>") ?: ""
        Tools.displayPostDescription(this, webView, cleanDescription)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isPlayerInitialized) { playVideo(video); isPlayerInitialized = true }
            if (viewModel.randomVideos.value?.isNotEmpty() == true) {
                lytSuggested.visibility = View.VISIBLE
            }
        }, 1000)
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

    private fun playVideo(video: Video) {
        if (video.video_type != null && (video.video_type.equals("local", ignoreCase = true) || (video.video_url != null && video.video_url.startsWith("android.resource")))) {
            setupLocalVideo()
            return
        }

        var videoUrl = if (video.video_type != null && video.video_type.equals("Upload", ignoreCase = true)) sharedPref.baseUrl + "/upload/video/" + video.video_url else video.video_url

        if (!videoUrl.isNullOrEmpty() && (video.video_type == null || !video.video_type.equals("youtube", ignoreCase = true))) {

            // ปรับปรุง URL สำหรับ Google Drive ให้เป็น /preview และเปิด Autoplay
            if (videoUrl.contains("drive.google.com")) {
                if (videoUrl.contains("/view")) {
                    videoUrl = videoUrl.replace("/view", "/preview")
                }
                if (!videoUrl.contains("autoplay=1")) {
                    videoUrl = if (videoUrl.contains("?")) "$videoUrl&autoplay=1" else "$videoUrl?autoplay=1"
                }
            }

            if (isMediaUrl(videoUrl)) {
                try {
                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))
                    exoPlayer?.setMediaSource(mediaSource)
                    exoPlayer?.prepare()
                    exoPlayer?.playWhenReady = true
                    findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
                } catch (e: Exception) {
                    Log.e("ActivityVideoDetail", "Error playing ProgressiveMediaSource: ${e.message}")
                    styledPlayerView.visibility = View.GONE
                    findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
                    setupPlayerWebView(videoUrl)
                }
            } else {
                // TeraBox Web Share Link (e.g. 1024terabox.com/s/...)
                styledPlayerView.visibility = View.GONE
                findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
                setupPlayerWebView(videoUrl)
            }
        }
    }

    override fun onPause() { super.onPause(); exoPlayer?.playWhenReady = false }
    override fun onResume() { super.onResume(); if (exoPlayer != null && isPlayerInitialized) exoPlayer?.playWhenReady = true }
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        exoPlayer?.release()
        if (::playerWebView.isInitialized) {
            playerWebView.stopLoading()
            playerWebView.destroy()
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.ERROR) {
            textToSpeech?.language = Locale("th", "TH")
            textToSpeech?.setSpeechRate(0.75f)
            isTtsReady = true

            // ถ้าโหลดวิดีโอเสร็จก่อน TTS พร้อม ให้สั่งอ่านที่นี่
            video?.let { v ->
                val title = (v.video_title ?: "").replace("(?i)\\[video\\]".toRegex(), "").trim()
                if (title.isNotBlank()) {
                    textToSpeech?.speak(title, TextToSpeech.QUEUE_FLUSH, null, "video_title_tts")
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupPlayerWebView(url: String) {
        playerWebView.visibility = View.VISIBLE
        val settings = playerWebView.settings
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
        settings.userAgentString = browserUserAgent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(playerWebView, true)
        }

        playerWebView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                findViewById<View>(R.id.lyt_thumbnail_overlay).visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE

                // ฉีด JavaScript เพื่อคลิกปุ่มเล่นอัตโนมัติ (แก้ไขให้ปลอดภัยขึ้น ไม่ไปกดปุ่มลบ)
                val js = """
                    (function() {
                        if (window.autoPlayTimer) clearInterval(window.autoPlayTimer);
                        
                        function forceClick(el) {
                            if (!el) return;
                            var text = (el.innerText || el.getAttribute('aria-label') || '').toLowerCase();
                            var dangerWords = ['ลบ', 'นำออก', 'delete', 'remove', 'trash', 'ถังขยะ'];
                            for (var word of dangerWords) {
                                if (text.indexOf(word) > -1) return;
                            }

                            var events = ['mousedown', 'mouseup', 'click', 'touchstart', 'touchend'];
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
                                if (vids[i].paused) {
                                    try { vids[i].click(); } catch(e) {}
                                    vids[i].play().catch(function(e){});
                                }
                                vids[i].muted = false;
                            }
                            
                            var playSelectors = [
                                '.ytp-large-play-button', '.play-button', 
                                '[aria-label*="Play"]', '[aria-label*="เล่น"]',
                                '.ytp-cued-thumbnail-overlay', '.goog-inline-block', 'center svg',
                                '.ytp-play-button', '.drive-viewer-video-player',
                                '.ytp-cued-thumbnail-overlay-image'
                            ];
                            playSelectors.forEach(function(s) {
                                var elms = document.querySelectorAll(s);
                                for (var j = 0; j < elms.length; j++) {
                                    forceClick(elms[j]);
                                    var svg = elms[j].querySelector('svg');
                                    if (svg) forceClick(svg);
                                }
                            });
                            
                            // คลิกตรงกลางจอหลายๆ ครั้งเพื่อล้าง Overlay (สำคัญมาก!)
                            var midX = window.innerWidth / 2;
                            var midY = window.innerHeight / 2;
                            var points = [[midX, midY], [midX, midY - 30], [midX, midY + 30], [midX - 30, midY], [midX + 30, midY]];
                            points.forEach(function(p) {
                                var el = document.elementFromPoint(p[0], p[1]);
                                if (el) forceClick(el);
                            });

                            // พยายามซ่อนองค์ประกอบที่อาจทำให้หน้าจอมืด
                            var overlays = document.querySelectorAll('.ytp-cued-thumbnail-overlay, .ytp-upnext, .ytp-pause-overlay');
                            overlays.forEach(function(o) { o.style.display = 'none'; });
                        }

                        var count = 0;
                        window.autoPlayTimer = setInterval(function() {
                            tryPlay();
                            count++;
                            if (count > 30) clearInterval(window.autoPlayTimer);
                        }, 500);
                    })();
                """.trimIndent()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    view?.evaluateJavascript(js, null)
                } else {
                    view?.loadUrl("javascript:$js")
                }
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (isMediaUrl(url)) {
                        playerWebView.visibility = View.GONE
                        styledPlayerView.visibility = View.VISIBLE
                        try {
                            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
                            exoPlayer?.setMediaSource(mediaSource)
                            exoPlayer?.prepare()
                            exoPlayer?.playWhenReady = true
                            return true
                        } catch (e: Exception) {
                            Log.e("ActivityVideoDetail", "Error switching to ExoPlayer: ${e.message}")
                        }
                    }
                    view?.loadUrl(url)
                }
                return true
            }
        }

        playerWebView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }

        val extraHeaders = HashMap<String, String>()
        if (url.contains("terabox") || url.contains("freeterabox")) {
            extraHeaders["Referer"] = "https://freeterabox.com/"
        }
        playerWebView.loadUrl(url, extraHeaders)
    }

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
            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
            val params = cardPlayer.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(margin, 4, margin, 8)
            cardPlayer.layoutParams = params
        }
    }
}
