package com.pongploydev.priyankvex.paintapp.compose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.priyankvex.paintapp.R

enum class LanguageHubCategory {
    PORTAL,
    THAI,
    ENGLISH
}

@Composable
fun LanguagePortalComposeScreen(
    onSelectThaiHub: () -> Unit,
    onSelectEnglishHub: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8E1), // Soft Warm Cream
                        Color(0xFFFCE4EC), // Soft Pastel Pink
                        Color(0xFFE0F7FA)  // Soft Sky Mint
                    )
                )
            )
    ) {
        KidBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── TOP HEADER WITH STATUS BAR INSETS PADDING ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF5722),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF5722),
                                    Color(0xFFFF8A65),
                                    Color(0xFFFFB74D)
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "กลับ",
                                tint = Color(0xFF333333)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🌟 ศูนย์การเรียนรู้สำหรับเด็ก",
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.35f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 3f
                                )
                            )
                        )
                        Text(
                            text = "เลือกห้องเรียนภาษาไทย หรือ ภาษาอังกฤษ 🎨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFF3E0)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(width = 46.dp, height = 38.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.thai_header_mascot),
                            contentDescription = "Mascot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── INSTRUCTION TITLE ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "แตะเลือกห้องเรียนที่ต้องการ 🚀",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF37474F),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "มีเสียงอ่านคำบรรยาย พร้อมมินิเกมสุ่มทายแสนสนุก",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF78909C),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 2 MAIN HUB CARDS ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 🇹🇭 1. THAI LANGUAGE HUB CARD
                LanguageHubPortalCard(
                    flagEmoji = "🇹🇭",
                    title = "ห้องเรียนภาษาไทย",
                    subtitle = "เรียนรู้พยัญชนะ ก-ฮ, สระ, ไตรยางศ์ และเลขไทย",
                    gradientColors = listOf(
                        Color(0xFFFF5252), // Vibrant Coral Red
                        Color(0xFFFF7A00)  // Golden Orange
                    ),
                    previewChips = listOf("ก - ฮ (๔๔ ตัว)", "สระและวรรณยุกต์", "อักษร ๓ หมู่ (ไตรยางศ์)", "เลขไทย ๑ - ๑๐"),
                    mascotDrawableRes = R.drawable.thai_card_1,
                    cardBorderColor = Color(0xFFFF5252),
                    badgeColor = Color(0xFFFFEBEE),
                    badgeTextColor = Color(0xFFC62828),
                    onClick = onSelectThaiHub
                )

                // 🇬🇧 2. ENGLISH LANGUAGE HUB CARD
                LanguageHubPortalCard(
                    flagEmoji = "🇬🇧",
                    title = "English Kids Hub",
                    subtitle = "Learn ABC, Numbers, Animals & Fruits with Fun",
                    gradientColors = listOf(
                        Color(0xFF00B0FF), // Sky Blue
                        Color(0xFF00E676)  // Fresh Emerald Green
                    ),
                    previewChips = listOf("Alphabet A - Z", "Numbers 1 - 10", "Animals & Sounds", "Fruits & Veggies"),
                    mascotDrawableRes = R.drawable.elephant,
                    cardBorderColor = Color(0xFF00B0FF),
                    badgeColor = Color(0xFFE1F5FE),
                    badgeTextColor = Color(0xFF0277BD),
                    onClick = onSelectEnglishHub
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── BOTTOM FEATURE BADGE ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ระบบเสียงอ่าน ๒ ภาษา • มินิเกมสุ่มทายสะสมดาว ⭐",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageHubPortalCard(
    flagEmoji: String,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    previewChips: List<String>,
    mascotDrawableRes: Int,
    cardBorderColor: Color,
    badgeColor: Color,
    badgeTextColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portalPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, cardBorderColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = flagEmoji,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1.5f, 1.5f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "เข้าสู่หมวดนี้",
                                tint = gradientColors.first(),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeColor,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = mascotDrawableRes),
                            contentDescription = title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF455A64),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        previewChips.forEach { chipText ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor,
                                border = BorderStroke(0.8.dp, cardBorderColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = chipText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
