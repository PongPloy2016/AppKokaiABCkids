package com.pongploydev.videowatchfeaturesupabase.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pongploydev.videowatchfeaturesupabase.Constant
import com.pongploydev.videowatchfeaturesupabase.R
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.utils.Tools
import java.util.Locale

/**
 * หน้าจอเฉพาะสำหรับเนื้อหาที่มาจาก RSS / XML Feeds (เช่น Google News RSS, Thai PBS Kids, YouTube Atom feeds)
 * ออกแบบมาสำหรับการเรียนรู้ของเด็กโดยเฉพาะ มีทั้งเครื่องเล่นวิดีโอ YouTube,
 * ตัวอ่านเนื้อหานิทานตัวหนังสือขนาดใหญ่ และระบบอ่านออกเสียง (Text-to-Speech) ภาษาไทย
 */
class RssFeedDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var video: Video? = null
    private var youTubePlayerView: YouTubePlayerView? = null
    private var youTubePlayer: YouTubePlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isTtsPlaying = false

    private lateinit var btnBack: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var txtFeedBadge: TextView
    private lateinit var txtSourceProvider: TextView
    private lateinit var txtFeedTitle: TextView
    private lateinit var txtAgeBadge: TextView
    private lateinit var txtViewsBadge: TextView
    private lateinit var txtFeedDate: TextView
    private lateinit var txtFeedDescription: TextView
    private lateinit var txtTtsStatus: TextView
    private lateinit var btnTtsStart: Button
    private lateinit var btnTtsStop: Button
    private lateinit var btnOpenWebSource: Button
    private lateinit var imgFeedThumbnail: ImageView
    private lateinit var imgPlayOverlay: ImageView
    private lateinit var lytThumbnailFallback: RelativeLayout
    private lateinit var iconTtsSpeaker: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss_feed_detail)

        val passedVideo: Video? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Constant.EXTRA_OBJC, Video::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Constant.EXTRA_OBJC)
        }

        if (passedVideo == null) {
            finish()
            return
        }

        this.video = passedVideo
        initViews()
        bindData(passedVideo)
        setupTts()
        setupListeners()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopTts()
                finish()
            }
        })
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnFavorite = findViewById(R.id.btn_favorite)
        btnShare = findViewById(R.id.btn_share)
        txtFeedBadge = findViewById(R.id.txt_feed_badge)
        txtSourceProvider = findViewById(R.id.txt_source_provider)
        txtFeedTitle = findViewById(R.id.txt_feed_title)
        txtAgeBadge = findViewById(R.id.txt_age_badge)
        txtViewsBadge = findViewById(R.id.txt_views_badge)
        txtFeedDate = findViewById(R.id.txt_feed_date)
        txtFeedDescription = findViewById(R.id.txt_feed_description)
        txtTtsStatus = findViewById(R.id.txt_tts_status)
        btnTtsStart = findViewById(R.id.btn_tts_start)
        btnTtsStop = findViewById(R.id.btn_tts_stop)
        btnOpenWebSource = findViewById(R.id.btn_open_web_source)
        imgFeedThumbnail = findViewById(R.id.img_feed_thumbnail)
        imgPlayOverlay = findViewById(R.id.img_play_overlay)
        lytThumbnailFallback = findViewById(R.id.lyt_thumbnail_fallback)
        iconTtsSpeaker = findViewById(R.id.icon_tts_speaker)
        youTubePlayerView = findViewById(R.id.youtube_player_view)
    }

    private fun bindData(item: Video) {
        val cleanTitle = item.video_title.orEmpty().replace(" - YouTube", "").trim()
        txtFeedTitle.text = cleanTitle

        // Badge & Provider
        val catName = item.category_name.orEmpty()
        txtFeedBadge.text = if (catName.isNotEmpty()) catName else "🌟 เนื้อหา RSS / XML"
        txtSourceProvider.text = if (item.tags.contains("thaipbs", ignoreCase = true)) {
            "Thai PBS Kids (YouTube / XML Feed)"
        } else if (item.tags.contains("feed", ignoreCase = true)) {
            "แหล่งเรียนรู้และนิทานเด็กสร้างสรรค์"
        } else {
            "ฟีดวิดีโอเด็กและครอบครัว"
        }

        // Age Rating & Date
        txtAgeBadge.text = item.age_rating.ifEmpty { "เด็กและครอบครัว" }
        txtFeedDate.text = if (item.date_time.isNotEmpty()) item.date_time else "อัปเดตสดใหม่"

        // Description / Story
        val descHtml = item.video_description.orEmpty()
        val plainDesc = Html.fromHtml(descHtml).toString().trim()
        val storyText = if (plainDesc.isNotEmpty() && plainDesc.length > 15) {
            plainDesc
        } else {
            "เรื่องราวแสนสนุกและการเรียนรู้สำหรับเด็ก: $cleanTitle ชวนน้องๆ มาร่วมฝึกอ่าน สะกดคำ และเปิดโลกจินตนาการไปพร้อมกัน เหมาะสำหรับเด็กปฐมวัยและทุกคนในครอบครัว"
        }
        txtFeedDescription.text = storyText

        // Thumbnail
        val thumbUrl = item.video_thumbnail.orEmpty()
        if (thumbUrl.isNotEmpty()) {
            Glide.with(this)
                .load(thumbUrl)
                .placeholder(R.drawable.ic_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgFeedThumbnail)
        }

        // Setup YouTube Player if ID is valid
        val vId = item.video_id.orEmpty()
        if (vId.isNotEmpty() && vId.length == 11) {
            setupYouTubePlayer(vId)
        } else {
            imgPlayOverlay.setOnClickListener {
                openSourceUrl(item.video_url)
            }
        }
    }

    private fun setupYouTubePlayer(videoId: String) {
        val playerView = youTubePlayerView ?: return
        lifecycle.addObserver(playerView)

        playerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull player: YouTubePlayer) {
                youTubePlayer = player
                imgPlayOverlay.setOnClickListener {
                    stopTts()
                    lytThumbnailFallback.visibility = View.GONE
                    playerView.visibility = View.VISIBLE
                    player.loadVideo(videoId, 0f)
                }
            }

            override fun onStateChange(@NonNull youTubePlayer: YouTubePlayer, @NonNull state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    stopTts()
                    lytThumbnailFallback.visibility = View.GONE
                    playerView.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupTts() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("th", "TH"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // ภาษาไทยอาจไม่รองรับในเครื่อง ให้ลอง Locale ปกติ
                tts?.setLanguage(Locale.getDefault())
            }
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    runOnUiThread {
                        isTtsPlaying = true
                        txtTtsStatus.text = "🔊 กำลังอ่านให้น้องๆ ฟัง..."
                        btnTtsStop.visibility = View.VISIBLE
                        btnTtsStart.text = "⏸️ อ่านอยู่..."
                        YoYo.with(Techniques.Pulse).duration(1000).repeat(YoYo.INFINITE).playOn(iconTtsSpeaker)
                    }
                }

                override fun onDone(utteranceId: String?) {
                    runOnUiThread {
                        resetTtsUi()
                    }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread {
                        resetTtsUi()
                    }
                }
            })
        }
    }

    private fun startTts() {
        if (!isTtsReady || tts == null) {
            Toast.makeText(this, "ระบบอ่านออกเสียงยังไม่พร้อม กรุณารอสักครู่", Toast.LENGTH_SHORT).show()
            return
        }

        // Pause YouTube Video when reading aloud
        youTubePlayer?.pause()

        val textToRead = "${txtFeedTitle.text}. ${txtFeedDescription.text}"
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "kids_story_tts")
        tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params, "kids_story_tts")
    }

    private fun stopTts() {
        tts?.stop()
        resetTtsUi()
    }

    private fun resetTtsUi() {
        isTtsPlaying = false
        txtTtsStatus.text = "กดปุ่มเริ่ม เพื่อให้น้องๆ ฟังเสียงอ่านสะกดคำแสนสนุก"
        btnTtsStop.visibility = View.GONE
        btnTtsStart.text = "🔊 เริ่มอ่านให้ฟัง"
        iconTtsSpeaker.clearAnimation()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            stopTts()
            finish()
        }

        btnFavorite.setOnClickListener {
            YoYo.with(Techniques.Bounce).duration(500).playOn(btnFavorite)
            video?.let { v ->
                v.isFavorite = !v.isFavorite
                if (v.isFavorite) {
                    btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                    Toast.makeText(this, "บันทึกในรายการโปรดแล้ว ❤️", Toast.LENGTH_SHORT).show()
                } else {
                    btnFavorite.setImageResource(R.drawable.ic_heart_outline)
                    Toast.makeText(this, "ลบออกจากรายการโปรดแล้ว", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnShare.setOnClickListener {
            video?.let { v ->
                YoYo.with(Techniques.Tada).duration(500).playOn(btnShare)
                Tools.shareContentDetail(this, v)
            }
        }

        btnTtsStart.setOnClickListener {
            if (isTtsPlaying) {
                stopTts()
            } else {
                YoYo.with(Techniques.BounceIn).duration(400).playOn(btnTtsStart)
                startTts()
            }
        }

        btnTtsStop.setOnClickListener {
            stopTts()
        }

        btnOpenWebSource.setOnClickListener {
            video?.let { v ->
                YoYo.with(Techniques.RubberBand).duration(500).playOn(btnOpenWebSource)
                openSourceUrl(v.video_url)
            }
        }
    }

    private fun openSourceUrl(url: String?) {
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "ไม่มีลิงก์ต้นทาง", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "ไม่สามารถเปิดลิงก์ได้", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTts()
        tts?.shutdown()
        tts = null
    }
}

