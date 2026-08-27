package com.pongploydev.priyankvex.paintapp.compose

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
            Color(0x55FFD740), Color(0x5540C4FF), Color(0x55FF6D6D),
            Color(0x5569F0AE), Color(0x55FF80AB), Color(0x55B388FF),
            Color(0x55FFB74D), Color(0x554FC3F7), Color(0x55A5D6A7)
        )
        List(18) { i ->
            Bubble(
                xFrac = ((i * 37 + 11) % 97) / 100f,
                yFrac = ((i * 53 + 7) % 100) / 100f,
                radius = 14f + (i % 5) * 8f,
                color = cols[i % cols.size],
                speedFrac = 0.3f + (i % 4) * 0.18f,
                phaseOffset = (i * 41 % 100) / 100f
            )
        }
    }

    val stars = remember {
        val starCols = listOf(
            Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFCC02),
            Color(0xFFFFAB40), Color(0xFFFFFFFF)
        )
        List(12) { i ->
            StarParticle(
                xFrac = ((i * 61 + 19) % 95) / 100f,
                yFrac = ((i * 43 + 13) % 90) / 100f,
                size = 5f + (i % 4) * 4f,
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
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "blinkTick"
    )
    val swayTick by inf.animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "swayTick"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF8E1),
                    Color(0xFFE1F5FE),
                    Color(0xFFFCE4EC)
                )
            )
        )

        val cloudPath = Path().apply {
            moveTo(0f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.76f, w * 0.3f, h * 0.86f, w * 0.5f, h * 0.8f)
            cubicTo(w * 0.7f, h * 0.74f, w * 0.85f, h * 0.84f, w, h * 0.79f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(cloudPath, color = Color(0x22FFFFFF))

        for (bubble in bubbles) {
            val progress = (floatTick + bubble.phaseOffset) % 1f
            val rawY = bubble.yFrac - progress * bubble.speedFrac
            val yNorm = ((rawY % 1f) + 1f) % 1f
            val swayX = sin(swayTick + bubble.phaseOffset * 6.28f) * 0.025f
            val cx = (bubble.xFrac + swayX).coerceIn(0.04f, 0.96f) * w
            val cy = yNorm * h
            val fadeAlpha = if (yNorm < 0.08f) (yNorm / 0.08f) else 1f

            drawCircle(
                color = bubble.color.copy(alpha = bubble.color.alpha * fadeAlpha),
                radius = bubble.radius * density,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.25f * fadeAlpha),
                radius = bubble.radius * density,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f * density)
            )
        }

        for (star in stars) {
            val blink = sin((blinkTick + star.blinkPhase) * 2f * PI.toFloat())
            val alpha = (blink * 0.4f + 0.6f).coerceIn(0.1f, 1f)
            val cx = star.xFrac * w
            val cy = star.yFrac * h * 0.75f
            val r = star.size * density

            for (arm in 0 until 4) {
                val angle = arm * (PI / 2).toFloat()
                drawLine(
                    color = star.color.copy(alpha = alpha),
                    start = Offset(cx + cos(angle) * r * 0.25f, cy + sin(angle) * r * 0.25f),
                    end = Offset(cx + cos(angle) * r, cy + sin(angle) * r),
                    strokeWidth = 2f * density,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(
                color = star.color.copy(alpha = alpha),
                radius = r * 0.28f,
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
