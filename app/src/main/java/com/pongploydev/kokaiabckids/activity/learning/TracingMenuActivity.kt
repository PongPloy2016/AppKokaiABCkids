package com.pongploydev.kokaiabckids.activity.learning

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.model.ThaiAlphabetRepository
import com.pongploydev.kokaiabckids.utils.MusicManager
import com.pongploydev.kokaiabckids.utils.ScreenUtils
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TracingMenuActivity : AppCompatActivity() {

    enum class MenuGroup {
        LANGUAGE,
        VOCABULARY
    }

    sealed class CardVisual {
        data class DrawableIcon(val resId: Int) : CardVisual()
        data class EmblemBadge(
            val mainSymbol: String,
            val pillLabel: String,
            val circleColor: Color,
            val symbolColor: Color,
            val pillColor: Color,
            val symbolSizeSp: Int = 38
        ) : CardVisual()
    }

    data class TracingMenuCategory(
        val title: String,
        val subtitle: String,
        val group: MenuGroup,
        val visual: CardVisual,
        val accentColor: Color,
        val stageBgColor: Color,
        val categoryMode: String,
        val categoryPosition: Int = 1
    )

    private var soundPool: SoundPool? = null
    private var soundClickId: Int = 0
    private var isSoundLoaded: Boolean = false

    private val categories: List<TracingMenuCategory> by lazy {
        listOf(
            TracingMenuCategory(
                title = "พยัญชนะ ก - ฮ",
                subtitle = "${ThaiAlphabetRepository.getThaiAlphabetList().size} ตัวอักษร",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.EmblemBadge(
                    mainSymbol = "ก",
                    pillLabel = "ก - ฮ",
                    circleColor = Color(0xFFFFE4E6),
                    symbolColor = Color(0xFFE11D48),
                    pillColor = Color(0xFFEF4444),
                    symbolSizeSp = 44
                ),
                accentColor = Color(0xFFEF4444),
                stageBgColor = Color(0xFFFFF5F5),
                categoryMode = "THAI",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "ตัวอักษร A - Z",
                subtitle = "${ThaiAlphabetRepository.getEnglishAlphabetList().size} ตัวอักษร",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.DrawableIcon(R.drawable.ic_svg_alphabet_abc_two),
                accentColor = Color(0xFF0EA5E9),
                stageBgColor = Color(0xFFF0F9FF),
                categoryMode = "ENGLISH",
                categoryPosition = 0
            ),
            TracingMenuCategory(
                title = "ตัวเลข 1 - 10",
                subtitle = "${ThaiAlphabetRepository.getEnglishNumbersList().size} ตัวเลข",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.DrawableIcon(R.drawable.home_number),
                accentColor = Color(0xFFF59E0B),
                stageBgColor = Color(0xFFFFFBEB),
                categoryMode = "NUMBERS",
                categoryPosition = 0
            ),
            TracingMenuCategory(
                title = "เลขไทย ๑ - ๑๐",
                subtitle = "${ThaiAlphabetRepository.getNumbersList().size} ตัวเลข",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.EmblemBadge(
                    mainSymbol = "๑๒๓",
                    pillLabel = "เลขไทย",
                    circleColor = Color(0xFFD1FAE5),
                    symbolColor = Color(0xFF047857),
                    pillColor = Color(0xFF10B981),
                    symbolSizeSp = 28
                ),
                accentColor = Color(0xFF10B981),
                stageBgColor = Color(0xFFECFDF5),
                categoryMode = "NUMBERS",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "สระภาษาไทย",
                subtitle = "${ThaiAlphabetRepository.getThaiVowelList().size} สระและวรรณยุกต์",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.EmblemBadge(
                    mainSymbol = "อะ อา",
                    pillLabel = "สระไทย",
                    circleColor = Color(0xFFE0F2FE),
                    symbolColor = Color(0xFF0369A1),
                    pillColor = Color(0xFF0284C7),
                    symbolSizeSp = 24
                ),
                accentColor = Color(0xFF0284C7),
                stageBgColor = Color(0xFFF0F9FF),
                categoryMode = "THAI_VOWELS",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "อักษร ๓ หมู่",
                subtitle = "${ThaiAlphabetRepository.getToneGroupsList().size} ตัวอักษร",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.EmblemBadge(
                    mainSymbol = "ก ข ค",
                    pillLabel = "ไตรยางศ์",
                    circleColor = Color(0xFFEDE9FE),
                    symbolColor = Color(0xFF6D28D9),
                    pillColor = Color(0xFF7C3AED),
                    symbolSizeSp = 24
                ),
                accentColor = Color(0xFF7C3AED),
                stageBgColor = Color(0xFFF5F3FF),
                categoryMode = "TONE_GROUPS",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "ฝึกสะกดคำ",
                subtitle = "${ThaiAlphabetRepository.getSpellingList().size} คำศัพท์",
                group = MenuGroup.LANGUAGE,
                visual = CardVisual.EmblemBadge(
                    mainSymbol = "ก+า",
                    pillLabel = "สะกดคำ",
                    circleColor = Color(0xFFFFEDD5),
                    symbolColor = Color(0xFFC2410C),
                    pillColor = Color(0xFFF97316),
                    symbolSizeSp = 28
                ),
                accentColor = Color(0xFFF97316),
                stageBgColor = Color(0xFFFFF7ED),
                categoryMode = "SPELLING",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "สีและวันทั้ง 7",
                subtitle = "${ThaiAlphabetRepository.getColorsAndDaysList().size} รายการ",
                group = MenuGroup.VOCABULARY,
                visual = CardVisual.DrawableIcon(R.drawable.home_color),
                accentColor = Color(0xFFEC4899),
                stageBgColor = Color(0xFFFDF2F8),
                categoryMode = "COLORS_DAYS",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "สัตว์น่ารัก",
                subtitle = "${ThaiAlphabetRepository.getAnimalsList().size} ชนิด",
                group = MenuGroup.VOCABULARY,
                visual = CardVisual.DrawableIcon(R.drawable.home_animal),
                accentColor = Color(0xFF16A34A),
                stageBgColor = Color(0xFFF0FDF4),
                categoryMode = "ANIMALS",
                categoryPosition = 0
            ),
            TracingMenuCategory(
                title = "ผลไม้และผัก",
                subtitle = "${ThaiAlphabetRepository.getFruitsList().size} ชนิด",
                group = MenuGroup.VOCABULARY,
                visual = CardVisual.DrawableIcon(R.drawable.home_fruits),
                accentColor = Color(0xFFE11D48),
                stageBgColor = Color(0xFFFFF1F2),
                categoryMode = "FRUITS",
                categoryPosition = 0
            ),
            TracingMenuCategory(
                title = "ยานพาหนะ",
                subtitle = "${ThaiAlphabetRepository.getVehiclesList().size} ชนิด",
                group = MenuGroup.VOCABULARY,
                visual = CardVisual.DrawableIcon(R.drawable.home_vehicle),
                accentColor = Color(0xFF0891B2),
                stageBgColor = Color(0xFFECFEFF),
                categoryMode = "VEHICLES",
                categoryPosition = 1
            ),
            TracingMenuCategory(
                title = "ส่วนของร่างกาย",
                subtitle = "${ThaiAlphabetRepository.getBodyPartsList().size} คำศัพท์",
                group = MenuGroup.VOCABULARY,
                visual = CardVisual.DrawableIcon(R.drawable.home_body_parts),
                accentColor = Color(0xFF0D9488),
                stageBgColor = Color(0xFFF0FDFA),
                categoryMode = "BODY_PARTS",
                categoryPosition = 1
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        window.statusBarColor = "#D8F1FF".toColorInt()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        initSounds()

        val spanCount = ScreenUtils.getGridSpanCount(this).coerceAtLeast(2)

        setContent {
            MaterialTheme {
                TracingMenuScreen(
                    categories = categories,
                    spanCount = spanCount,
                    onBackClick = {
                        playClickSound()
                        finish()
                    },
                    onTabSound = { playClickSound() },
                    onCategoryClick = { category ->
                        playClickSound()
                        openTracingCategory(category)
                    },
                    onRandomClick = {
                        playClickSound()
                        if (categories.isNotEmpty()) {
                            val randomCategory = categories[Random.nextInt(categories.size)]
                            openTracingCategory(randomCategory)
                        }
                    }
                )
            }
        }
    }

    private fun initSounds() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()
            soundClickId = soundPool?.load(this, R.raw.button, 1) ?: 0
            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) isSoundLoaded = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playClickSound() {
        if (isSoundLoaded) {
            soundPool?.play(soundClickId, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun openTracingCategory(category: TracingMenuCategory) {
        val intent = Intent(this, FunTracingActivity::class.java).apply {
            putExtra("categoryMode", category.categoryMode)
            putExtra("categoryPosition", category.categoryPosition)
            putExtra("selectedPosition", 0)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        MusicManager.getInstance().startMusic(this)
    }

    override fun onPause() {
        super.onPause()
        MusicManager.getInstance().pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }
}

/**
 * Playful animated children's storybook backdrop:
 * soft sunny sky, gently rotating sunbeams, drifting fluffy clouds,
 * twinkling pastel stars, floating bubbles, and layered green meadow hills.
 */
@Composable
private fun KidsStorybookBackdrop(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "kidsBackdrop")

    val cloudDrift1 by infinite.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud1"
    )
    val cloudDrift2 by infinite.animateFloat(
        initialValue = 1.15f,
        targetValue = -0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 46000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud2"
    )
    val sunAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunAngle"
    )
    val twinkle by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    val floatBob by infinite.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatBob"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Warm pastel sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD8F1FF),
                    Color(0xFFEBF7FF),
                    Color(0xFFFFFBF0),
                    Color(0xFFEAF9EC)
                )
            )
        )

        // 2. Cheerful sun with rotating soft sunbeams (top-right)
        val sunCenter = Offset(w * 0.88f, h * 0.11f)
        val sunRadius = 30.dp.toPx()
        drawCircle(
            color = Color(0xFFFFF3B0).copy(alpha = 0.55f),
            radius = sunRadius * 1.55f,
            center = sunCenter
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = sunRadius,
            center = sunCenter
        )
        val rayCount = 10
        val rayInner = sunRadius * 1.2f
        val rayOuter = sunRadius * 1.65f
        for (i in 0 until rayCount) {
            val rad = ((sunAngle + i * (360f / rayCount)) * PI / 180.0)
            val start = Offset(
                sunCenter.x + (cos(rad) * rayInner).toFloat(),
                sunCenter.y + (sin(rad) * rayInner).toFloat()
            )
            val end = Offset(
                sunCenter.x + (cos(rad) * rayOuter).toFloat(),
                sunCenter.y + (sin(rad) * rayOuter).toFloat()
            )
            drawLine(
                color = Color(0xFFFFCA28).copy(alpha = 0.65f),
                start = start,
                end = end,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 3. Soft pastel rainbow arc peeking in upper-left sky
        val arcCenter = Offset(w * 0.15f, h * 0.23f)
        val rainbowColors = listOf(
            Color(0xFFFFCDD2),
            Color(0xFFFFE0B2),
            Color(0xFFFFF9C4),
            Color(0xFFC8E6C9),
            Color(0xFFBBDEFB)
        )
        rainbowColors.forEachIndexed { idx, col ->
            val r = (128 - idx * 9).dp.toPx()
            drawCircle(
                color = col.copy(alpha = 0.28f),
                radius = r,
                center = arcCenter,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 7.dp.toPx())
            )
        }

        // 4. Drifting fluffy clouds
        drawFluffyCloud(
            center = Offset(w * cloudDrift1, h * 0.13f),
            scale = 1.0f,
            alpha = 0.85f
        )
        drawFluffyCloud(
            center = Offset(w * cloudDrift2, h * 0.25f),
            scale = 0.78f,
            alpha = 0.72f
        )
        drawFluffyCloud(
            center = Offset(w * ((cloudDrift1 + 0.5f) % 1.3f - 0.15f), h * 0.48f),
            scale = 0.65f,
            alpha = 0.55f
        )

        // 5. Playful twinkling stars & floating pastel bubbles
        drawFourPointStar(
            center = Offset(w * 0.14f, h * 0.18f + floatBob),
            radius = 7.dp.toPx(),
            color = Color(0xFFFFB74D).copy(alpha = twinkle * 0.8f)
        )
        drawFourPointStar(
            center = Offset(w * 0.62f, h * 0.16f - floatBob * 0.7f),
            radius = 5.5.dp.toPx(),
            color = Color(0xFF38BDF8).copy(alpha = (1.2f - twinkle).coerceIn(0.3f, 0.85f))
        )
        drawFourPointStar(
            center = Offset(w * 0.90f, h * 0.36f + floatBob * 0.5f),
            radius = 6.dp.toPx(),
            color = Color(0xFFF472B6).copy(alpha = twinkle * 0.7f)
        )

        // Floating pastel bubbles
        drawCircle(
            color = Color(0xFF93C5FD).copy(alpha = 0.22f),
            radius = 12.dp.toPx(),
            center = Offset(w * 0.08f, h * 0.62f + floatBob)
        )
        drawCircle(
            color = Color(0xFFFDA4AF).copy(alpha = 0.22f),
            radius = 9.dp.toPx(),
            center = Offset(w * 0.92f, h * 0.68f - floatBob)
        )

        // 6. Soft layered meadow hills at the bottom
        val backHill = Path().apply {
            moveTo(0f, h * 0.90f)
            quadraticTo(w * 0.28f, h * 0.82f, w * 0.58f, h * 0.89f)
            quadraticTo(w * 0.82f, h * 0.94f, w, h * 0.86f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(backHill, color = Color(0xFFC8E6C9).copy(alpha = 0.75f))

        val frontHill = Path().apply {
            moveTo(0f, h * 0.93f)
            quadraticTo(w * 0.25f, h * 0.97f, w * 0.52f, h * 0.92f)
            quadraticTo(w * 0.78f, h * 0.87f, w, h * 0.93f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(frontHill, color = Color(0xFFA5D6A7).copy(alpha = 0.70f))
    }
}

private fun DrawScope.drawFluffyCloud(center: Offset, scale: Float, alpha: Float) {
    val c = Color.White.copy(alpha = alpha)
    val r = 20.dp.toPx() * scale
    drawCircle(color = c, radius = r * 1.15f, center = center)
    drawCircle(color = c, radius = r * 0.9f, center = Offset(center.x - r * 1.0f, center.y + r * 0.22f))
    drawCircle(color = c, radius = r * 0.85f, center = Offset(center.x + r * 1.05f, center.y + r * 0.24f))
    drawCircle(color = c, radius = r * 0.72f, center = Offset(center.x + r * 0.35f, center.y - r * 0.45f))
}

private fun DrawScope.drawFourPointStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.28f, center.y - radius * 0.28f)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + radius * 0.28f, center.y + radius * 0.28f)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.28f, center.y + radius * 0.28f)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - radius * 0.28f, center.y - radius * 0.28f)
        close()
    }
    drawPath(path, color = color)
}

