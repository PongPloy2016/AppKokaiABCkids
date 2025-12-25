package com.pongploydev.priyankvex.paintapp.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun BannerAdCompose(
    modifier: Modifier = Modifier,
    useRealAd: Boolean = false,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Test ID
) {
//    if (useRealAd) {
//        RealAdMobBanner(adUnitId = adUnitId)
//    } else {
//        CustomBannerAd(modifier = modifier)
//    }
}

@Composable
fun RealAdMobBanner(
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId

                    // ตั้งค่า Ad Listener
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            println("✅ Banner Ad loaded successfully")
                        }

                        override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                            super.onAdFailedToLoad(adError)
                            println("❌ Banner Ad failed to load: ${adError.message}")
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            println("👆 Banner Ad clicked")
                        }

                        override fun onAdOpened() {
                            super.onAdOpened()
                            println("📱 Banner Ad opened")
                        }
                    }

                    // โหลดโฆษณา
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun CustomBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "banner animation")

    // Pulse animation สำหรับปุ่ม CTA
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse animation"
    )

    var isLoading by remember { mutableStateOf(true) }
    var isLoaded by remember { mutableStateOf(false) }
    var showAdMessage by remember { mutableStateOf(false) }

    // Simulate ad loading
    LaunchedEffect(Unit) {
        delay(1500) // Simulate loading time
        isLoading = false
        isLoaded = true
    }

    // Animation สำหรับ ad container
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isLoaded) {
        if (isLoaded) {
            scaleAnim.animateTo(
                1.05f,
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
            scaleAnim.animateTo(1f)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                // เมื่อคลิกที่ Banner Ad
                Toast.makeText(
                    context,
                    "ขอบคุณสำหรับการสนับสนุนนักพัฒนา! 🙏",
                    Toast.LENGTH_SHORT
                ).show()
                showAdMessage = true
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3047)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(
//                        brush = Brush.linearGradient(
//                            colors = listOf(
//                                Color(0xFF4ECDC4),
//                                Color(0xFF2D3047),
//                                Color(0xFF7209B7)
//                            ),
//                            startX = 0f,
//                            endX = Float.POSITIVE_INFINITY
//                        )
//                    )
            )

            when {
                isLoading -> {
                    LoadingBannerContent()
                }
                isLoaded -> {
                    LoadedBannerContent(
                        pulseAlpha = pulseAlpha,
                        onSupportClick = {
                            Toast.makeText(
                                context,
                                "🎉 ขอบคุณที่สนับสนุน! แอปฯ นี้ฟรีเพราะการสนับสนุนจากคุณ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }

    // แสดงข้อความหลังจากคลิก
    if (showAdMessage) {
        LaunchedEffect(Unit) {
            delay(2000)
            showAdMessage = false
        }
    }
}

@Composable
fun LoadingBannerContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 3.dp,
            color = Color.White
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "กำลังโหลดโฆษณา...",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "ขอบคุณที่รอสักครู่",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun LoadedBannerContent(
    pulseAlpha: Float,
    onSupportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ad Badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(0xFFFF6B6B),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AD",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Ad Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "สนับสนุนนักพัฒนา",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "แอปฯ นี้ฟรีเพราะการสนับสนุนจากคุณ",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp
            )
        }

        // Support Button
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF06D6A0).copy(alpha = pulseAlpha),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onSupportClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "สนับสนุน",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SimpleBannerAd() {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📱 ",
                fontSize = 18.sp
            )

            Text(
                text = "ขอบคุณที่ใช้แอปฯ ของเรา",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = {
                    Toast.makeText(
                        context,
                        "กำลังแสดงโฆษณา...",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4ECDC4)
                )
            ) {
                Text(
                    text = "ดูโฆษณา",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PremiumBannerAd() {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isExpanded) 100.dp else 60.dp)
            .padding(16.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF87CEEB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF87CEEB),
                            Color(0xFF98FB98)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌟 ",
                        fontSize = 24.sp
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "สนับสนุนการพัฒนาแอปฯ",
                            color = Color(0xFF2D3047),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "การสนับสนุนจากโฆษณาช่วยให้เราสามารถพัฒนาและปรับปรุงแอปฯ ต่อไปได้ฟรีสำหรับทุกคน",
                                color = Color(0xFF2D3047).copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "แตะเพื่อดูรายละเอียด",
                                color = Color(0xFF2D3047).copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        color = Color(0xFF2D3047),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    Toast.makeText(
                                        context,
                                        "ขอบคุณสำหรับการสนับสนุน! 🎉",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "สนับสนุน",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    color = Color(0xFF4ECDC4),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    Toast.makeText(
                                        context,
                                        "กำลังเปิดเวอร์ชันพรีเมียม...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "พรีเมียม",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}