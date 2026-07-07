package com.pongploydev.livefeature

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.pongploydev.livefeature.databinding.FragmentLiveBinding

class LiveFragment : Fragment() {

    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!

    private var exoPlayer: ExoPlayer? = null
    private val liveStreamUrl = "https://cdn-live.dltv.ac.th/dltv10.m3u8"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        if (exoPlayer != null) return // Already initialized

        val context = requireContext()

        // Initialize player but don't start playback yet
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
        }

        binding.playerView.player = exoPlayer

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutError.visibility = View.GONE
                    }
                    Player.STATE_IDLE -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                binding.progressBar.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
            }
        })
    }

    private fun prepareStream() {
        val context = context ?: return
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val mediaItem = MediaItem.fromUri(Uri.parse(liveStreamUrl))
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
        // Initialize player if it was released or hasn't been created yet
        if (exoPlayer == null) {
            initPlayer()
        }
        // Start playback only when fragment becomes active
        prepareStream()
    }

    override fun onPause() {
        super.onPause()
        // Stop playback and sound immediately when fragment is not active
        exoPlayer?.let {
            it.playWhenReady = false
            it.stop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
        _binding = null
    }
}
