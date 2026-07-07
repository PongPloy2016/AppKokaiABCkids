package com.pongploydev.livefeature

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.pongploydev.livefeature.databinding.ActivityDetailLiveBinding
import com.pongploydev.livefeature.model.DltvChannel

class DetailLiveActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHANNEL = "extra_channel"
    }

    private lateinit var binding: ActivityDetailLiveBinding
    private var exoPlayer: ExoPlayer? = null
    private var channel: DltvChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CHANNEL, DltvChannel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_CHANNEL) as? DltvChannel
        }

        setupToolbar()
        setupLiveIndicator()
        initUI()
        initPlayer()

        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            prepareStream()
        }

        binding.btnRefreshStream.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            prepareStream()
        }

        binding.btnPip.setOnClickListener {
            enterPipMode()
        }

        binding.btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        setupSwipeToPip()
    }

    private fun setupSwipeToPip() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // e1 = down event, e2 = move event
                if (e1 != null && e2.y - e1.y > 200 && Math.abs(velocityY) > 200) {
                    enterPipMode()
                    return true
                }
                return false
            }
        })

        binding.playerView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            // We return false to let the StyledPlayerView handle its own touches (like showing controls)
            false
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupLiveIndicator() {
        val anim = AlphaAnimation(1.0f, 0.2f).apply {
            duration = 800
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.liveIndicatorDot.startAnimation(anim)
    }

    private fun initUI() {
        channel?.let {
            binding.tvChannelTitle.text = it.displayName
            binding.tvGrade.text = it.grade
            binding.tvChannelNum.text = getString(R.string.channel_number, it.channelNumber)
            binding.tvInfoDescription.text = it.description

            com.bumptech.glide.Glide.with(this)
                .load(it.logo)
                .into(binding.ivChannelLogo)
        }
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> binding.progressBar.visibility = View.VISIBLE
                    Player.STATE_READY -> {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutError.visibility = View.GONE
                    }
                    else -> binding.progressBar.visibility = View.GONE
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                binding.progressBar.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
            }
        })
    }

    private fun prepareStream() {
        val url = channel?.stream ?: return
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        exoPlayer?.apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (exoPlayer == null) initPlayer()
        prepareStream()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val visibleRect = Rect()
            binding.playerView.getGlobalVisibleRect(visibleRect)
            
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .setSourceRectHint(visibleRect)
                .build()
            
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        
        val rootParams = binding.rootContentLayout.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        
        if (isInPictureInPictureMode) {
            // Remove the scrolling behavior to prevent offsets in PiP
            rootParams.behavior = null
            binding.rootContentLayout.layoutParams = rootParams

            // Hide EVERYTHING except the player card
            binding.appBar.visibility = View.GONE
            binding.scrollView.visibility = View.GONE
            
            // Force ALL containers to fill the PiP window 100%
            val cardParams = binding.playerCardView.layoutParams as android.view.ViewGroup.MarginLayoutParams
            cardParams.width = android.view.ViewGroup.MarginLayoutParams.MATCH_PARENT
            cardParams.height = android.view.ViewGroup.MarginLayoutParams.MATCH_PARENT
            cardParams.setMargins(0, 0, 0, 0)
            binding.playerCardView.layoutParams = cardParams
            binding.playerCardView.radius = 0f
            binding.playerCardView.cardElevation = 0f

            val containerParams = binding.playerContainer.layoutParams
            containerParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            containerParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            binding.playerContainer.layoutParams = containerParams

            val playerParams = binding.playerView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            playerParams.width = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
            playerParams.height = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
            playerParams.dimensionRatio = null
            binding.playerView.layoutParams = playerParams

            binding.btnPip.visibility = View.GONE
            binding.btnFullscreen.visibility = View.GONE
            binding.playerView.useController = false
            binding.playerView.resizeMode = com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
        } else {
            // Restore the scrolling behavior
            rootParams.behavior = com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior()
            binding.rootContentLayout.layoutParams = rootParams

            // Restore everything to Portrait style
            binding.appBar.visibility = View.VISIBLE
            binding.scrollView.visibility = View.VISIBLE
            
            // Restore Player Card
            val cardParams = binding.playerCardView.layoutParams as android.view.ViewGroup.MarginLayoutParams
            cardParams.width = android.view.ViewGroup.MarginLayoutParams.MATCH_PARENT
            cardParams.height = android.view.ViewGroup.MarginLayoutParams.WRAP_CONTENT
            val margin = (16 * resources.displayMetrics.density).toInt()
            cardParams.setMargins(margin, margin, margin, margin)
            binding.playerCardView.layoutParams = cardParams
            binding.playerCardView.radius = (24 * resources.displayMetrics.density)
            binding.playerCardView.cardElevation = (12 * resources.displayMetrics.density)

            // Restore Player Container
            val containerParams = binding.playerContainer.layoutParams
            containerParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            containerParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            binding.playerContainer.layoutParams = containerParams

            // Restore Player View ratio
            val playerParams = binding.playerView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            playerParams.width = 0
            playerParams.height = 0
            playerParams.dimensionRatio = "16:9"
            binding.playerView.layoutParams = playerParams

            binding.btnPip.visibility = View.VISIBLE
            binding.btnFullscreen.visibility = View.VISIBLE
            binding.playerView.useController = true
            binding.playerView.resizeMode = com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    private fun toggleFullscreen() {
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        requestedOrientation = if (isPortrait) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isPip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) isInPictureInPictureMode else false
        if (!isPip) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                enterFullscreenUI()
            } else {
                exitFullscreenUI()
            }
        }
    }

    private fun enterFullscreenUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Hide other sections
        binding.appBar.visibility = View.GONE
        binding.scrollView.visibility = View.GONE

        // Expand Player Card to fill screen
        val cardParams = binding.playerCardView.layoutParams as android.view.ViewGroup.MarginLayoutParams
        cardParams.width = android.view.ViewGroup.MarginLayoutParams.MATCH_PARENT
        cardParams.height = android.view.ViewGroup.MarginLayoutParams.MATCH_PARENT
        cardParams.setMargins(0, 0, 0, 0)
        binding.playerCardView.layoutParams = cardParams
        binding.playerCardView.radius = 0f

        // Expand Player View
        val playerParams = binding.playerView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        playerParams.width = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
        playerParams.height = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
        playerParams.dimensionRatio = null
        binding.playerView.layoutParams = playerParams
        binding.playerView.resizeMode = com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

        binding.btnFullscreen.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
    }

    private fun exitFullscreenUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())

        binding.appBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.VISIBLE

        // Restore Player Card
        val cardParams = binding.playerCardView.layoutParams as android.view.ViewGroup.MarginLayoutParams
        cardParams.width = android.view.ViewGroup.MarginLayoutParams.MATCH_PARENT
        cardParams.height = android.view.ViewGroup.MarginLayoutParams.WRAP_CONTENT
        val margin = (16 * resources.displayMetrics.density).toInt()
        cardParams.setMargins(margin, margin, margin, margin)
        binding.playerCardView.layoutParams = cardParams
        binding.playerCardView.radius = (24 * resources.displayMetrics.density)

        // Restore Player View ratio
        val playerParams = binding.playerView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        playerParams.width = 0
        playerParams.height = 0
        playerParams.dimensionRatio = "16:9"
        binding.playerView.layoutParams = playerParams
        binding.playerView.resizeMode = com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT

        binding.btnFullscreen.setImageResource(android.R.drawable.ic_menu_add)
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode) {
            // Keep playing
        } else {
            exoPlayer?.apply {
                playWhenReady = false
                stop()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
