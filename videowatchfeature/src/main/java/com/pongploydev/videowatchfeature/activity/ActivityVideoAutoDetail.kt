package com.pongploydev.videowatchfeature.activity



import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
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
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pongploydev.videowatchfeature.Constant
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.utils.Tools
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetail
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.rests.RestAdapter

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

class ActivityVideoAutoDetail : AppCompatActivity() {

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
//    private lateinit var dbFavorite: DbFavoriteite
    private lateinit var parentView: CoordinatorLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytSuggested: RelativeLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var sharedPref: SharedPref
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var adsPref: AdsPref
//    private lateinit var adsManager: AdsPrefManager
    private lateinit var tools: Tools
//    private lateinit var onBackPressedDispatcher: OnBackPressedDispatcher

    // ตัวแปรสำหรับ YouTube Player
    private lateinit var youTubePlayerView: YouTubePlayerView
    private var youTubePlayer: YouTubePlayer? = null
    private lateinit var lytThumbnailContainer: RelativeLayout
    private lateinit var progressBar: ProgressBar

    // ตัวแปรสำหรับ SeekBar และการควบคุม
    private lateinit var seekBar: YouTubePlayerSeekBar
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalDuration: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnRew10s: ImageButton
    private lateinit var btnFfwd10s: ImageButton
    private var isPlaying = false
    private val seekBarHandler = Handler()
    private var updateSeekBarRunnable: Runnable? = null
    private var videoDuration = 0f

    private val seekBarTracker = SeekBarTracker()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // Tools.getTheme(this)
        setContentView(R.layout.activity_video_auto_detail)

        video = intent.getParcelableExtra(Constant.EXTRA_OBJC)!!

        //Tools.setNavigation(this)
        tools = Tools(this)
        sharedPref = SharedPref(this)
        adsPref = AdsPref(this)
//        adsManager = AdsManager(this)
//        dbFavorite = DbFavorite(applicationContext)
//
//        adsManager.loadBannerAd(adsPref.isBannerPostDetails)
//        adsManager.loadInterstitialAd(adsPref.isInterstitialPostDetails, 1)

        val nativeAdView = findViewById<LinearLayout>(R.id.native_ad_view)
//        Tools.setNativeAdStyle(this, nativeAdView, Constant.NATIVE_AD_STYLE_POST_DETAILS)
//
//        if (nativeAdView != null) {
//            adsManager.loadNativeAd(adsPref.isNativeAdPostDetails, Constant.NATIVE_AD_STYLE_EXIT_DIALOG)
//        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary)
        swipeRefreshLayout.isRefreshing = false

        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
        lytMainContent = findViewById(R.id.lyt_main_content)
//        parentView = findViewById(R.id.lyt_content)

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

        // กำหนดค่าให้กับตัวแปรใหม่
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        lytThumbnailContainer = findViewById(R.id.lyt_thumbnail_container)
        progressBar = findViewById(R.id.progress_bar)

        // กำหนดค่าให้กับตัวแปร SeekBar และการควบคุม
        seekBar = findViewById(R.id.youtube_player_seekbar)
        txtCurrentTime = findViewById(R.id.txt_current_time)
        txtTotalDuration = findViewById(R.id.txt_duration)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnRew10s = findViewById(R.id.btn_rew_10s)
        btnFfwd10s = findViewById(R.id.btn_ffwd_10s)

//        if (AppConfig.ALLOW_APP_ACCESSED_USING_VPN) {
//            requestAction()
//        } else {
//            if (Tools.isVpnConnectionAvailable()) {
//                Tools.showWarningDialog(this, getString(R.string.vpn_detected), getString(R.string.close_vpn))
//            } else {
                requestAction()
     //       }
       // }

        swipeRefreshLayout.setOnRefreshListener {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
            lytMainContent.visibility = View.GONE
            requestAction()
        }

        //initToolbar()
        handleOnBackPressed()

        // เริ่มต้น YouTube Player
        initYouTubePlayer()