@Composable
private fun TracingMenuScreen(
    categories: List<TracingMenuActivity.TracingMenuCategory>,
    spanCount: Int,
    onBackClick: () -> Unit,
    onTabSound: () -> Unit,
    onCategoryClick: (TracingMenuActivity.TracingMenuCategory) -> Unit,
    onRandomClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ทั้งหมด", "อักษรและตัวเลข", "คำศัพท์รอบตัว")

    val filteredCategories = remember(selectedTab, categories) {
        when (selectedTab) {
            1 -> categories.filter { it.group == TracingMenuActivity.MenuGroup.LANGUAGE }
            2 -> categories.filter { it.group == TracingMenuActivity.MenuGroup.VOCABULARY }
            else -> categories
        }
    }

    // Gentle floating animation for the pencil icon in header
    val infinite = rememberInfiniteTransition(label = "headerPencil")
    val pencilTilt by infinite.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pencilTilt"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated Kids Storybook Background
        KidsStorybookBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Kids Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncyCircleButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "ย้อนกลับ",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Playful animated pencil icon badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF3C4),
                    border = BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Create,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier
                                .size(21.dp)
                                .graphicsLayer { rotationZ = pencilTilt }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "เลือกหมวดฝึกเขียน",
                        color = Color(0xFF0F172A),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "มาสนุกกับการคัดลายมือกันเถอะ",
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bouncy Random Pick Pill Button
                BouncyRandomButton(onClick = onRandomClick)
            }

            // Playful Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabColors = listOf(
                    Color(0xFF0284C7),
                    Color(0xFFEF4444),
                    Color(0xFF10B981)
                )
                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    val activeColor = tabColors[index % tabColors.size]
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) activeColor else Color.White.copy(alpha = 0.92f),
                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable {
                            if (selectedTab != index) {
                                onTabSound()
                                selectedTab = index
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Uniform Playful 2-Column Card Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(spanCount),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 28.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(
                    items = filteredCategories,
                    key = { _, it -> "${it.categoryMode}_${it.categoryPosition}_${it.title}" }
                ) { index, category ->
                    TracingCategoryCard(
                        category = category,
                        index = index,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BouncyCircleButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "circleBtnScale"
    )

    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun BouncyRandomButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "randomBtnScale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFF5E7E),
        shadowElevation = 3.dp,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "สุ่มหมวด",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "สุ่มหมวด",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TracingCategoryCard(
    category: TracingMenuActivity.TracingMenuCategory,
    index: Int,
    onClick: () -> Unit
) {
    // Staggered pop-in entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 40L).coerceAtMost(280L))
        visible = true
    }

    // Bouncy spring press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardPressScale"
    )

    // Gentle floating visual bobbing
    val infinite = rememberInfiniteTransition(label = "cardFloat_$index")
    val bobOffsetY by infinite.animateFloat(
        initialValue = if (index % 2 == 0) -2.5f else 2.5f,
        targetValue = if (index % 2 == 0) 2.5f else -2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200 + (index % 3) * 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobY"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(240)) + scaleIn(
            initialScale = 0.88f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    ) {
        // Chunky playful 3D bottom lip container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clip(RoundedCornerShape(24.dp))
                .background(category.accentColor.copy(alpha = 0.28f))
                .padding(bottom = 4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, category.accentColor.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // Top Playful Visual Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(126.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(category.stageBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        // Subtle notebook lines + decorative corner dots
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val lineColor = category.accentColor.copy(alpha = 0.09f)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            val step = size.height / 4f
                            for (i in 1..3) {
                                val y = step * i
                                drawLine(
                                    color = lineColor,
                                    start = Offset(12.dp.toPx(), y),
                                    end = Offset(size.width - 12.dp.toPx(), y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = if (i == 2) dashEffect else null
                                )
                            }
                            // Playful pastel corner circles
                            drawCircle(
                                color = category.accentColor.copy(alpha = 0.12f),
                                radius = 14.dp.toPx(),
                                center = Offset(size.width - 6.dp.toPx(), 6.dp.toPx())
                            )
                            drawCircle(
                                color = category.accentColor.copy(alpha = 0.10f),
                                radius = 10.dp.toPx(),
                                center = Offset(8.dp.toPx(), size.height - 6.dp.toPx())
                            )
                        }

                        // Tiny sparkle accent in top-right of stage
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = category.accentColor.copy(alpha = 0.35f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(15.dp)
                        )

                        // Floating Icon or Emblem
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = bobOffsetY.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (val visual = category.visual) {
                                is TracingMenuActivity.CardVisual.DrawableIcon -> {
                                    Image(
                                        painter = painterResource(id = visual.resId),
                                        contentDescription = category.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp)
                                    )
                                }
                                is TracingMenuActivity.CardVisual.EmblemBadge -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(visual.circleColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = visual.mainSymbol,
                                                color = visual.symbolColor,
                                                fontSize = visual.symbolSizeSp.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = visual.pillColor
                                        ) {
                                            Text(
                                                text = visual.pillLabel,
                                                color = Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer: Title + Item Count + Colored Arrow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.title,
                                color = Color(0xFF0F172A),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = category.subtitle,
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(category.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
