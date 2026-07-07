package com.pongploydev.livefeature

import android.net.Uri
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        @Suppress("DEPRECATION")
        channel = intent.getSerializableExtra(EXTRA_CHANNEL) as? DltvChannel

        setupUI()
        setupLiveIndicator()
        initPlayer()

        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            prepareStream()
        }

        binding.btnRefreshStream.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            prepareStream()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupUI() {
        val ch = channel ?: return
        binding.tvChannelTitle.text = ch.displayName
        binding.tvInfoDescription.text = ch.description
    }

    private fun setupLiveIndicator() {
        val anim = AlphaAnimation(1.0f, 0.2f).apply {
            duration = 800
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.liveIndicatorDot.startAnimation(anim)
    }

    private fun initPlayer() {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(this).build().apply {
            playWhenReady = false
        }
        binding.playerView.player = exoPlayer

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> binding.progressBar.visibility = View.VISIBLE
                    Player.STATE_READY -> {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutError.visibility = View.GONE
                    }
                    Player.STATE_IDLE -> binding.progressBar.visibility = View.GONE
                    Player.STATE_ENDED -> binding.progressBar.visibility = View.GONE
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                binding.progressBar.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
            }
        })
    }

    private fun prepareStream() {
        val streamUrl = channel?.stream ?: return
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(mediaItem)

        exoPlayer?.let {
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = true
        }
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (exoPlayer == null) initPlayer()
        prepareStream()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.let {
            it.playWhenReady = false
            it.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
