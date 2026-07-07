package com.pongploydev.livefeature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pongploydev.livefeature.data.DltvChannelProvider
import com.pongploydev.livefeature.model.DltvChannel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────
//  SharedPreferences helper for favorites
// ─────────────────────────────────────────────
private const val PREFS_NAME = "dltv_favorites"
private const val KEY_FAVORITES = "favorite_ids"

private fun loadFavoriteIds(context: Context): Set<Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val raw = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    return raw.mapNotNull { it.toIntOrNull() }.toSet()
}

private fun saveFavoriteIds(context: Context, ids: Set<Int>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putStringSet(KEY_FAVORITES, ids.map { it.toString() }.toSet()).apply()
}

// ─────────────────────────────────────────────
//  Fragment
// ─────────────────────────────────────────────
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
// ─────────────────────────────────────────────
//  Animated Kid Background (Canvas)
// ─────────────────────────────────────────────

// ข้อมูลฟองสบู่แต่ละลูก
private data class Bubble(
    val xFrac: Float,   // ตำแหน่ง X เป็น % ของหน้าจอ
    val yFrac: Float,   // ตำแหน่ง Y เริ่มต้น เป็น % ของหน้าจอ
    val radius: Float,  // รัศมี dp
    val color: Color,
    val speedFrac: Float, // ความเร็วลอย (1 = เร็ว)
    val phaseOffset: Float // phase offset ของการแกว่ง
)

// ข้อมูลดาว
private data class StarParticle(
    val xFrac: Float,
    val yFrac: Float,
    val size: Float,
    val color: Color,
    val blinkPhase: Float
)

