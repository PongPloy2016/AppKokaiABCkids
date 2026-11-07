package com.pongploydev.newsfeature

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.newsfeature.data.NewsArticle
import com.pongploydev.newsfeature.ui.CategoryBadge
import com.pongploydev.newsfeature.ui.NewsColors
import com.pongploydev.newsfeature.ui.NewsImage
import com.pongploydev.newsfeature.ui.NewsTheme
import com.pongploydev.newsfeature.ui.formatFullDate

/** หน้าอ่านรายละเอียดข่าวแบบเต็ม พร้อมรูปภาพและเนื้อหาข่าว */
class NewsDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_LINK = "extra_link"
        private const val EXTRA_IMAGE = "extra_image"
        private const val EXTRA_CATEGORY = "extra_category"
        private const val EXTRA_TIME = "extra_time"
        private const val EXTRA_SUMMARY = "extra_summary"
        private const val EXTRA_PARAGRAPHS = "extra_paragraphs"
        private const val EXTRA_VIDEO = "extra_video"
        private const val EXTRA_AUDIO = "extra_audio"

        fun createIntent(context: Context, article: NewsArticle): Intent =
            Intent(context, NewsDetailActivity::class.java).apply {
                putExtra(EXTRA_TITLE, article.title)
                putExtra(EXTRA_LINK, article.link)
                putExtra(EXTRA_IMAGE, article.imageUrl)
                putExtra(EXTRA_CATEGORY, article.category)
                putExtra(EXTRA_TIME, article.publishedAtMillis)
                putExtra(EXTRA_SUMMARY, article.summary)
                putStringArrayListExtra(EXTRA_PARAGRAPHS, ArrayList(article.paragraphs))
                putExtra(EXTRA_VIDEO, article.videoUrl)
                putExtra(EXTRA_AUDIO, article.audioUrl)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val link = intent.getStringExtra(EXTRA_LINK).orEmpty()
        val image = intent.getStringExtra(EXTRA_IMAGE)
        val category = intent.getStringExtra(EXTRA_CATEGORY).orEmpty()
        val time = intent.getLongExtra(EXTRA_TIME, 0L)
        val summary = intent.getStringExtra(EXTRA_SUMMARY).orEmpty()
        val paragraphs = intent.getStringArrayListExtra(EXTRA_PARAGRAPHS).orEmpty()
        val video = intent.getStringExtra(EXTRA_VIDEO)
        val audio = intent.getStringExtra(EXTRA_AUDIO)

        val body = paragraphs.ifEmpty { listOfNotNull(summary.takeIf { it.isNotBlank() }) }

        setContent {
            NewsTheme {
                NewsDetailScreen(
                    title = title,
                    category = category,
                    imageUrl = image,
                    timeMillis = time,
                    paragraphs = body,
                    hasVideo = !video.isNullOrBlank(),
                    audioUrl = audio,
                    onBack = { finish() },
                    onShare = { share(title, link) },
                    onOpenWeb = { openUrl(link) },
                    onOpenVideo = { video?.let { openUrl(it) } },
                    onOpenAudio = { audio?.let { openUrl(it) } }
                )
            }
        }
    }

    private fun share(title: String, link: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, if (link.isNotEmpty()) "$title\n$link" else title)
        }
        startActivity(Intent.createChooser(send, "แชร์ข่าว"))
    }

    private fun openUrl(url: String) {
        if (url.isBlank()) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "ไม่พบแอปสำหรับเปิดลิงก์", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun NewsDetailScreen(
    title: String,
    category: String,
    imageUrl: String?,
    timeMillis: Long,
    paragraphs: List<String>,
    hasVideo: Boolean,
    audioUrl: String?,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenWeb: () -> Unit,
    onOpenVideo: () -> Unit,
    onOpenAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NewsColors.Background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "ย้อนกลับ",
                    tint = NewsColors.TextPrimary
                )
            }
            Text(
                text = "นิทานและการเรียนรู้เด็ก 🌟",
                color = NewsColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "แชร์", tint = NewsColors.TextPrimary)
            }
            IconButton(onClick = onOpenWeb) {
                Icon(
                    Icons.Default.OpenInBrowser,
                    contentDescription = "เปิดบนเว็บ",
                    tint = NewsColors.TextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            if (!imageUrl.isNullOrBlank()) {
                NewsImage(
                    url = imageUrl,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                if (category.isNotBlank()) {
                    CategoryBadge(category = category)
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = title,
                    color = NewsColors.TextPrimary,
                    fontSize = 22.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                val date = formatFullDate(timeMillis)
                if (date.isNotEmpty()) {
                    val meta = if (category.isNotBlank()) "$category · $date" else date
                    Text(
                        text = meta,
                        color = NewsColors.TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (!audioUrl.isNullOrBlank()) {
                    AudioPlayerSection(
                        audioUrl = audioUrl,
                        onOpenExternal = onOpenAudio
                    )
                    Spacer(Modifier.height(16.dp))
                }

                paragraphs.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        color = NewsColors.TextPrimary.copy(alpha = 0.92f),
                        fontSize = 16.sp,
                        lineHeight = 27.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                if (hasVideo) {
                    Button(
                        onClick = onOpenVideo,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("ชมวิดีโอบน YouTube 🎬", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Button(
                    onClick = onOpenWeb,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NewsColors.Accent)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (!audioUrl.isNullOrBlank()) {
                            "เปิดฟังเสียงหรือดูเต็มเรื่องบนเว็บ"
                        } else if (category.isNotBlank() && category != "ข่าวเด็ก") {
                            "อ่านสาระฉบับเต็มจาก $category"
                        } else {
                            "อ่านสาระฉบับเต็มจากต้นฉบับ"
                        },
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioPlayerSection(
    audioUrl: String,
    onOpenExternal: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(audioUrl) {
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.setOnPreparedListener {
                isPrepared = true
                isBuffering = false
                mediaPlayer.start()
                isPlaying = true
            }
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                isBuffering = false
                isPlaying = false
                errorMessage = "ไม่สามารถเล่นไฟล์เสียงนี้ได้โดยตรง"
                true
            }
        } catch (e: Exception) {
            errorMessage = "เกิดข้อผิดพลาดในการโหลดเสียง"
        }

        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NewsColors.SurfaceHigh)
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            try {
                                mediaPlayer.pause()
                                isPlaying = false
                            } catch (_: Exception) {}
                        } else {
                            if (!isPrepared) {
                                isBuffering = true
                                errorMessage = null
                                try {
                                    mediaPlayer.prepareAsync()
                                } catch (e: Exception) {
                                    isBuffering = false
                                    onOpenExternal()
                                }
                            } else {
                                try {
                                    mediaPlayer.start()
                                    isPlaying = true
                                } catch (e: Exception) {
                                    onOpenExternal()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NewsColors.Accent)
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "หยุดชั่วคราว" else "เล่นเสียง",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPlaying) "กำลังเล่นนิทานเสียง... 🎧" else "แตะเพื่อฟังนิทานเสียง 🎧",
                        color = NewsColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBuffering) "กำลังโหลดข้อมูลเสียง..." else "Thai PBS Podcast",
                        color = NewsColors.TextMuted,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onOpenExternal) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "เปิดในเว็บ/แอปอื่น",
                        tint = NewsColors.TextMuted
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$errorMessage (แตะไอคอนขวาเพื่อเปิดฟังผ่านเบราว์เซอร์)",
                    color = NewsColors.Error,
                    fontSize = 11.sp
                )
            }
        }
    }
}
