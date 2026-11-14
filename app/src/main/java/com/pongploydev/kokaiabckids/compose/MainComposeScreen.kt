package com.pongploydev.kokaiabckids.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import com.pongploydev.profilefeature.compose.SubscriptionDialogHelper
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.pongploydev.kidswebviewfeature.KidsWebViewActivity
import com.pongploydev.livefeature.LiveListActivity
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.activity.AbcMatchGameActivity
import com.pongploydev.kokaiabckids.activity.GameHubActivity
import com.pongploydev.kokaiabckids.activity.QuizActivity
import com.pongploydev.kokaiabckids.activity.learning.LeaningActvity
import com.pongploydev.kokaiabckids.activity.learning.SubActivity
import com.pongploydev.kokaiabckids.activity.learning.showKids.ColoringGameActivity
import com.pongploydev.kokaiabckids.activity.thailetters.MenuThaiLettersActivity
import com.pongploydev.videowatchfeaturesupabase.utils.ads.InterstitialAdManager
import kotlinx.coroutines.delay
import kotlin.math.sin

// ==================== DESIGN TOKENS ====================

object KidTheme {
    val Primary = Color(0xFF7B61FF)
    val PrimaryDeep = Color(0xFF6246EA)
    val PrimaryLight = Color(0xFFB4A5FF)
    val Accent = Color(0xFFFFD166)
    val Success = Color(0xFF00C853)
    val Danger = Color(0xFFEF476F)
    val TextDark = Color(0xFF2D3047)
    val TextMuted = Color(0xFF8A8FA3)
    val SurfaceSoft = Color(0xFFF7F8FC)

    val HeaderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6A4EFF), Color(0xFF866BFF), Color(0xFF68B6FF))
    )
    val GoldGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFD166), Color(0xFFFF9A62))
    )
}

private fun formatKidDisplayName(rawName: String): String {
    val trimmed = rawName.trim()
    if (trimmed.isEmpty() ||
        trimmed.equals("guest", ignoreCase = true) ||
        trimmed.startsWith("guest", ignoreCase = true) ||
        (trimmed.length > 14 && trimmed.any { it.isDigit() } && !trimmed.contains(" "))
    ) {
        return "น้องคนเก่ง"
    }
    return trimmed
}

val KidsColorPalette = listOf(
    Color(0xFFFF6B6B),
    Color(0xFF4ECDC4),
    Color(0xFFFFD166),
    Color(0xFF06D6A0),
    Color(0xFF118AB2),
    Color(0xFFEF476F),
    Color(0xFF7209B7),
    Color(0xFFF15BB5)
)

val ActivityColorPalette = listOf(
    Color(0xFFFF9E6D),
    Color(0xFF6A67CE),
    Color(0xFF4CD7D0),
    Color(0xFFFF78C8),
    Color(0xFF9DDE1B),
    Color(0xFFE8A0B0)
)

val GradientBackground = Brush.verticalGradient(
    colors = listOf(Color(0xFF87CEEB), Color(0xFF98FB98))
)

val RainbowGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFD166),
        Color(0xFF06D6A0),
        Color(0xFF118AB2),
        Color(0xFF7209B7)
    )
)

// ==================== UTILS ====================

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    clickable(interactionSource = interaction, indication = null) { onClick() }
}

fun playSoundEffect(context: Context, soundResId: Int) {
    try {
        val mp = android.media.MediaPlayer.create(context, soundResId)
        mp?.setOnCompletionListener { player -> player.release() }
        mp?.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



// ==================== SECTION HEADER ====================

@Composable
fun DashboardSection(
    title: String,
    emoji: String = "✨",
    accent: Color = KidTheme.Primary,
    onViewAllClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 22.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidTheme.TextDark
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = accent.copy(alpha = 0.12f),
                modifier = Modifier.clickableNoRipple { onViewAllClick() }
            ) {
                Text(
                    text = "ดูทั้งหมด ›",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

// ==================== REWARD DIALOG ====================

@Composable
fun RewardDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playSoundEffect(context, R.raw.is_sound_success_sound)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(KidTheme.GoldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 รางวัลประจำวัน 🎉",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "trophyScale"
                    )

                    Image(
                        painter = painterResource(id = R.drawable.kids_quiz_3d),
                        contentDescription = "รางวัล",
                        modifier = Modifier
                            .size(115.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ยินดีด้วยจ้าคนเก่ง! 🎉",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "วันนี้หนูตั้งใจเรียนรู้มาก!\nรับรางวัลพิเศษประจำวันไปเลย:",
                        fontSize = 15.sp,
                        color = KidTheme.TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF9E6),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD166))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⭐ +10",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ดาวทอง",
                                    fontSize = 13.sp,
                                    color = KidTheme.TextDark
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.5.dp, Color(0xFF81C784))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏆 เหรียญทอง",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickableNoRipple {
                                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(26.dp),
                        color = KidTheme.Success,
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "รับรางวัลเลย! เย้! 🎈",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}



// ==================== BANNER CAROUSEL ====================

data class HomeBannerItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val tag: String,
    val imageRes: Int,
    val bgColors: List<Color>,
    val buttonText: String,
    val actionId: Int
)