        // ตั้งค่า SeekBar และการควบคุม
        setupControls()
    }

    private fun initYouTubePlayer() {
        // ตั้งค่าเริ่มต้นให้ YouTube Player
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull player: YouTubePlayer) {
                youTubePlayer = player

                // ถ้าเป็นวิดีโอ YouTube และโหลดข้อมูลเสร็จแล้ว ให้เล่นอัตโนมัติ
                if (video != null && video.video_type != null &&
                    video.video_type.equals("youtube", ignoreCase = true) &&
                    video.video_id != null && video.video_id.isNotEmpty()
                ) {

                    // ซ่อน progress bar เมื่อพร้อม
                    progressBar.visibility = View.GONE

                    // เล่นวิดีโออัตโนมัติ
                    player.loadVideo(video.video_id, 0f)
                    isPlaying = true
                    updatePlayPauseButton()

                    // ซ่อน thumbnail container และแสดง YouTube player
                    lytThumbnailContainer.visibility = View.GONE
                    youTubePlayerView.visibility = View.VISIBLE

                    // เริ่มอัพเดท SeekBar
                    startSeekBarUpdates()
                }
            }

            override fun onStateChange(
                @NonNull youTubePlayer: YouTubePlayer,
                @NonNull state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)

                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> {
                        isPlaying = true
                        startSeekBarUpdates()
                    }
                    PlayerConstants.PlayerState.PAUSED -> {
                        isPlaying = false
                        stopSeekBarUpdates()
                    }
                    PlayerConstants.PlayerState.ENDED -> {
                        isPlaying = false
                        stopSeekBarUpdates()
                    }
                    else -> {}
                }

                updatePlayPauseButton()
            }

            override fun onCurrentSecond(@NonNull youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                updateCurrentTime(second)
            }

            override fun onVideoDuration(@NonNull youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                videoDuration = duration
                updateTotalDuration(duration)
            }

            override fun onError(
                @NonNull youTubePlayer: YouTubePlayer,
                @NonNull error: PlayerConstants.PlayerError
            ) {
                super.onError(youTubePlayer, error)
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ActivityVideoAutoDetail,
                    "เกิดข้อผิดพลาดในการเล่นวิดีโอ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupControls() {
        // ตั้งค่า SeekBar listener
        //seekBar.setYoutubePlayerSeekBarListener(seekBarTracker)

        // ตั้งค่าปุ่ม Play/Pause
        btnPlayPause.setOnClickListener {
            youTubePlayer?.let { player ->
                if (isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
                isPlaying = !isPlaying
                updatePlayPauseButton()
            }
        }

        // ตั้งค่าปุ่ม Rewind 10 วินาที
        btnRew10s.setOnClickListener {
            youTubePlayer?.let { player ->
                val currentProgress = seekBarTracker.progress
                val currentSecond = (currentProgress / 1000f) * videoDuration
                val newPosition = 0f.coerceAtLeast(currentSecond - 10)
                player.seekTo(newPosition)
            }
        }

        // ตั้งค่าปุ่ม Fast Forward 10 วินาที
        btnFfwd10s.setOnClickListener {
            youTubePlayer?.let { player ->
                val currentProgress = seekBarTracker.progress
                val currentSecond = (currentProgress / 1000f) * videoDuration
                val newPosition = videoDuration.coerceAtMost(currentSecond + 10)
                player.seekTo(newPosition)
            }
        }

        // ตั้งค่าปุ่ม Fullscreen
        val btnFullscreen = findViewById<ImageView>(R.id.btn_fullscreen)
        btnFullscreen.setOnClickListener {
            // youTubePlayer?.toggleFullscreen()
        }
    }

    private fun updatePlayPauseButton() {
        btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_exo_pause else R.drawable.ic_exo_play
        )
    }

    private fun updateCurrentTime(currentSecond: Float) {
        val time = formatTime(currentSecond.toInt())
        txtCurrentTime.text = time

        // อัพเดท SeekBar
        if (videoDuration > 0) {
            val progress = ((currentSecond / videoDuration) * 1000).toInt()
            seekBarTracker.seekTo(progress.toFloat())
        }
    }

    private fun updateTotalDuration(duration: Float) {
        val time = formatTime(duration.toInt())
        txtTotalDuration.text = time
    }

    private inner class SeekBarTracker : YouTubePlayerSeekBarListener {
        private var currentProgress = 0f

        override fun seekTo(time: Float) {
            // คำนวณ progress จาก time
            if (videoDuration > 0) {
                currentProgress = (time / videoDuration) * 1000
            }
        }

        val progress: Float
            get() = currentProgress

        val currentSecond: Float
            get() = (currentProgress / 1000f) * videoDuration
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%d:%02d", minutes, secs)
        }
    }

    private fun startSeekBarUpdates() {
        stopSeekBarUpdates() // หยุดการอัพเดทก่อน (ถ้ามี)

        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                if (youTubePlayer != null && isPlaying) {
                    // อัพเดท SeekBar ทุก 1 วินาที
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

    fun handleOnBackPressed() {
        //onBackPressedDispatcher = onBackPressedDispatcher
//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                finish()
//               // adsManager.destroyBannerAd()
//            }
//        })
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        Handler().postDelayed({ requestPostData() }, 200)
    }

    private fun requestPostData() {
        callbackCall = RestAdapter.createAPI(sharedPref.baseUrl).getWatchVideoDetail(video.vid)
        callbackCall?.enqueue(object : Callback<CallbackVideoDetail> {
            override fun onResponse(
                @NonNull call: Call<CallbackVideoDetail>,
                @NonNull response: Response<CallbackVideoDetail>
            ) {
                val responseHome = response.body()
                if (responseHome == null || responseHome.status != "ok") {
                    onFailRequest()
                    return
                }
                displayAllData(responseHome)
                swipeProgress(false)
                lytMainContent.visibility = View.VISIBLE

                // เมื่อโหลดข้อมูลเสร็จแล้ว ให้เล่นวิดีโออัตโนมัติถ้าเป็น YouTube
                if (video != null && video.video_type != null &&
                    video.video_type.equals("youtube", ignoreCase = true) &&
                    video.video_id != null && video.video_id.isNotEmpty() &&
                    youTubePlayer != null
                ) {

                    youTubePlayer?.loadVideo(video.video_id, 0f)
                    isPlaying = true
                    updatePlayPauseButton()
                    lytThumbnailContainer.visibility = View.GONE
                    youTubePlayerView.visibility = View.VISIBLE
                    startSeekBarUpdates()
                }
            }

            override fun onFailure(@NonNull call: Call<CallbackVideoDetail>, @NonNull th: Throwable) {
                if (!call.isCanceled) {
                    onFailRequest()
                }
            }
        })
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
            lytMainContent.visibility = View.GONE
        }
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
                Tools.withSuffix(video.total_views) + " " + getResources().getString(R.string.views_count)
        } else {
            lytView.visibility = View.GONE
        }

        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txtDateTime.text = if (AppConfig.DISPLAY_DATE_AS_TIME_AGO)
                Tools.getTimeAgo(video.date_time)
            else
                Tools.getFormatedDateSimple(video.date_time)
        } else {
            lytDate.visibility = View.GONE
        }

        // Load thumbnail
        var thumbnailUrl = video.video_thumbnail
        if (video.video_type != null && video.video_type.equals("youtube", ignoreCase = true) && thumbnailUrl.isEmpty()) {
            thumbnailUrl = Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ
        } else {
            thumbnailUrl = sharedPref.baseUrl + "/upload/" + thumbnailUrl
        }

        Glide.with(this)
            .load(thumbnailUrl)
            .placeholder(R.drawable.ic_thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(videoThumbnail)

        // 🎯 Click to play - สำหรับวิดีโอที่ไม่ใช่ YouTube
        videoThumbnail.setOnClickListener { view ->
            if (!video.video_type.equals("youtube", ignoreCase = true)) {
                Log.d("VideoClick", "User clicked video thumbnail")
                onVideoClick(video)
                showInterstitialAd()
            }
        }

        Tools.displayPostDescription(this, webView, video.video_description)

        btnShare.setOnClickListener { Tools.shareContentDetail(this, video) }

        addToFavorite()

        Handler().postDelayed({ lytSuggested.visibility = View.VISIBLE }, 1000)
    }

    private fun showInterstitialAd() {
        if (adsPref.counter >= adsPref.interstitialAdInterval) {
           // adsPref.saveCounter(1)
            //adsManager.showInterstitialAd()
        } else {
            adsPref.saveCounter(adsPref.counter + 1)
        }
    }

    private fun onVideoClick(video: Video) {
        Log.d("VideoClick", "Type: " + video.video_type + ", ID/URL: " + video.video_id + " / " + video.video_url)

        if (!Tools.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.network_required), Toast.LENGTH_SHORT).show()
            return
        }

        val intent: Intent

        if (video.video_type != null && video.video_type.equals("youtube", ignoreCase = true)) {
            // ไม่ต้องทำอะไรสำหรับ YouTube เพราะเล่นอัตโนมัติแล้ว
            return
        } else if (video.video_type != null && video.video_type.equals("upload", ignoreCase = true)) {
            //intent = Intent(this, ActivityVideoPlayer::class.java)
            //intent.putExtra("url", sharedPref.baseUrl + "/upload/video/" + video.video_url)
        } else {
//            intent = Intent(this, ActivityVideoPlayer::class.java)
//            intent.putExtra("url", video.video_url)
        }

        Log.d("IntentLaunch", "Launching video activity")
        //startActivity(intent)
        loadViewed()
    }