@Composable
fun KidBackground(modifier: Modifier = Modifier) {

    // สร้างฟองสบู่ 18 ลูก แบบ deterministic (ไม่ใช้ Random เพื่อไม่ให้ recompose)
    val bubbles = remember {
        val cols = listOf(
            Color(0x55FFD740), Color(0x5540C4FF), Color(0x55FF6D6D),
            Color(0x5569F0AE), Color(0x55FF80AB), Color(0x55B388FF),
            Color(0x55FFB74D), Color(0x554FC3F7), Color(0x55A5D6A7)
        )
        List(18) { i ->
            Bubble(
                xFrac   = ((i * 37 + 11) % 97) / 100f,
                yFrac   = ((i * 53 + 7)  % 100) / 100f,
                radius  = 14f + (i % 5) * 8f,
                color   = cols[i % cols.size],
                speedFrac = 0.3f + (i % 4) * 0.18f,
                phaseOffset = (i * 41 % 100) / 100f
            )
        }
    }

    // ดาว 12 ดวง
    val stars = remember {
        val starCols = listOf(
            Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFCC02),
            Color(0xFFFFAB40), Color(0xFFFFFFFF)
        )
        List(12) { i ->
            StarParticle(
                xFrac      = ((i * 61 + 19) % 95) / 100f,
                yFrac      = ((i * 43 + 13) % 90) / 100f,
                size       = 5f + (i % 4) * 4f,
                color      = starCols[i % starCols.size],
                blinkPhase = (i * 29 % 100) / 100f
            )
        }
    }

    // InfiniteTransition หลัก (tick 0..1 วนซ้ำ)
    val inf = rememberInfiniteTransition(label = "bg")

    // tick ลอย 0f -> 1f ใน 8 วินาที
    val floatTick by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "floatTick"
    )
    // tick กระพริบดาว
    val blinkTick by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "blinkTick"
    )
    // tick แกว่ง
    val swayTick by inf.animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "swayTick"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // ── พื้นหลัง gradient อ่อนๆ ──
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF8E1),   // เหลืองครีมด้านบน
                    Color(0xFFE1F5FE),   // ฟ้าอ่อนตรงกลาง
                    Color(0xFFFCE4EC)    // ชมพูอ่อนด้านล่าง
                )
            )
        )

        // ── เส้นคลื่นขอบล่าง (เมฆ) ──
        val cloudPath = Path().apply {
            moveTo(0f, h * 0.82f)
            cubicTo(
                w * 0.15f, h * 0.76f,
                w * 0.3f,  h * 0.86f,
                w * 0.5f,  h * 0.8f
            )
            cubicTo(
                w * 0.7f,  h * 0.74f,
                w * 0.85f, h * 0.84f,
                w,         h * 0.79f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(cloudPath, color = Color(0x22FFFFFF))

        // ── ฟองสบู่ลอยขึ้น ──
        for (bubble in bubbles) {
            // คำนวณ Y ที่เลื่อนขึ้น (wrap จาก 0-1)
            val progress = (floatTick + bubble.phaseOffset) % 1f
            // ลอยจากล่างขึ้นบน: Y เริ่มที่ yFrac แล้วลอยขึ้น
            val rawY = bubble.yFrac - progress * bubble.speedFrac
            val yNorm = ((rawY % 1f) + 1f) % 1f // wrap ไม่ให้หลุดจอ

            // แกว่งซ้ายขวา
            val swayX = sin(swayTick + bubble.phaseOffset * 6.28f) * 0.025f
            val cx = (bubble.xFrac + swayX).coerceIn(0.04f, 0.96f) * w
            val cy = yNorm * h

            // alpha fade ใกล้ขอบบน
            val fadeAlpha = if (yNorm < 0.08f) (yNorm / 0.08f) else 1f

            // วาดฟอง
            drawCircle(
                color   = bubble.color.copy(alpha = bubble.color.alpha * fadeAlpha),
                radius  = bubble.radius * density,
                center  = Offset(cx, cy)
            )
            // ไฮไลท์ขอบฟอง
            drawCircle(
                color  = Color.White.copy(alpha = 0.25f * fadeAlpha),
                radius = bubble.radius * density,
                center = Offset(cx, cy),
                style  = Stroke(width = 1.5f * density)
            )
            // แสงวาว (highlight จุดเล็กๆ)
            drawCircle(
                color  = Color.White.copy(alpha = 0.55f * fadeAlpha),
                radius = bubble.radius * density * 0.22f,
                center = Offset(cx - bubble.radius * density * 0.3f,
                                cy - bubble.radius * density * 0.3f)
            )
        }

        // ── ดาวกระพริบ ──
        for (star in stars) {
            val blink = sin((blinkTick + star.blinkPhase) * 2f * PI.toFloat())
            val alpha = (blink * 0.4f + 0.6f).coerceIn(0.1f, 1f)
            val cx = star.xFrac * w
            val cy = star.yFrac * h * 0.75f  // ดาวอยู่ครึ่งบน
            val r  = star.size * density

            // วาดดาว 4 แฉก
            for (arm in 0 until 4) {
                val angle = arm * (PI / 2).toFloat()
                drawLine(
                    color = star.color.copy(alpha = alpha),
                    start = Offset(cx + cos(angle) * r * 0.25f, cy + sin(angle) * r * 0.25f),
                    end   = Offset(cx + cos(angle) * r, cy + sin(angle) * r),
                    strokeWidth = 2f * density,
                    cap   = StrokeCap.Round
                )
            }
            // จุดกลาง
            drawCircle(
                color  = star.color.copy(alpha = alpha),
                radius = r * 0.28f,
                center = Offset(cx, cy)
            )
        }
    }
}

private val KidSkyBlue    = Color(0xFF4FC3F7)
private val KidDeepBlue   = Color(0xFF0288D1)
private val KidPurple     = Color(0xFFAB47BC)
private val KidYellow     = Color(0xFFFFD54F)
private val KidGreen      = Color(0xFF66BB6A)
private val KidOrange     = Color(0xFFFF8A65)
private val BgTop         = Color(0xFFE3F2FD)
private val BgBottom      = Color(0xFFF3E5F5)
private val CardBg        = Color(0xFFFFFFFF)
private val LiveRed       = Color(0xFFE53935)
private val FavStar       = Color(0xFFFFC107)
private val FavStarEmpty  = Color(0xFFBDBDBD)