fun getHomeBanners(): List<HomeBannerItem> = listOf(
    HomeBannerItem(
        id = 1,
        title = "พยัญชนะไทย ก-ฮ",
        subtitle = "ฝึกอ่าน คัดลายมือ เพลิดเพลิน",
        tag = "🌟 ยอดนิยม",
        imageRes = R.drawable.kids_thai_3d,
        bgColors = listOf(Color(0xFFFF5E7E), Color(0xFFFF9966)),
        buttonText = "เริ่มฝึกอ่านเลย",
        actionId = 2
    ),
    HomeBannerItem(
        id = 2,
        title = "นับเลขแสนสนุก 1-20",
        subtitle = "บวกลบเลขง่ายๆ เสริมทักษะ",
        tag = "🔢 ฝึกสมอง",
        imageRes = R.drawable.kids_math_3d,
        bgColors = listOf(Color(0xFF3A88E9), Color(0xFF00C6FF)),
        buttonText = "ไปลุยกันเลย",
        actionId = 1
    ),
    HomeBannerItem(
        id = 3,
        title = "ภาษาอังกฤษ A-Z",
        subtitle = "โฟนิกส์และคำศัพท์น่ารู้",
        tag = "🔤 สนุกทุกวัน",
        imageRes = R.drawable.kids_english_3d,
        bgColors = listOf(Color(0xFF00B09B), Color(0xFF96C93D)),
        buttonText = "เรียน A-Z เลย",
        actionId = 3
    ),
    HomeBannerItem(
        id = 4,
        title = "วาดภาพระบายสี",
        subtitle = "แต่งแต้มสีสัน เสริมจินตนาการ",
        tag = "🎨 ศิลปะแสนสนุก",
        imageRes = R.drawable.kids_art_3d,
        bgColors = listOf(Color(0xFFF857A6), Color(0xFFFF5858)),
        buttonText = "ไปวาดรูปเลย",
        actionId = 8
    ),
    HomeBannerItem(
        id = 5,
        title = "ทีวีเพื่อการศึกษา",
        subtitle = "ชมสดช่อง DLTV ทุกระดับชั้น",
        tag = "📺 ชมสดออนไลน์",
        imageRes = R.drawable.kids_tv_3d,
        bgColors = listOf(Color(0xFF654EA3), Color(0xFFEAAFC8)),
        buttonText = "รับชมตอนนี้",
        actionId = 7
    )
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AutoSlideBannerCarousel(
    banners: List<HomeBannerItem>,
    onBannerClick: (HomeBannerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth().height(158.dp)
        ) { page ->
            val banner = banners[page]
            val pageOffset = ((pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
            val cardScale = 1f - kotlin.math.abs(pageOffset) * 0.07f

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = 1f - kotlin.math.abs(pageOffset) * 0.25f
                    }
                    .clickableNoRipple {
                        playSoundEffect(context, R.raw.is_sound_buttonswitch)
                        com.pongploydev.kokaiabckids.log.AnalyticsLogger.logClickEvent(context, "แบนเนอร์_${banner.title}", "หน้าแรก (Home)")
                        onBannerClick(banner)
                    },
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(colors = banner.bgColors))
                ) {
                    // วงกลมตกแต่ง
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterEnd)
                            .graphicsLayer { translationX = 46f }
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.13f))
                    )
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.TopStart)
                            .graphicsLayer { translationX = -20f; translationY = -20f }
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = banner.tag,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = banner.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = banner.subtitle,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                modifier = Modifier.clickableNoRipple {
                                    playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                    onBannerClick(banner)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = banner.buttonText,
                                        color = banner.bgColors.first(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "›",
                                        color = banner.bgColors.first(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Image(
                            painter = painterResource(id = banner.imageRes),
                            contentDescription = banner.title,
                            modifier = Modifier.size(98.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val dotWidth by animateFloatAsState(
                    targetValue = if (isSelected) 22f else 7f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "dotWidth"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(dotWidth.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) KidTheme.Primary
                            else KidTheme.Primary.copy(alpha = 0.25f)
                        )
                )
            }
        }
    }
}

// ==================== HOME HEADER ====================

@Composable
fun HomeHeader(
    userName: String = "คนเก่ง",
    isMusicEnabled: Boolean = true,
    starCount: Int = 150,
    level: Int = 3,
    levelTitle: String = "นักอ่านตัวน้อย",
    levelProgress: Float = 0.7f,
    starsRemaining: Int = 30,
    streakDays: Int = 5,
    isSubscriptionEnabled: Boolean = true,
    onMenuClick: () -> Unit = {},
    onStarClick: () -> Unit = {},
    onSubscriptionClick: () -> Unit = {},
    onMusicToggle: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSearchChange: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var messageIndex by remember { mutableStateOf(0) }
    var showSpeechBubble by remember { mutableStateOf(false) }

    val encouragingMessages = remember {
        listOf(
            "สวัสดีจ้าคนเก่ง! วันนี้อยากเรียนรู้อะไรดีนะ? 🌟",
            "หนูเก่งมากๆ เลย ฝึกอ่านทุกวันนะจ๊ะ! 💖",
            "แตะที่การ์ดด้านล่างเพื่อเริ่มสนุกกันเลย! 🚀",
            "ว้าว! วันนี้หนูพร้อมรับดาวทองหรือยัง? ⭐",
            "สู้ๆ นะคนเก่ง มาเรียนให้ครบ 3 บทเรียนกัน! 🎉"
        )
    }

    var avatarBounceTrigger by remember { mutableStateOf(0) }
    val avatarScale by animateFloatAsState(
        targetValue = if (avatarBounceTrigger % 2 == 1) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatarScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "starFloat")
    val starOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starY"
    )

    val animatedLevelProgress by animateFloatAsState(
        targetValue = levelProgress,
        animationSpec = tween(900),
        label = "lvlProgress"
    )

    LaunchedEffect(avatarBounceTrigger) {
        if (avatarBounceTrigger > 0) {
            delay(250)
            avatarBounceTrigger = 0
        }
    }

    LaunchedEffect(showSpeechBubble) {
        if (showSpeechBubble) {
            delay(4000)
            showSpeechBubble = false
        }
    }

    val displayKidName = remember(userName) { formatKidDisplayName(userName) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(KidTheme.HeaderGradient)
    ) {
        // วงกลมตกแต่งพื้นหลัง header สไตล์น่ารักสำหรับเด็ก
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 56f; translationY = -64f }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer { translationX = -38f; translationY = 38f }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Text(
            text = "✨",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 66.dp, end = 24.dp)
                .graphicsLayer { alpha = 0.65f }
        )

        Column(
            modifier = Modifier
                .padding(top = 14.dp, bottom = 22.dp)
                .padding(horizontal = 18.dp)
        ) {
            // แถวบน: เมนู + ดาว + ปุ่มลัดแบบไอคอนกะทัดรัด (พอดีจอไม่ล้นขอบขวา)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickableNoRipple {
                                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                onMenuClick()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "เปิดเมนู",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                        modifier = Modifier.clickableNoRipple {
                            playSoundEffect(context, R.raw.is_sound_buttonswitch)
                            onStarClick()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐ $starCount",
                                color = KidTheme.Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.graphicsLayer { translationY = starOffsetY }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ดาว",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (isSubscriptionEnabled) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFFFD166).copy(alpha = 0.30f),
                            border = BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.9f)),
                            modifier = Modifier.clickableNoRipple {
                                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                onSubscriptionClick()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "👑", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "VIP",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSearchVisible) KidTheme.Accent
                        else Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(
                            1.dp,
                            if (isSearchVisible) KidTheme.Accent.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.42f)
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .clickableNoRipple {
                                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) {
                                    searchQuery = ""
                                    onSearchChange("")
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close
                                else Icons.Default.Search,
                                contentDescription = if (isSearchVisible) "ปิดค้นหา" else "ค้นหา",
                                tint = if (isSearchVisible) KidTheme.TextDark else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isMusicEnabled) KidTheme.Success.copy(alpha = 0.88f)
                        else Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(
                            1.dp,
                            if (isMusicEnabled) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.42f)
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .clickableNoRipple {
                                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                onMusicToggle()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isMusicEnabled) Icons.Rounded.MusicNote else Icons.Rounded.MusicOff,
                                contentDescription = if (isMusicEnabled) "ปิดเพลง" else "เปิดเพลง",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)),
                        modifier = Modifier
                            .size(36.dp)
                            .clickableNoRipple {
                                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                onSettingsClick()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "ตั้งค่า",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ทักทาย + อวตารมาสคอตเด็กน้อยถือพู่กัน
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "สวัสดี $displayKidName! 👋",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "พร้อมสนุกและเรียนรู้กันหรือยัง? 🚀",
                        color = Color.White.copy(alpha = 0.94f),
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = avatarScale
                            scaleY = avatarScale
                            translationY = starOffsetY
                        }
                        .clickableNoRipple {
                            playSoundEffect(context, R.raw.is_sound_buttonswitch)
                            avatarBounceTrigger = 1
                            messageIndex = (messageIndex + 1) % encouragingMessages.size
                            showSpeechBubble = true
                        }
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFF8E7),
                        border = BorderStroke(3.dp, KidTheme.Accent),
                        shadowElevation = 8.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app),
                            contentDescription = "แตะเพื่อคุยกับฉัน",
                            modifier = Modifier.fillMaxSize().padding(2.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp),
                        shape = CircleShape,
                        color = KidTheme.Accent,
                        border = BorderStroke(1.5.dp, Color.White),
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "💬", fontSize = 11.sp)
                        }
                    }
                }
            }

            // กล่องคำพูด
            AnimatedVisibility(
                visible = showSpeechBubble,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableNoRipple { showSpeechBubble = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🐥", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = encouragingMessages[messageIndex],
                                color = KidTheme.TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // แถบเลเวล + สตรีค
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🏅", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ระดับ $level · $levelTitle",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "🔥 $streakDays วัน",
                                color = Color(0xFFFFE58F),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(9.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.28f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedLevelProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(KidTheme.GoldGradient)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "อีก $starsRemaining ดาว จะขึ้นระดับ ${level + 1}! 🚀",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ช่องค้นหา
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchChange(it)
                        },
                        placeholder = {
                            Text(
                                "ค้นหาบทเรียนหรือคำศัพท์...",
                                color = KidTheme.TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = KidTheme.TextMuted
                            )
                        },
                        singleLine = true
                    )
                }
            }
        }
    }
}