//    private fun displaySuggested(videos: List<Video>) {
//        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_suggested)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        val adapterSuggested = AdapterSuggested(this, recyclerView, videos)
//        recyclerView.adapter = adapterSuggested
//        recyclerView.isNestedScrollingEnabled = false
//
//        adapterSuggested.setOnItemClickListener { view, obj, position ->
//            val intent = Intent(applicationContext, ActivityVideoDetail::class.java)
//            intent.putExtra(Constant.EXTRA_OBJC, obj)
//            startActivity(intent)
//            adsManager.destroyBannerAd()
//        }
//
//        adapterSuggested.setOnItemOverflowClickListener { view, obj, position ->
//            tools.showBottomSheetDialog(this, parentView, obj)
//        }
//
//        val txtSuggested = findViewById<TextView>(R.id.txt_suggested)
//        txtSuggested.text = if (videos.isEmpty()) "" else getString(R.string.txt_suggested)
//    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (sharedPref.isDarkTheme) R.color.color_light_primary else R.color.color_light_primary
            )
        )

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.title = video.category_name
        }

        txtCategory.text = if (video.category_name == null || video.category_name.isEmpty()) "-" else video.category_name
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    fun addToFavorite() {
//        val data = dbFavorite.getFavRow(video.vid)
//        btnFavorite.setImageResource(if (data.isEmpty()) R.drawable.ic_fav_outline else R.drawable.ic_fav)
//
//        btnFavorite.setOnClickListener { view ->
//            val current = dbFavorite.getFavRow(video.vid)
//            if (current.isEmpty()) {
//                dbFavorite.addToFavorite(video)
//                Snackbar.make(lytSuggested, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show()
//                btnFavorite.setImageResource(R.drawable.ic_fav)
//            } else {
//                dbFavorite.RemoveFav(Video(video.vid))
//                Snackbar.make(lytSuggested, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show()
//                btnFavorite.setImageResource(R.drawable.ic_fav_outline)
//            }
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressWarnings("deprecation")
    private fun loadViewed() {
        MyTask().execute(sharedPref.baseUrl + "/api/get_total_views/?id=" + video.vid)
    }

    @SuppressWarnings("deprecation")
    private class MyTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            return Tools.getJSONString(params[0])
        }

        override fun onPostExecute(result: String) {
            if (result == null || result.isEmpty()) {
                Log.d("Viewed", "No data returned")
                return
            }
            try {
                val json = JSONObject(result)
                val arr = json.getJSONArray("result")
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    Log.d("Viewed", "Updated views")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (callbackCall != null && !callbackCall!!.isCanceled) callbackCall!!.cancel()
        shimmerFrameLayout.stopShimmer()
       // adsManager.destroyBannerAd()

        // หยุดการอัพเดท SeekBar
        stopSeekBarUpdates()

        // ลบ YouTube Player listener
        youTubePlayerView?.release()
    }

    override fun onPause() {
        super.onPause()
        // หยุดการอัพเดท SeekBar เมื่อแอปถูก pause
        stopSeekBarUpdates()
    }

    override fun onResume() {
        super.onResume()
        // เริ่มอัพเดท SeekBar ใหม่เมื่อแอป resume
        if (isPlaying) {
            startSeekBarUpdates()
        }
       // adsManager.resumeBannerAd(adsPref.isBannerPostDetails)
    }
}