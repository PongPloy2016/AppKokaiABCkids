package com.pongploydev.kokaiabckids.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Bubble(
    val xFrac: Float, val yFrac: Float, val radius: Float,
    val color: Color, val speedFrac: Float, val phaseOffset: Float
)

private data class StarParticle(
    val xFrac: Float, val yFrac: Float, val size: Float,
    val color: Color, val blinkPhase: Float
)

@Composable
fun KidBackground(modifier: Modifier = Modifier) {
    val bubbles = remember {
        val cols = listOf(
            Color(0x16FFD740), Color(0x1640C4FF), Color(0x16FF6D6D),
            Color(0x1669F0AE), Color(0x16FF80AB), Color(0x16B388FF),
            Color(0x16FFB74D), Color(0x164FC3F7), Color(0x16A5D6A7)
        )
        List(10) { i ->
            Bubble(
                xFrac = ((i * 37 + 11) % 97) / 100f,
                yFrac = ((i * 53 + 7) % 100) / 100f,
                radius = 7f + (i % 4) * 3f,
                color = cols[i % cols.size],
                speedFrac = 0.22f + (i % 3) * 0.12f,
                phaseOffset = (i * 41 % 100) / 100f
            )
        }
    }

    val stars = remember {
        val starCols = listOf(
            Color(0xFFFFD54F), Color(0xFFFFCA28), Color(0xFFFFB74D),
            Color(0xFF80D8FF), Color(0xFFFF80AB)
        )
        List(8) { i ->
            StarParticle(
                xFrac = ((i * 61 + 19) % 95) / 100f,
                yFrac = ((i * 43 + 13) % 90) / 100f,
                size = 3.5f + (i % 3) * 1.5f,
                color = starCols[i % starCols.size],
                blinkPhase = (i * 29 % 100) / 100f
            )
        }
    }

    val inf = rememberInfiniteTransition(label = "bg")
    val floatTick by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "floatTick"
    )
    val blinkTick by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "blinkTick"
    )
    val swayTick by inf.animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing)),
        label = "swayTick"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // พื้นหลังท้องฟ้าพาสเทลสดใสสำหรับเด็ก
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFEAF4FF),
                    Color(0xFFF6F1FF),
                    Color(0xFFFFF8EC),
                    Color(0xFFEEFFF5)
                )
            )
        )

        // สายรุ้งพาสเทลจาง ๆ เพิ่มความสดใสแบบไม่กวนสายตา
        val rainbowCenter = Offset(w * 0.85f, h * 0.42f)
        val rainbowRadius = w * 0.72f
        val rainbowColors = listOf(
            Color(0x22FF758F),
            Color(0x22FFB347),
            Color(0x22FFE066),
            Color(0x226BCB77),
            Color(0x224D96FF)
        )
        rainbowColors.forEachIndexed { idx, col ->
            val r = rainbowRadius - idx * (11f * density)
            if (r > 0f) {
                drawArc(
                    color = col,
                    startAngle = 150f,
                    sweepAngle = 125f,
                    useCenter = false,
                    topLeft = Offset(rainbowCenter.x - r, rainbowCenter.y - r),
                    size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
                    style = Stroke(width = 10f * density, cap = StrokeCap.Round)
                )
            }
        }

        // ก้อนเมฆปุยนุ่มด้านบน/กลาง
        val drift = sin(swayTick) * 8f * density
        val cloudColor = Color.White.copy(alpha = 0.72f)
        // เมฆซ้าย
        val c1x = w * 0.16f + drift
        val c1y = h * 0.34f
        drawCircle(cloudColor, radius = 26f * density, center = Offset(c1x, c1y))
        drawCircle(cloudColor, radius = 20f * density, center = Offset(c1x - 20f * density, c1y + 6f * density))
        drawCircle(cloudColor, radius = 22f * density, center = Offset(c1x + 22f * density, c1y + 5f * density))
        // เมฆขวา
        val c2x = w * 0.82f - drift * 0.8f
        val c2y = h * 0.62f
        drawCircle(cloudColor, radius = 24f * density, center = Offset(c2x, c2y))
        drawCircle(cloudColor, radius = 18f * density, center = Offset(c2x - 19f * density, c2y + 5f * density))
        drawCircle(cloudColor, radius = 19f * density, center = Offset(c2x + 19f * density, c2y + 5f * density))

        // เนินหญ้าและก้อนเมฆด้านล่างนุ่มนวล
        val meadowBack = Path().apply {
            moveTo(0f, h * 0.83f)
            cubicTo(w * 0.25f, h * 0.78f, w * 0.55f, h * 0.88f, w, h * 0.80f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(meadowBack, color = Color(0xFFD6F9E4).copy(alpha = 0.55f))

        val cloudPath = Path().apply {
            moveTo(0f, h * 0.89f)
            cubicTo(w * 0.18f, h * 0.85f, w * 0.38f, h * 0.92f, w * 0.58f, h * 0.88f)
            cubicTo(w * 0.76f, h * 0.85f, w * 0.88f, h * 0.91f, w, h * 0.87f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(cloudPath, color = Color.White.copy(alpha = 0.78f))

        for (bubble in bubbles) {
            val progress = (floatTick + bubble.phaseOffset) % 1f
            val rawY = bubble.yFrac - progress * bubble.speedFrac
            val yNorm = ((rawY % 1f) + 1f) % 1f
            val swayX = sin(swayTick + bubble.phaseOffset * 6.28f) * 0.025f
            val cx = (bubble.xFrac + swayX).coerceIn(0.05f, 0.95f) * w
            val cy = yNorm * h
            val fadeAlpha = if (yNorm < 0.08f) (yNorm / 0.08f) else 1f
            val rad = bubble.radius * 1.45f * density

            drawCircle(
                color = bubble.color.copy(alpha = (bubble.color.alpha * 1.8f).coerceAtMost(0.35f) * fadeAlpha),
                radius = rad,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.38f * fadeAlpha),
                radius = rad,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2f * density)
            )
            // แสงสะท้อนฟองสบู่
            drawCircle(
                color = Color.White.copy(alpha = 0.55f * fadeAlpha),
                radius = rad * 0.26f,
                center = Offset(cx - rad * 0.32f, cy - rad * 0.32f)
            )
        }

        for (star in stars) {
            val blink = sin((blinkTick + star.blinkPhase) * 2f * PI.toFloat())
            val alpha = (blink * 0.35f + 0.45f).coerceIn(0.1f, 0.8f)
            val cx = star.xFrac * w
            val cy = star.yFrac * h * 0.75f
            val r = star.size * density

            // Delicate sparkle star (4-point curved diamond)
            val starPath = Path().apply {
                moveTo(cx, cy - r)
                quadraticBezierTo(cx, cy, cx + r, cy)
                quadraticBezierTo(cx, cy, cx, cy + r)
                quadraticBezierTo(cx, cy, cx - r, cy)
                quadraticBezierTo(cx, cy, cx, cy - r)
                close()
            }
            drawPath(starPath, color = star.color.copy(alpha = alpha * 0.45f))
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = r * 0.22f,
                center = Offset(cx, cy)
            )
        }
    }
}