// ==================== QUICK ACTIONS ====================

data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val topColor: Color,
    val id: Int
)

@Composable
fun QuickActionRow(onActionClick: (Int) -> Unit) {
    val ctx = LocalContext.current
    val actions = remember {
        listOf(
            QuickAction(Icons.Rounded.Edit, "ฝึกเขียน", Color(0xFFFF5E5B), Color(0xFFFF9A8B), 1),
            QuickAction(Icons.Rounded.VolumeUp, "ฝึกอ่าน", Color(0xFF0EA5E9), Color(0xFF67E8F9), 2),
            QuickAction(Icons.Rounded.Palette, "ระบายสี", Color(0xFFFF9F1C), Color(0xFFFFD166), 3),
            QuickAction(Icons.Rounded.SportsEsports, "เกมสนุก", Color(0xFFEC4899), Color(0xFFF9A8D4), 5),
            QuickAction(Icons.Rounded.Quiz, "ทดสอบ", Color(0xFF7C3AED), Color(0xFFC4B5FD), 4)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { a ->
            var pressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (pressed) 0.88f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "qaScale"
            )
            LaunchedEffect(pressed) {
                if (pressed) {
                    delay(120)
                    pressed = false
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickableNoRipple {
                        pressed = true
                        playSoundEffect(ctx, R.raw.is_sound_buttonswitch)
                        com.pongploydev.kokaiabckids.log.AnalyticsLogger.logClickEvent(
                            ctx, "เมนูด่วน_${a.label}", "หน้าแรก (Home)"
                        )
                        onActionClick(a.id)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 5.dp,
                    border = BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(a.color.copy(alpha = 0.55f), a.color.copy(alpha = 0.18f))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(a.color.copy(alpha = 0.16f), a.color.copy(alpha = 0.04f))
                                    )
                                )
                        )

                        // ปุ่มไอคอนลูกกวาด 3D สีสดใสสำหรับเด็ก
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(a.topColor, a.color)
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = a.icon,
                                contentDescription = a.label,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            a.color.copy(alpha = 0.25f),
                                            a.color.copy(alpha = 0.78f),
                                            a.color.copy(alpha = 0.25f)
                                        )
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = a.label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3142),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==================== CATEGORY CARD ====================

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    backgroundColor: Color,
    accentColor: Color = KidTheme.Primary,
    lessonCount: Int = 12,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "floatImg")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Card(
        modifier = Modifier
            .width(182.dp)
            .height(228.dp)
            .padding(start = 20.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickableNoRipple {
                isPressed = true
                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                com.pongploydev.kokaiabckids.log.AnalyticsLogger.logClickEvent(context, "หมวดหมู่_$title", "หน้าแรก (Home)")
                onClick()
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer { translationX = 34f; translationY = -34f }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f))
            )

            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.7f)) {
                    Text(
                        text = "$lessonCount บทเรียน",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidTheme.TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = KidTheme.TextDark.copy(alpha = 0.65f),
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Surface(shape = CircleShape, color = accentColor, shadowElevation = 3.dp) {
                        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "›",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        modifier = Modifier
                            .size(96.dp)
                            .graphicsLayer { translationY = floatOffset },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreCategoriesSection(
    onCategoryClick: (Int) -> Unit,
    onViewAllClick: () -> Unit = {}
) {
    DashboardSection(
        title = "หมวดหมู่การเรียนรู้",
        emoji = "📚",
        accent = KidTheme.Primary,
        onViewAllClick = onViewAllClick
    ) {
        LazyRow(contentPadding = PaddingValues(end = 20.dp)) {
            item {
                CategoryCard(
                    title = "คณิตศาสตร์",
                    subtitle = "ฝึกนับเลข 1-20\nและบวกลบเลข",
                    imageRes = R.drawable.kids_math_3d,
                    backgroundColor = Color(0xFFE8E4FF),
                    accentColor = Color(0xFF6A67CE),
                    lessonCount = 20,
                    onClick = { onCategoryClick(1) }
                )
            }
            item {
                CategoryCard(
                    title = "ภาษาอังกฤษ",
                    subtitle = "โฟนิกส์ A-Z\nและคำศัพท์น่ารู้",
                    imageRes = R.drawable.kids_english_3d,
                    backgroundColor = Color(0xFFE2F9E9),
                    accentColor = Color(0xFF00B09B),
                    lessonCount = 26,
                    onClick = { onCategoryClick(2) }
                )
            }
            item {
                CategoryCard(
                    title = "ภาษาไทย",
                    subtitle = "ฝึกอ่าน-เขียน\nพยัญชนะ ก-ฮ",
                    imageRes = R.drawable.kids_thai_3d,
                    backgroundColor = Color(0xFFFFE8EC),
                    accentColor = Color(0xFFFF5E7E),
                    lessonCount = 44,
                    onClick = { onCategoryClick(4) }
                )
            }
            item {
                CategoryCard(
                    title = "ศิลปะระบายสี",
                    subtitle = "วาดภาพระบายสี\nเสริมจินตนาการ",
                    imageRes = R.drawable.kids_art_3d,
                    backgroundColor = Color(0xFFFFF0B4),
                    accentColor = Color(0xFFFF9800),
                    lessonCount = 15,
                    onClick = { onCategoryClick(3) }
                )
            }
            item {
                CategoryCard(
                    title = "แบบทดสอบ",
                    subtitle = "ตอบคำถามสนุกๆ\nสะสมคะแนน",
                    imageRes = R.drawable.kids_quiz_3d,
                    backgroundColor = Color(0xFFFFE2E2),
                    accentColor = Color(0xFFFF4B4B),
                    lessonCount = 30,
                    onClick = { onCategoryClick(5) }
                )
            }
            item {
                CategoryCard(
                    title = "ทีวีเพื่อการศึกษา",
                    subtitle = "ช่องรายการสด\nDLTV ทุกระดับชั้น",
                    imageRes = R.drawable.kids_tv_3d,
                    backgroundColor = Color(0xFFE0F7FA),
                    accentColor = Color(0xFF0085FF),
                    lessonCount = 12,
                    onClick = { onCategoryClick(6) }
                )
            }
            item {
                CategoryCard(
                    title = "เกมแสนสนุก",
                    subtitle = "เกมฝึกสมอง 10 รูปแบบ\nสะสมคะแนนและดาว ⭐",
                    imageRes = R.drawable.kids_quiz_3d,
                    backgroundColor = Color(0xFFF3E5F5),
                    accentColor = Color(0xFFAB47BC),
                    lessonCount = 10,
                    onClick = { onCategoryClick(7) }
                )
            }
        }
    }
}

// ==================== LEARNING CARD ====================

@Composable
fun LearningCard(
    title: String,
    subject: String,
    progress: Float,
    imageRes: Int,
    backgroundColor: Color,
    progressColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "learnScale"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900),
        label = "learnProgress"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Card(
        modifier = Modifier
            .width(172.dp)
            .padding(start = 20.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickableNoRipple {
                isPressed = true
                playSoundEffect(context, R.raw.is_sound_buttonswitch)
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(shape = RoundedCornerShape(50), color = progressColor.copy(alpha = 0.15f)) {
                Text(
                    text = subject,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = KidTheme.TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                    modifier = Modifier.width(36.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(progressColor)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(shape = CircleShape, color = progressColor) {
                    Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContinueLearningSection(
    onLessonClick: (Int) -> Unit,
    onViewAllClick: () -> Unit = {}
) {
    DashboardSection(
        title = "เรียนรู้ต่อเนื่อง",
        emoji = "🚀",
        accent = Color(0xFF00B09B),
        onViewAllClick = onViewAllClick
    ) {
        LazyRow(contentPadding = PaddingValues(end = 20.dp)) {
            item {
                LearningCard(
                    title = "ผจญภัยตัวเลข\n1 ถึง 20",
                    subject = "คณิตศาสตร์",
                    progress = 0.65f,
                    imageRes = R.drawable.kids_math_3d,
                    backgroundColor = Color(0xFFE2F0FF),
                    progressColor = Color(0xFF0085FF),
                    onClick = { onLessonClick(1) }
                )
            }
            item {
                LearningCard(
                    title = "เพื่อนสัตว์โลก\nแสนน่ารัก",
                    subject = "ภาษาอังกฤษ",
                    progress = 0.77f,
                    imageRes = R.drawable.bear,
                    backgroundColor = Color(0xFFFFE2E2),
                    progressColor = Color(0xFFFF4B4B),
                    onClick = { onLessonClick(2) }
                )
            }
            item {
                LearningCard(
                    title = "พยัญชนะไทย\nก ไก่ ถึง ฮ นกฮูก",
                    subject = "ภาษาไทย",
                    progress = 0.85f,
                    imageRes = R.drawable.kids_thai_3d,
                    backgroundColor = Color(0xFFFFF4E0),
                    progressColor = Color(0xFFFF9800),
                    onClick = { onLessonClick(4) }
                )
            }
            item {
                LearningCard(
                    title = "ต้นไม้และธรรมชาติ\nรอบตัวเรา",
                    subject = "วิทยาศาสตร์",
                    progress = 0.46f,
                    imageRes = R.drawable.sunflower,
                    backgroundColor = Color(0xFFE2FFF0),
                    progressColor = KidTheme.Success,
                    onClick = { onLessonClick(3) }
                )
            }
        }
    }
}

// ==================== DAILY GOAL ====================

@Composable
fun DailyGoalSection(onGiftClick: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "giftWiggle")
    val wiggleAngle by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wAngle"
    )
    val giftScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gScale"
    )
    val goalProgress by animateFloatAsState(
        targetValue = 0.66f,
        animationSpec = tween(1000),
        label = "goalProgress"
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 22.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(KidTheme.Danger)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "🎯", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "เป้าหมายประจำวัน",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidTheme.TextDark
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFFE8EC),
                modifier = Modifier.clickableNoRipple { onGiftClick() }
            ) {
                Text(
                    text = "🎁 เปิดของขวัญ!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidTheme.Danger,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kids_thai_3d),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "เก่งมากเลยคนเก่ง! 🎉",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidTheme.TextDark
                    )
                    Text(
                        text = "เรียนสำเร็จ 2 จาก 3 บทเรียนแล้ว",
                        fontSize = 13.sp,
                        color = KidTheme.TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(goalProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF06D6A0), Color(0xFF00C853))
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.clickableNoRipple { onGiftClick() }) {
                    Image(
                        painter = painterResource(id = R.drawable.kids_quiz_3d),
                        contentDescription = "กล่องของขวัญ",
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer {
                                rotationZ = wiggleAngle
                                scaleX = giftScale
                                scaleY = giftScale
                            }
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

// ==================== MAIN SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposeScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var isMusicEnabled by remember {
        mutableStateOf(
            if (isPreview) true
            else com.pongploydev.kokaiabckids.utils.MusicManager
                .getInstance().isMusicEnabled(context)
        )
    }
    var showRewardDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val supabaseAuthManager = remember { SupabaseAuthManager.getInstance(context) }
    val dataManager = remember { SupabaseDataManager.getInstance(context) }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val userId = supabaseAuthManager.getStoredUser()?.id ?: firebaseAuth.currentUser?.uid ?: "guest_user"

    var starCount by remember { mutableIntStateOf(dataManager.getUserTotalStars()) }
    var currentLevel by remember { mutableIntStateOf(dataManager.getUserCurrentLevel()) }
    var userDisplayName by remember { mutableStateOf(dataManager.getLocalDisplayName() ?: "คนเก่ง") }
    var isSubscriptionEnabled by remember {
        mutableStateOf(dataManager.getCachedAppSettings()?.isSubscriptionEnabled ?: false)
    }

    fun syncAppSettings() {
        scope.launch {
            dataManager.fetchAppSettings().onSuccess { settings ->
                isSubscriptionEnabled = settings.isSubscriptionEnabled
            }
        }
    }

    // Sync live stars and level from local persistent cache & Supabase
    fun loadSupabaseStars() {
        val currentUserId = supabaseAuthManager.getStoredUser()?.id ?: firebaseAuth.currentUser?.uid ?: "guest_user"
        val localStars = dataManager.getUserTotalStars()
        val localLvl = dataManager.getUserCurrentLevel()
        if (localStars > starCount) {
            starCount = localStars
            currentLevel = localLvl
        } else if (starCount > localStars) {
            dataManager.saveUserProgress(starCount, currentLevel)
        }

        if (currentUserId.isNotEmpty()) {
            scope.launch {
                dataManager.fetchUserProfile(currentUserId).onSuccess { prof ->
                    starCount = prof.totalStars
                    currentLevel = prof.currentLevel
                    val name = prof.displayName
                    if (!name.isNullOrBlank()) {
                        userDisplayName = name
                    }
                }
            }
        }
    }

    val homeLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(homeLifecycleOwner, userId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                loadSupabaseStars()
                syncAppSettings()
            }
        }
        homeLifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            homeLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(userId) {
        loadSupabaseStars()
        syncAppSettings()
    }

    val menuItems = remember { getMenuItems() }

    val interstitialAdManager = remember { InterstitialAdManager(context) }
    var isAdLoading by remember { mutableStateOf(false) }
    var adLoadAttempt by remember { mutableStateOf(0) }

    val loadInterstitialAd: (InterstitialAdManager) -> Unit = { manager ->
        if (!isAdLoading && adLoadAttempt < 3) {
            isAdLoading = true
            adLoadAttempt++
            manager.loadInterstitialAd(
                onAdLoaded = {
                    isAdLoading = false
                    adLoadAttempt = 0
                },
                onAdFailed = { isAdLoading = false }
            )
        }
    }

    val navigateWithAd: (() -> Unit) -> Unit = { onNavigate ->
        val activity = context as? Activity
        if (activity != null && interstitialAdManager.isAdLoaded()) {
            interstitialAdManager.showInterstitialAd(
                activity = activity,
                onAdDismissed = { onNavigate() },
                onAdFailed = { onNavigate() }
            )
        } else {
            onNavigate()
            if (!interstitialAdManager.isAdLoaded()) loadInterstitialAd(interstitialAdManager)
        }
    }

    LaunchedEffect(Unit) {
        if (!isPreview) loadInterstitialAd(interstitialAdManager)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val musicManager = com.pongploydev.kokaiabckids.utils.MusicManager.getInstance()
        val observer = LifecycleEventObserver { _, event ->
            if (isPreview) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> musicManager.startMusic(context)
                Lifecycle.Event.ON_PAUSE -> musicManager.pauseMusic()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showRewardDialog) {
        RewardDialog(onDismiss = {
            showRewardDialog = false
            scope.launch {
                val (newStars, newLvl) = dataManager.addStars(userId, 10)
                starCount = newStars
                currentLevel = newLvl
                Toast.makeText(context, "บันทึกคะแนนสำเร็จแล้ว! ⭐ +10 🌟 (รวม $starCount ดาว)", Toast.LENGTH_SHORT).show()
            }
        })
    }

    if (showSettingsDialog) {
        KidFriendlySettingsDialog(
            isMusicEnabled = isMusicEnabled,
            onMusicToggle = { enabled ->
                isMusicEnabled = enabled
                com.pongploydev.kokaiabckids.utils.MusicManager
                    .getInstance().setMusicEnabled(context, enabled)
            },
            onOtherActivitiesClick = {
                showSettingsDialog = false
                showKidsActivitiesDialog(context)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showSubscriptionDialog && isSubscriptionEnabled) {
        SubscriptionDialogHelper.Show(
            onDismiss = { showSubscriptionDialog = false },
            onSubscribed = {
                showSubscriptionDialog = false
                starCount = dataManager.getUserTotalStars()
                currentLevel = dataManager.getUserCurrentLevel()
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                KidBackground(modifier = Modifier.fillMaxSize())

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    item {
                        val starsInLevel = starCount % 50
                        val starsNeeded = 50 - starsInLevel
                        val levelProgress = (starsInLevel.toFloat() / 50f).coerceIn(0f, 1f)
                        val computedLevel = maxOf(1, 1 + (starCount / 50))
                        val levelTitle = when (computedLevel) {
                            1 -> "นักอ่านตัวน้อย"
                            2 -> "นักสะสมดาว"
                            3 -> "ยอดนักอ่าน"
                            4 -> "ผู้พิชิตคำศัพท์"
                            5 -> "ยอดอัจฉริยะตัวน้อย"
                            else -> "ปรมาจารย์ตัวน้อย"
                        }

                        HomeHeader(
                            userName = userDisplayName,
                            isMusicEnabled = isMusicEnabled,
                            starCount = starCount,
                            level = computedLevel,
                            levelTitle = levelTitle,
                            levelProgress = levelProgress,
                            starsRemaining = starsNeeded,
                            streakDays = dataManager.getUserStreakDays(),
                            isSubscriptionEnabled = isSubscriptionEnabled,
                            onMenuClick = {
                                (context as? com.pongploydev.kokaiabckids.activity.MainActivity)?.openDrawer()
                            },
                            onStarClick = {
                                scope.launch {
                                    val (newStars, newLvl) = dataManager.addStars(userId, 5)
                                    starCount = newStars
                                    currentLevel = newLvl
                                    Toast.makeText(context, "บันทึกคะแนนสำเร็จแล้ว! ⭐ +5 (รวม $starCount ดาว)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onMusicToggle = {
                                isMusicEnabled = !isMusicEnabled
                                com.pongploydev.kokaiabckids.utils.MusicManager
                                    .getInstance().setMusicEnabled(context, isMusicEnabled)
                            },
                            onSettingsClick = { showSettingsDialog = true },
                            onSubscriptionClick = { showSubscriptionDialog = true },
                            onSearchChange = { }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(18.dp)) }

                    item {
                        QuickActionRow(onActionClick = { id ->
                            val actionName = when (id) {
                                1 -> "ฝึกเขียน ก-ฮ"
                                2 -> "ฝึกอ่าน"
                                3 -> "ระบายสี"
                                4 -> "ทดสอบวัดความรู้"
                                5 -> "ศูนย์รวมเกมเด็ก"
                                else -> "กิจกรรมเรียนรู้"
                            }
                            scope.launch {
                                dataManager.saveLearningProgress(userId, "home_quick_action", "action_$id", 100)
                                val (newStars, newLvl) = dataManager.addStars(userId, 5)
                                starCount = newStars
                                currentLevel = newLvl
                            }
                            Toast.makeText(context, "บันทึกคะแนนสำเร็จแล้ว! ⭐ +5 🌟 ($actionName)", Toast.LENGTH_SHORT).show()
                            navigateWithAd {
                                when (id) {
                                    1 -> context.startActivity(
                                        Intent(context, MenuThaiLettersActivity::class.java).apply {
                                            putExtra(MenuThaiLettersActivity.EXTRA_MODE, "THAI")
                                        }
                                    )
                                    2 -> context.startActivity(
                                        Intent(context, LeaningActvity::class.java)
                                    )
                                    3 -> context.startActivity(
                                        Intent(context, ColoringGameActivity::class.java)
                                    )
                                    4 -> context.startActivity(
                                        Intent(context, QuizActivity::class.java)
                                    )
                                    5 -> context.startActivity(
                                        Intent(context, GameHubActivity::class.java)
                                    )
                                }
                            }
                        })
                    }

                    item { Spacer(modifier = Modifier.height(14.dp)) }

                    // VIP MEMBER / SUBSCRIPTION BANNER CARD
                    if (isSubscriptionEnabled) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clickableNoRipple {
                                        playSoundEffect(context, R.raw.is_sound_buttonswitch)
                                        showSubscriptionDialog = true
                                    },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF7B61FF), Color(0xFF9B6BFF), Color(0xFFFF8E72))
                                            )
                                        )
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "👑", fontSize = 22.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "ปลดล็อก VIP สนุกไร้โฆษณา ✨",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "รับดาวโบนัส x2 • เล่นได้ทุกด่าน & เกม",
                                                    color = Color.White.copy(alpha = 0.94f),
                                                    fontSize = 11.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = Color(0xFFFFD166),
                                            shadowElevation = 2.dp
                                        ) {
                                            Text(
                                                text = "เปิด VIP ✨",
                                                color = Color(0xFF2D3047),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(18.dp)) }
                    }

                    item {
                        val banners = remember { getHomeBanners() }
                        AutoSlideBannerCarousel(
                            banners = banners,
                            onBannerClick = { banner ->
                                navigateWithAd {
                                    when (banner.id) {
                                        1 -> context.startActivity(
                                            Intent(context, MenuThaiLettersActivity::class.java)
                                                .apply {
                                                    putExtra(
                                                        MenuThaiLettersActivity.EXTRA_MODE,
                                                        "THAI"
                                                    )
                                                }
                                        )
                                        2 -> context.startActivity(
                                            Intent(context, SubActivity::class.java).apply {
                                                putExtra("categoryPosition", 1)
                                                putExtra("Category", "ตัวเลข 1-20")
                                                putExtra("Type", 1)
                                            }
                                        )
                                        3 -> context.startActivity(
                                            Intent(context, MenuThaiLettersActivity::class.java)
                                                .apply {
                                                    putExtra(
                                                        MenuThaiLettersActivity.EXTRA_MODE,
                                                        "ENGLISH"
                                                    )
                                                }
                                        )
                                        4 -> context.startActivity(
                                            Intent(context, ColoringGameActivity::class.java)
                                        )
                                        5 -> context.startActivity(
                                            Intent(context, LiveListActivity::class.java)
                                        )
                                        else -> {
                                            menuItems.find { it.id == banner.actionId }?.let {
                                                handleItemClickDirectly(context, it, navController)
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(14.dp)) }

                    item {
                        ExploreCategoriesSection(
                            onCategoryClick = { index ->
                                navigateWithAd {
                                    when (index) {
                                        1 -> context.startActivity(
                                            Intent(context, SubActivity::class.java).apply {
                                                putExtra("categoryPosition", 1)
                                                putExtra("Category", "ตัวเลข 1-20")
                                                putExtra("Type", 1)
                                            }
                                        )
                                        2 -> context.startActivity(
                                            Intent(context, MenuThaiLettersActivity::class.java)
                                                .apply {
                                                    putExtra(
                                                        MenuThaiLettersActivity.EXTRA_MODE,
                                                        "ENGLISH"
                                                    )
                                                }
                                        )
                                        3 -> context.startActivity(
                                            Intent(context, ColoringGameActivity::class.java)
                                        )
                                        4 -> context.startActivity(
                                            Intent(context, MenuThaiLettersActivity::class.java)
                                                .apply {
                                                    putExtra(
                                                        MenuThaiLettersActivity.EXTRA_MODE,
                                                        "THAI"
                                                    )
                                                }
                                        )
                                        5 -> context.startActivity(
                                            Intent(context, QuizActivity::class.java)
                                        )
                                        6 -> context.startActivity(
                                            Intent(context, LiveListActivity::class.java)
                                        )
                                        7 -> context.startActivity(
                                            Intent(context, GameHubActivity::class.java)
                                        )
                                        else -> context.startActivity(
                                            Intent(context, LeaningActvity::class.java)
                                        )
                                    }
                                }
                            },
                            onViewAllClick = {
                                navigateWithAd {
                                    context.startActivity(
                                        Intent(context, LeaningActvity::class.java)
                                    )
                                }
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    item {
                        ContinueLearningSection(
                            onLessonClick = { index ->
                                navigateWithAd {
                                    when (index) {
                                        1 -> context.startActivity(
                                            Intent(context, SubActivity::class.java).apply {
                                                putExtra("categoryPosition", 1)
                                                putExtra("Category", "ตัวเลข 1-20")
                                                putExtra("Type", 1)
                                            }
                                        )
                                        2 -> context.startActivity(
                                            Intent(context, SubActivity::class.java).apply {
                                                putExtra("categoryPosition", 4)
                                                putExtra("Category", "สัตว์น่ารัก")
                                                putExtra("Type", 1)
                                            }
                                        )
                                        3 -> context.startActivity(
                                            Intent(context, SubActivity::class.java).apply {
                                                putExtra("categoryPosition", 6)
                                                putExtra("Category", "ดอกไม้และพืช")
                                                putExtra("Type", 1)
                                            }
                                        )
                                        4 -> context.startActivity(
                                            Intent(context, MenuThaiLettersActivity::class.java)
                                                .apply {
                                                    putExtra(
                                                        MenuThaiLettersActivity.EXTRA_MODE,
                                                        "THAI"
                                                    )
                                                }
                                        )
                                        else -> context.startActivity(
                                            Intent(context, LeaningActvity::class.java)
                                        )
                                    }
                                }
                            },
                            onViewAllClick = {
                                navigateWithAd {
                                    context.startActivity(
                                        Intent(context, LeaningActvity::class.java)
                                    )
                                }
                            }
                        )
                    }

                    item { DailyGoalSection(onGiftClick = { showRewardDialog = true }) }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    )
}

// ==================== BANNER AD FALLBACK ====================

@Composable
fun BannerAdFallback() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = KidTheme.TextDark)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clickableNoRipple {
                Toast.makeText(context, "ขอบคุณที่สนับสนุนนักพัฒนา! 🙏", Toast.LENGTH_SHORT).show()
            },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFF6B6B), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        "สนับสนุนนักพัฒนา",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "ขอบคุณที่ใช้แอปพลิเคชันของเรา",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF06D6A0), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("สนับสนุน", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== MISC COMPONENTS ====================

@Composable
fun VisitCounter(currentCount: Int, maxCount: Int, isAdLoaded: Boolean) {
    val remaining = maxCount - currentCount
    val progress = currentCount.toFloat() / maxCount.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                currentCount >= maxCount && isAdLoaded -> "🎉 พร้อมแสดงโฆษณา!"
                currentCount >= maxCount -> "⏳ รอโฆษณาโหลด..."
                else -> "📊 อีก $remaining ครั้งจะแสดงโฆษณา"
            },
            color = when {
                currentCount >= maxCount && isAdLoaded -> Color(0xFF06D6A0)
                currentCount >= maxCount -> Color(0xFFFF6B6B)
                else -> KidTheme.TextMuted
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(
                        when {
                            currentCount >= maxCount && isAdLoaded -> Color(0xFF06D6A0)
                            currentCount >= maxCount -> Color(0xFFFF6B6B)
                            else -> Color(0xFF4ECDC4)
                        },
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        Text(
            text = "ขอบคุณที่สนับสนุนนักพัฒนา! 🙏",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AdLoadingStatus(isAdLoading: Boolean, isAdLoaded: Boolean, adLoadAttempt: Int) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isAdLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.dp,
                    color = Color(0xFF4ECDC4)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "กำลังโหลดโฆษณาเต็มจอ... (ครั้งที่ $adLoadAttempt)",
                    color = KidTheme.TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    buttonColor: Color,
    size: Dp = 40.dp
) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isClicked) 360f else 0f,
        animationSpec = tween(500),
        label = "iconRotation"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(500)
            isClicked = false
        }
    }

    IconButton(onClick = {
        isClicked = true
        onClick()
    }) {
        Box(
            modifier = Modifier
                .size(size)
                .background(buttonColor, RoundedCornerShape(10.dp))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ViewModeSelector(currentMode: ViewMode, onModeSelected: (ViewMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFFFD166), Color(0xFFFF6B6B)))
                )
                .padding(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                AnimatedSelectorButton(
                    text = "แบบตาราง",
                    isSelected = currentMode == ViewMode.GRID,
                    onClick = { onModeSelected(ViewMode.GRID) },
                    selectedColor = Color(0xFF7209B7),
                    unselectedColor = Color.Transparent
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedSelectorButton(
                    text = "แบบรายการ",
                    isSelected = currentMode == ViewMode.LIST,
                    onClick = { onModeSelected(ViewMode.LIST) },
                    selectedColor = Color(0xFF7209B7),
                    unselectedColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun AnimatedSelectorButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color
) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectorScale"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(150)
            isClicked = false
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) selectedColor else unselectedColor)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickableNoRipple {
                isClicked = true
                onClick()
            }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else KidTheme.TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun GridView(items: List<MenuItem>, onItemClick: (MenuItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            AnimatedGridItem(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun ListView(items: List<MenuItem>, onItemClick: (MenuItem) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            AnimatedListItem(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun AnimatedGridItem(item: MenuItem, onClick: () -> Unit) {
    var isClicked by remember { mutableStateOf(false) }

    val cardColor = when (item.targetActivity) {
        4 -> ActivityColorPalette[(item.id - 1) % ActivityColorPalette.size]
        else -> KidsColorPalette[(item.id - 1) % KidsColorPalette.size]
    }

    val darkerColor = Color(
        red = (cardColor.red * 0.8f).coerceIn(0f, 1f),
        green = (cardColor.green * 0.8f).coerceIn(0f, 1f),
        blue = (cardColor.blue * 0.8f).coerceIn(0f, 1f)
    )

    val scale by animateFloatAsState(
        targetValue = if (isClicked) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gridScale"
    )

    val bounceScale = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "gridItemFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            bounceScale.animateTo(
                0.9f,
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            bounceScale.animateTo(1f)
            isClicked = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale * bounceScale.value
                scaleY = scale * bounceScale.value
                translationY = floatOffset
                rotationZ = if (isClicked) 2f else 0f
            }
            .then(
                if (item.isLive) Modifier.border(
                    width = 4.dp,
                    brush = Brush.sweepGradient(
                        listOf(Color.Red, Color.White, Color.Blue, Color.Red)
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) else Modifier
            )
            .clickableNoRipple {
                isClicked = true
                onClick()
            },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isClicked) 12.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isClicked) darkerColor else cardColor
        ),
        border = BorderStroke(4.dp, Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (item.isLive) {
                    KidsTvThailandLogo(
                        modifier = Modifier.fillMaxSize().background(KidTheme.TextDark)
                    )
                    LiveBadge(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    )
                } else {
                    PulsingImage(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                }
            }

            Text(
                text = item.title,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimatedListItem(item: MenuItem, onClick: () -> Unit) {
    var isClicked by remember { mutableStateOf(false) }

    val cardColor = when (item.targetActivity) {
        4 -> ActivityColorPalette[(item.id - 1) % ActivityColorPalette.size]
        else -> KidsColorPalette[(item.id - 1) % KidsColorPalette.size]
    }

    val darkerColor = Color(
        red = (cardColor.red * 0.8f).coerceIn(0f, 1f),
        green = (cardColor.green * 0.8f).coerceIn(0f, 1f),
        blue = (cardColor.blue * 0.8f).coerceIn(0f, 1f)
    )

    val scale by animateFloatAsState(
        targetValue = if (isClicked) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "listScale"
    )

    val slideOffset = remember { Animatable(100f) }
    val pressOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        slideOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            pressOffset.animateTo(4f, tween(100))
            pressOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
            isClicked = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = slideOffset.value + pressOffset.value
                translationY = pressOffset.value * 0.5f
            }
            .then(
                if (item.isLive) Modifier.border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(Color.Red, Color.White, Color.Blue)),
                    shape = RoundedCornerShape(32.dp)
                ) else Modifier
            )
            .clickableNoRipple {
                isClicked = true
                onClick()
            },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isClicked) 12.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isClicked) darkerColor else cardColor
        ),
        border = BorderStroke(4.dp, Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (item.isLive) {
                    KidsTvThailandLogo(
                        modifier = Modifier.fillMaxSize().background(KidTheme.TextDark)
                    )
                    LiveBadge(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                } else {
                    FadeInImage(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                }
            }

            Text(
                text = item.title,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PulsingImage(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier.background(
            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent))
        )
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun FadeInImage(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    val alphaValue by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "fadeIn"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier = modifier.background(
            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent))
        )
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .graphicsLayer { this.alpha = alphaValue },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun KidsTvThailandLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoRotation"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LogoBlock(char = "K", color = Color(0xFF0055AA))
                LogoBlock(char = "i", color = Color(0xFF0055AA))
                LogoBlock(char = "d", color = Color(0xFF0055AA))
                LogoBlock(char = "s", color = Color(0xFF0055AA))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LogoBlock("t", Color.White, Color(0xFF003388), 45.dp)
                LogoBlock("v", Color.White, Color(0xFF003388), 45.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color.White, Color(0xFFE0E0E0)))
                    )
                    .border(2.dp, Color(0xFF003388), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "CHANNEL Thailand",
                    color = Color(0xFF003388),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun LogoBlock(
    char: String,
    color: Color,
    bgColor: Color = Color.White,
    size: Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                shadowElevation = 8f
                shape = RoundedCornerShape(8.dp)
                clip = true
            }
            .background(Brush.verticalGradient(listOf(Color.Red, bgColor)))
            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            color = color,
            fontSize = (size.value * 0.7f).sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "liveBadge")
    val alphaValue by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )

    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = alphaValue }
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ==================== DATA & NAVIGATION ====================

data class MenuItem(
    val id: Int,
    val title: String,
    val iconRes: Int,
    val targetActivity: Int,
    val isLive: Boolean = false
)

enum class ViewMode { GRID, LIST }

private fun getMenuItems(): List<MenuItem> = listOf(
    MenuItem(1, "ฝึกอ่าน", R.drawable.doing_learn_01, 0),
    MenuItem(2, "ฝึกเขียน ก - ฮ", R.drawable.card_one, 1),
    MenuItem(3, "ฝึกเขียน A - Z", R.drawable.card_two, 2),
    MenuItem(4, "กิจกรรมเสริม", R.drawable.card_three, 3),
    MenuItem(5, "บทความน่ารู้", R.drawable.app, 4),
    MenuItem(6, "แบบทดสอบ", R.drawable.card_three_two, 5),
    MenuItem(7, "ดู DLTV Live", R.drawable.card_four, 6, isLive = true),
    MenuItem(8, "วาดระบายสี", R.drawable.home_color, 7),
    MenuItem(9, "หมวดเกมสนุก", R.drawable.kids_quiz_3d, 8)
)

private fun handleItemClickDirectly(
    context: Context,
    item: MenuItem,
    navController: NavController?
) {
    when (item.targetActivity) {
        0 -> context.startActivity(Intent(context, LeaningActvity::class.java))
        1 -> context.startActivity(
            Intent(context, MenuThaiLettersActivity::class.java).apply {
                putExtra(MenuThaiLettersActivity.EXTRA_MODE, "THAI")
            }
        )
        2 -> context.startActivity(
            Intent(context, MenuThaiLettersActivity::class.java).apply {
                putExtra(MenuThaiLettersActivity.EXTRA_MODE, "ENGLISH")
            }
        )
        3 -> showKidsActivitiesDialog(context)
        4 -> context.startActivity(
            Intent(context, KidsWebViewActivity::class.java).apply {
                putExtra("extra_url", "https://www.youngciety.com/")
                putExtra("extra_title", "Youngciety - สนุกสนาน")
            }
        )
        5 -> context.startActivity(Intent(context, QuizActivity::class.java))
        6 -> context.startActivity(Intent(context, LiveListActivity::class.java))
        7 -> context.startActivity(Intent(context, ColoringGameActivity::class.java))
        8 -> context.startActivity(Intent(context, GameHubActivity::class.java))
    }
}

private fun showKidsActivitiesDialog(context: Context) {
    val activities = listOf(
        "🎮 ศูนย์รวมเกมเด็ก (เก็บคะแนน)" to { context.startActivity(Intent(context, GameHubActivity::class.java)) },
        "🎨 ระบายสีสร้างสรรค์" to { startColoringActivity(context) },
        "🔤 เกมจับคู่ตัวอักษร" to { startAbcMatchGame(context) }
    )

    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(50, 30, 50, 30)
    }

    val titleText = TextView(context).apply {
        text = "เลือกกิจกรรมที่สนใจ:"
        textSize = 18f
        setTextColor(android.graphics.Color.BLACK)
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, 20)
    }
    layout.addView(titleText)

    activities.forEach { (activityName, onClick) ->
        val button = android.widget.Button(context).apply {
            text = activityName
            textSize = 16f
            setBackgroundColor(getRandomColor())
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            setOnClickListener { onClick() }
        }
        layout.addView(button)
    }

    AlertDialog.Builder(context)
        .setTitle("🎪 กิจกรรมเสริมสำหรับเด็ก")
        .setView(layout)
        .setPositiveButton("ปิด", null)
        .show()
}

private fun startColoringActivity(context: Context) {
    context.startActivity(Intent(context, ColoringGameActivity::class.java))
}

private fun startAbcMatchGame(context: Context) {
    context.startActivity(Intent(context, AbcMatchGameActivity::class.java))
}

private fun getRandomColor(): Int = listOf(
    android.graphics.Color.parseColor("#FF6B6B"),
    android.graphics.Color.parseColor("#4ECDC4"),
    android.graphics.Color.parseColor("#FFD166"),
    android.graphics.Color.parseColor("#06D6A0"),
    android.graphics.Color.parseColor("#118AB2"),
    android.graphics.Color.parseColor("#7209B7")
).random()

// ==================== PREVIEW ====================

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun PreviewMainComposeScreen() {
    MainComposeScreen()
}