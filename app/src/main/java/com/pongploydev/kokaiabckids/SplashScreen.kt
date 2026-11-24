package com.pongploydev.kokaiabckids

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.kokaiabckids.activity.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SplashScreen : ComponentActivity() {

    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )

        setContent {
            KidsStorybookSplashScreen(
                onFinishSplash = { navigateToMain() }
            )
        }
    }

    private fun navigateToMain() {
        if (navigated || isFinishing) return
        navigated = true
        startActivity(Intent(this, MainActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

@Composable
private fun KidsStorybookSplashScreen(
    onFinishSplash: () -> Unit
) {
    val mascotScale = remember { Animatable(0.55f) }
    val mascotAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(24f) }
    val progressAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splashLoop")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudDrift"
    )

    LaunchedEffect(Unit) {
        launch {
            mascotAlpha.animateTo(1f, tween(380))
        }
        launch {
            mascotScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(180)
            launch { titleAlpha.animateTo(1f, tween(450)) }
            launch { titleOffsetY.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
        }
        launch {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing)
            )
        }
        delay(2600)
        onFinishSplash()
    }

    val mascotFloatY = sin(wavePhase.toDouble()).toFloat() * 8f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A4EFF),
                        Color(0xFF8C72FF),
                        Color(0xFFDCEBFF),
                        Color(0xFFFFF8EC)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinishSplash
            )
    ) {
        // Pastel rainbow, drifting clouds, and twinkling stars backdrop
        SplashBackdropCanvas(
            wavePhase = wavePhase,
            cloudDrift = cloudDrift,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top welcome badge
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .graphicsLayer { alpha = titleAlpha.value }
            ) {
                Text(
                    text = "🌈 ยินดีต้อนรับสู่โลกแห่งการเรียนรู้",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }

            // Center hero: Glowing rings + Boy & Girl Mascot + Orbiting Learning Tiles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(308.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer breathing halo
                    Box(
                        modifier = Modifier
                            .size(258.dp)
                            .graphicsLayer {
                                scaleX = haloPulse
                                scaleY = haloPulse
                                alpha = mascotAlpha.value * 0.35f
                            }
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    // Inner soft glow ring
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .graphicsLayer {
                                scaleX = mascotScale.value
                                scaleY = mascotScale.value
                                translationY = mascotFloatY
                                alpha = mascotAlpha.value * 0.55f
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFF3B0),
                                        Color.White.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )

                    // Mascot portrait (upright, clean circle)
                    Surface(
                        modifier = Modifier
                            .size(210.dp)
                            .graphicsLayer {
                                scaleX = mascotScale.value
                                scaleY = mascotScale.value
                                translationY = mascotFloatY
                                alpha = mascotAlpha.value
                            },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(5.dp, Color.White),
                        shadowElevation = 16.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kids_splash_mascot),
                            contentDescription = "ก ไก่ ABC หรรษา",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    // Top-left floating tile: ก
                    FloatingSplashTile(
                        text = "ก",
                        containerColor = Color(0xFFFF6B8B),
                        rotationDeg = -10f,
                        floatY = sin((wavePhase + 0.6f).toDouble()).toFloat() * 7f,
                        scale = mascotScale.value,
                        alpha = mascotAlpha.value,
                        size = 54.dp,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 14.dp, y = 16.dp)
                    )

                    // Top-right floating tile: ABC
                    FloatingSplashTile(
                        text = "ABC",
                        containerColor = Color(0xFF10B981),
                        rotationDeg = 10f,
                        floatY = sin((wavePhase + 2.0f).toDouble()).toFloat() * 7f,
                        scale = mascotScale.value,
                        alpha = mascotAlpha.value,
                        size = 56.dp,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 20.dp)
                    )

                    // Bottom-left floating tile: 123
                    FloatingSplashTile(
                        text = "123",
                        containerColor = Color(0xFFFFB703),
                        rotationDeg = 8f,
                        floatY = sin((wavePhase + 3.4f).toDouble()).toFloat() * 7f,
                        scale = mascotScale.value,
                        alpha = mascotAlpha.value,
                        size = 54.dp,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 12.dp, y = (-20).dp)
                    )

                    // Bottom-right floating tile: Art palette
                    FloatingSplashTile(
                        text = "🎨",
                        containerColor = Color(0xFF38BDF8),
                        rotationDeg = -8f,
                        floatY = sin((wavePhase + 4.8f).toDouble()).toFloat() * 7f,
                        scale = mascotScale.value,
                        alpha = mascotAlpha.value,
                        size = 54.dp,
                        fontSize = 23.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-12).dp, y = (-16).dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Title + Subtitle Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffsetY.value
                    }
                ) {
                    Text(
                        text = "ก ไก่ ABC หรรษา",
                        color = Color(0xFF33207A),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF6A4EFF),
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = "✨ ฝึกเขียน • ฝึกอ่าน • ระบายสี • เกมสนุก ✨",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Bottom loading bar + interactive prompt
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .graphicsLayer { alpha = titleAlpha.value }
            ) {
                // Rainbow progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.74f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE7E2FF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim.value.coerceIn(0.06f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B8B),
                                        Color(0xFFFFB703),
                                        Color(0xFF10B981),
                                        Color(0xFF6A4EFF)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🚀 กำลังเตรียมความสนุก...",
                        color = Color(0xFF4A3D82),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(แตะเพื่อเริ่มเลย)",
                        color = Color(0xFF7B61FF),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingSplashTile(
    text: String,
    containerColor: Color,
    rotationDeg: Float,
    floatY: Float,
    scale: Float,
    alpha: Float,
    size: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotationDeg
                translationY = floatY
                this.alpha = alpha
            }
            .shadow(8.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(2.5.dp, Color.White)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SplashBackdropCanvas(
    wavePhase: Float,
    cloudDrift: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft pastel rainbow arc behind mascot
        val rainbowColors = listOf(
            Color(0xFFFF8FAB).copy(alpha = 0.24f),
            Color(0xFFFFD166).copy(alpha = 0.24f),
            Color(0xFF80ED99).copy(alpha = 0.24f),
            Color(0xFF74C0FC).copy(alpha = 0.22f)
        )
        val arcDiameter = w * 1.18f
        rainbowColors.forEachIndexed { index, color ->
            val inset = index * 18.dp.toPx()
            drawArc(
                color = color,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(
                    x = (w - arcDiameter) / 2f + inset,
                    y = h * 0.21f + inset
                ),
                size = Size(arcDiameter - inset * 2f, arcDiameter - inset * 2f),
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Drifting fluffy cloud top-left
        val c1x = w * 0.14f + cloudDrift
        val c1y = h * 0.16f
        val cloudColor = Color.White.copy(alpha = 0.28f)
        drawCircle(cloudColor, radius = 26.dp.toPx(), center = Offset(c1x, c1y))
        drawCircle(cloudColor, radius = 34.dp.toPx(), center = Offset(c1x + 26.dp.toPx(), c1y - 8.dp.toPx()))
        drawCircle(cloudColor, radius = 24.dp.toPx(), center = Offset(c1x + 54.dp.toPx(), c1y))

        // Drifting fluffy cloud top-right
        val c2x = w * 0.72f - cloudDrift * 0.8f
        val c2y = h * 0.22f
        drawCircle(cloudColor, radius = 22.dp.toPx(), center = Offset(c2x, c2y))
        drawCircle(cloudColor, radius = 30.dp.toPx(), center = Offset(c2x + 22.dp.toPx(), c2y - 6.dp.toPx()))
        drawCircle(cloudColor, radius = 20.dp.toPx(), center = Offset(c2x + 46.dp.toPx(), c2y + 2.dp.toPx()))

        // Soft floating pastel bokeh bubbles near bottom
        drawCircle(
            color = Color(0xFFFFD166).copy(alpha = 0.18f),
            radius = 52.dp.toPx(),
            center = Offset(w * 0.1f, h * 0.78f + cos(wavePhase.toDouble()).toFloat() * 8f)
        )
        drawCircle(
            color = Color(0xFFFF8FAB).copy(alpha = 0.16f),
            radius = 64.dp.toPx(),
            center = Offset(w * 0.92f, h * 0.72f + sin(wavePhase.toDouble()).toFloat() * 10f)
        )

        // Twinkling sparkle stars
        val stars = listOf(
            Triple(w * 0.16f, h * 0.29f, 0f),
            Triple(w * 0.86f, h * 0.30f, 1.2f),
            Triple(w * 0.10f, h * 0.56f, 2.3f),
            Triple(w * 0.90f, h * 0.58f, 3.5f),
            Triple(w * 0.25f, h * 0.76f, 4.4f),
            Triple(w * 0.78f, h * 0.79f, 5.1f)
        )
        stars.forEach { (sx, sy, phase) ->
            val pulse = 0.55f + 0.45f * sin((wavePhase + phase).toDouble()).toFloat()
            val radius = (7.dp.toPx()) * pulse
            drawCircle(
                color = Color(0xFFFFE57F).copy(alpha = 0.85f * pulse),
                radius = radius,
                center = Offset(sx, sy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * pulse),
                radius = radius * 0.45f,
                center = Offset(sx, sy)
            )
        }
    }
}