private val cardGradients = listOf(
    listOf(Color(0xFFE1F5FE), Color(0xFFF3E5F5)),
    listOf(Color(0xFFFFF9C4), Color(0xFFFFE0B2)),
    listOf(Color(0xFFE8F5E9), Color(0xFFE0F7FA)),
    listOf(Color(0xFFFCE4EC), Color(0xFFEDE7F6)),
    listOf(Color(0xFFF3E5F5), Color(0xFFE3F2FD)),
)

// ─────────────────────────────────────────────
//  Filter Tab enum
// ─────────────────────────────────────────────
enum class FilterTab(val label: String, val emoji: String) {
    ALL("ทั้งหมด", "📺"),
    FAVORITES("ช่องโปรด", "⭐")
}

// ─────────────────────────────────────────────
//  Root Screen
// ─────────────────────────────────────────────
@Composable
fun LiveListScreen(onChannelClick: (DltvChannel) -> Unit) {
    val context = LocalContext.current
    val allChannels = DltvChannelProvider.getChannels()

    // ── State: favorites (backed by SharedPreferences) ──
    var favoriteIds by rememberSaveable {
        mutableStateOf(loadFavoriteIds(context))
    }

    // ── State: active filter tab ──
    var activeTab by remember { mutableStateOf(FilterTab.ALL) }

    val displayedChannels = when (activeTab) {
        FilterTab.ALL       -> allChannels
        FilterTab.FAVORITES -> allChannels.filter { it.id in favoriteIds }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // พื้นหลังสำหรับเด็กวาดด้วย Canvas
            KidBackground(modifier = Modifier.fillMaxSize())

            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ─── Header ───
                item { LiveListHeader() }

                // ─── Filter Tabs ───
                item {
                    FilterTabRow(
                        activeTab = activeTab,
                        favCount = favoriteIds.size,
                        onTabSelected = { activeTab = it }
                    )
                }

                // ─── Subtitle ───
                item {
                    val subtitle = when (activeTab) {
                        FilterTab.ALL       -> "🎒 เลือกช่องที่อยากดูเลย!"
                        FilterTab.FAVORITES -> "⭐ ช่องที่คุณกดดาวไว้"
                    }
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = KidDeepBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                // ─── Empty state (ช่องโปรด ยังไม่ได้เพิ่ม) ───
                if (displayedChannels.isEmpty()) {
                    item { EmptyFavoriteState() }
                } else {
                    // ─── Channel Cards ───
                    items(items = displayedChannels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            gradientColors = cardGradients[channel.id % cardGradients.size],
                            isFavorite = channel.id in favoriteIds,
                            onFavoriteToggle = {
                                val newSet = if (channel.id in favoriteIds) {
                                    favoriteIds - channel.id
                                } else {
                                    favoriteIds + channel.id
                                }
                                favoriteIds = newSet
                                saveFavoriteIds(context, newSet)
                            },
                            onClick = { onChannelClick(channel) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Filter Tab Row
// ─────────────────────────────────────────────
@Composable
fun FilterTabRow(
    activeTab: FilterTab,
    favCount: Int,
    onTabSelected: (FilterTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterTab.entries.forEach { tab ->
            val isActive = tab == activeTab
            val label = if (tab == FilterTab.FAVORITES && favCount > 0)
                "${tab.emoji} ${tab.label} ($favCount)"
            else
                "${tab.emoji} ${tab.label}"

            val bgColor by animateColorAsState(
                targetValue = if (isActive) KidDeepBlue else Color.White,
                animationSpec = tween(250),
                label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else KidDeepBlue,
                animationSpec = tween(250),
                label = "tabText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Empty Favorite State
// ─────────────────────────────────────────────
@Composable
fun EmptyFavoriteState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bouncing star
        val infiniteTransition = rememberInfiniteTransition(label = "emptyBounce")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -12f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starBounce"
        )
        Text(
            text = "⭐",
            fontSize = 60.sp,
            modifier = Modifier.scale(1f + offsetY * 0.01f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ยังไม่มีช่องโปรดเลย!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "กดดาว ⭐ ที่มุมบนขวาของช่องใดก็ได้\nเพื่อเพิ่มเป็นช่องโปรด",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "ดูสดตอนนี้เลย! กดดาวเก็บช่องโปรด ⭐",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            LiveBadge()
        }
    }
}

// ─────────────────────────────────────────────
//  Pulsing dot
// ─────────────────────────────────────────────
@Composable
fun PulsingLiveDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "liveDot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "liveDotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700), repeatMode = RepeatMode.Reverse
        ), label = "liveDotAlpha"
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
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800), repeatMode = RepeatMode.Reverse
        ), label = "badgeAlpha"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LiveRed.copy(alpha = alpha))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = "● LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
//  Channel Card
// ─────────────────────────────────────────────
@Composable
fun ChannelCard(
    channel: DltvChannel,
    gradientColors: List<Color>,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    val emoji = when {
        channel.level.contains("ปฐมวัย")            -> "🌟"
        channel.level.contains("ประถม")              -> "🎨"
        channel.level.contains("มัธยมศึกษาตอนต้น")  -> "📚"
        channel.level.contains("มัธยมศึกษาตอนปลาย") -> "🎓"
        else                                         -> "📺"
    }
    val accentColor = when {
        channel.level.contains("ปฐมวัย")            -> KidOrange
        channel.level.contains("ประถม")              -> KidGreen
        channel.level.contains("มัธยมศึกษาตอนต้น")  -> KidSkyBlue
        channel.level.contains("มัธยมศึกษาตอนปลาย") -> KidPurple
        else                                         -> KidDeepBlue
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
                .background(Brush.horizontalGradient(gradientColors))
        ) {
            // ─── ปุ่มดาว (ซ้อนบนมุมขวาบน) ───
            FavoriteStarButton(
                isFavorite = isFavorite,
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 52.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ─── Logo ───
                ChannelLogo(logoUrl = channel.logo, accentColor = accentColor, placeholder = emoji)

                Spacer(modifier = Modifier.width(14.dp))

                // ─── Info ───
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = channel.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    GradeBadge(grade = channel.grade, color = accentColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channel.shortDescription,
                        fontSize = 12.sp,
                        color = Color(0xFF555555),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                MiniLiveIndicator()
            }
        }
    }
}

// ─────────────────────────────────────────────
//  ⭐ Favorite Star Button (with bounce anim)
// ─────────────────────────────────────────────
@Composable
fun FavoriteStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // bounce scale เมื่อ toggle
    var triggerAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (triggerAnim) 1.5f else 1f,
        animationSpec = if (triggerAnim)
            keyframes {
                durationMillis = 350
                1.0f at 0
                1.5f at 120 with FastOutSlowInEasing
                0.85f at 220
                1.0f at 350
            }
        else
            spring(stiffness = Spring.StiffnessMedium),
        finishedListener = { triggerAnim = false },
        label = "starScale"
    )

    val starColor by animateColorAsState(
        targetValue = if (isFavorite) FavStar else FavStarEmpty,
        animationSpec = tween(200),
        label = "starColor"
    )

    Text(
        text = "★",
        fontSize = 26.sp,
        color = starColor,
        modifier = modifier
            .scale(scale)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                triggerAnim = true
                onClick()
            }
    )
}

// ─────────────────────────────────────────────
//  Channel Logo
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
//  Grade Badge
// ─────────────────────────────────────────────
@Composable
fun GradeBadge(grade: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = grade, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ─────────────────────────────────────────────
//  Mini Live Indicator
// ─────────────────────────────────────────────
@Composable
fun MiniLiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "miniLive")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "miniDotScale"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(LiveRed)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = "LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LiveRed)
    }
}