private data class QuizParticle(
    val emoji: String,
    val xFrac: Float,
    val yFrac: Float,
    val size: Float,
    val speed: Float,
    val phaseOffset: Float
)

@Composable
fun QuizBackground(modifier: Modifier = Modifier) {
    val particles = remember {
        val emojis = listOf("❓", "💡", "✏️", "📚", "🏆", "🎓", "✅", "🔍", "📖", "✨")
        List(15) { i ->
            QuizParticle(
                emoji = emojis[i % emojis.size],
                xFrac = ((i * 47 + 13) % 95) / 100f,
                yFrac = ((i * 71 + 7) % 100) / 100f,
                size = 20f + (i % 5) * 8f,
                speed = 0.2f + (i % 3) * 0.15f,
                phaseOffset = (i * 31 % 100) / 100f
            )
        }
    }

    val inf = rememberInfiniteTransition(label = "quizBg")
    val floatTick by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "floatTick"
    )
    val rotateTick by inf.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "rotateTick"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFF3E5F5),
                        Color(0xFFFFF3E0)
                    )
                )
            )

            val path = Path()
            val w = size.width
            val h = size.height
            for (i in 0..5) {
                val y = h * (0.2f * i)
                path.moveTo(0f, y)
                path.quadraticBezierTo(w * 0.25f, y - 50f, w * 0.5f, y)
                path.quadraticBezierTo(w * 0.75f, y + 50f, w, y)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.15f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        particles.forEach { particle ->
            val progress = (floatTick + particle.phaseOffset) % 1f
            val yPos = ((particle.yFrac - progress * particle.speed) % 1f + 1f) % 1f
            val sway = sin((floatTick + particle.phaseOffset) * 2 * PI.toFloat()) * 20f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = particle.xFrac * size.width + sway
                        translationY = yPos * size.height
                    }
            ) {
                Text(
                    text = particle.emoji,
                    fontSize = particle.size.sp,
                    modifier = Modifier
                        .rotate(rotateTick + particle.phaseOffset * 360)
                        .alpha(0.3f)
                )
            }
        }
    }
}
