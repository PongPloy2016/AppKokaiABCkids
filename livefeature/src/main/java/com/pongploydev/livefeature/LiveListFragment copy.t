package com.pongploydev.livefeature

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pongploydev.livefeature.data.DltvChannelProvider
import com.pongploydev.livefeature.model.DltvChannel

class LiveListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LiveListScreen(
                    onChannelClick = { channel ->
                        val intent = Intent(
                            requireContext(),
                            DetailLiveActivity::class.java
                        ).apply {
                            putExtra(DetailLiveActivity.EXTRA_CHANNEL, channel)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Color Palette (สีสดใสสำหรับเด็ก)
// ─────────────────────────────────────────────
private val KidSkyBlue   = Color(0xFF4FC3F7)
private val KidDeepBlue  = Color(0xFF0288D1)
private val KidPurple    = Color(0xFFAB47BC)
private val KidPink      = Color(0xFFF48FB1)
private val KidYellow    = Color(0xFFFFD54F)
private val KidGreen     = Color(0xFF66BB6A)
private val KidOrange    = Color(0xFFFF8A65)
private val KidRed       = Color(0xFFEF5350)
private val BgTop        = Color(0xFFE3F2FD)
private val BgBottom     = Color(0xFFF3E5F5)
private val CardBg       = Color(0xFFFFFFFF)
private val LiveRed      = Color(0xFFE53935)

/** แผนการไล่สีของการ์ดตามลำดับ เพื่อให้แต่ละช่องดูสดใสสลับกัน */
private val cardGradients = listOf(
    listOf(Color(0xFFE1F5FE), Color(0xFFF3E5F5)),
    listOf(Color(0xFFFFF9C4), Color(0xFFFFE0B2)),
    listOf(Color(0xFFE8F5E9), Color(0xFFE0F7FA)),
    listOf(Color(0xFFFCE4EC), Color(0xFFEDE7F6)),
    listOf(Color(0xFFF3E5F5), Color(0xFFE3F2FD)),
)

// ─────────────────────────────────────────────
//  Root Screen
// ─────────────────────────────────────────────
@Composable
fun LiveListScreen(onChannelClick: (DltvChannel) -> Unit) {
    val channels = DltvChannelProvider.getChannels()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(BgTop, BgBottom))
                )
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ─── Header ───
                item {
                    LiveListHeader()
                }

                // ─── Subtitle ───
                item {
                    Text(
                        text = "🎒 เลือกช่องที่อยากดูเลย!",
                        fontSize = 15.sp,
                        color = KidDeepBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // ─── Channel Cards ───
                items(channels) { channel ->
                    ChannelCard(
                        channel = channel,
                        gradientColors = cardGradients[channel.id % cardGradients.size],
                        onClick = { onChannelClick(channel) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Header
// ─────────────────────────────────────────────
@Composable
fun LiveListHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(KidDeepBlue, KidPurple))
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ─── Pulsing live dot ───
            PulsingLiveDot()

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📺 ช่อง DLTV Live",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "ดูสดตอนนี้เลย!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // ─── LIVE badge ───
            LiveBadge()
        }
    }
}

// ─────────────────────────────────────────────
//  Pulsing dot (Compose animation)
// ─────────────────────────────────────────────
@Composable
fun PulsingLiveDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "liveDot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotAlpha"
    )

    Box(
        modifier = Modifier
            .size(14.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(0xFFFF5252).copy(alpha = alpha))
    )
}

// ─────────────────────────────────────────────
//  LIVE badge
// ─────────────────────────────────────────────
@Composable
fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "liveBadge")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeAlpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LiveRed.copy(alpha = alpha))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = "● LIVE",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────
//  Channel Card
// ─────────────────────────────────────────────
@Composable
fun ChannelCard(
    channel: DltvChannel,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    // เลือก emoji ตาม level ของช่อง
    val emoji = when {
        channel.level.contains("ปฐมวัย")           -> "🌟"
        channel.level.contains("ประถม")             -> "🎨"
        channel.level.contains("มัธยมศึกษาตอนต้น") -> "📚"
        channel.level.contains("มัธยมศึกษาตอนปลาย") -> "🎓"
        else                                        -> "📺"
    }

    // สี accent ตาม level
    val accentColor = when {
        channel.level.contains("ปฐมวัย")           -> KidOrange
        channel.level.contains("ประถม")             -> KidGreen
        channel.level.contains("มัธยมศึกษาตอนต้น") -> KidSkyBlue
        channel.level.contains("มัธยมศึกษาตอนปลาย") -> KidPurple
        else                                        -> KidDeepBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ─── Logo ───
                ChannelLogo(
                    logoUrl = channel.logo,
                    accentColor = accentColor,
                    placeholder = emoji
                )

                Spacer(modifier = Modifier.width(14.dp))

                // ─── Info ───
                Column(modifier = Modifier.weight(1f)) {
                    // ช่องหมายเลข + ชื่อ
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = emoji,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = channel.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // grade badge
                    GradeBadge(grade = channel.grade, color = accentColor)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Short description
                    Text(
                        text = channel.shortDescription,
                        fontSize = 12.sp,
                        color = Color(0xFF555555),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ─── Mini live indicator ───
                MiniLiveIndicator()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Channel Logo (Coil async image)
// ─────────────────────────────────────────────
@Composable
fun ChannelLogo(logoUrl: String, accentColor: Color, placeholder: String) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.15f))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(logoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

// ─────────────────────────────────────────────
//  Grade Badge (ป.1, อนุบาล 1, ม.1 ฯลฯ)
// ─────────────────────────────────────────────
@Composable
fun GradeBadge(grade: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = grade,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ─────────────────────────────────────────────
//  Mini Live Indicator column
// ─────────────────────────────────────────────
@Composable
fun MiniLiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "miniLive")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "miniDotScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(LiveRed)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "LIVE",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = LiveRed
        )
    }
}
