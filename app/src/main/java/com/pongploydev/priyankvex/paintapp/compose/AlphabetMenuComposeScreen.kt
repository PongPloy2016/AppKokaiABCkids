package com.pongploydev.priyankvex.paintapp.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.model.AlphabetCardItem
import com.pongploydev.priyankvex.paintapp.model.ThaiAlphabetRepository

enum class AlphabetMode {
    THAI,
    THAI_VOWELS,
    ENGLISH
}

@Composable
fun AlphabetMenuComposeScreen(
    initialMode: AlphabetMode = AlphabetMode.THAI,
    isAutoReading: Boolean = false,
    currentAutoReadIndex: Int = -1,
    onStartAutoRead: (AlphabetMode) -> Unit = {},
    onStopAutoRead: () -> Unit = {},
    onCardClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onCardLongClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onPracticeWriteClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onSoundClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var currentMode by remember { mutableStateOf(initialMode) }
    val thaiItems = remember { ThaiAlphabetRepository.getThaiAlphabetList() }
    val vowelItems = remember { ThaiAlphabetRepository.getThaiVowelList() }
    val englishItems = remember { ThaiAlphabetRepository.getEnglishAlphabetList() }

    val currentItems = when (currentMode) {
        AlphabetMode.THAI -> thaiItems
        AlphabetMode.THAI_VOWELS -> vowelItems
        AlphabetMode.ENGLISH -> englishItems
    }
    val listState = rememberLazyGridState()

    // Smooth auto-scroll when Auto-Reading
    LaunchedEffect(currentAutoReadIndex) {
        if (isAutoReading && currentAutoReadIndex in currentItems.indices) {
            listState.animateScrollToItem(currentAutoReadIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0), // Warm Cream
                        Color(0xFFFCE4EC), // Soft Pastel Pink
                        Color(0xFFE8F5E9)  // Gentle Mint Green
                    )
                )
            )
    ) {
        // Ambient kid bubbles & stars background
        KidBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── TOP HEADER SECTION ──
            AlphabetTopHeader(
                currentMode = currentMode,
                onModeChange = { newMode ->
                    if (isAutoReading) onStopAutoRead()
                    currentMode = newMode
                },
                onBackClick = onBackClick
            )

            // ── AUTO READ CONTROL PANEL ──
            AutoReadControlBar(
                isAutoReading = isAutoReading,
                currentIndex = currentAutoReadIndex,
                totalCount = currentItems.size,
                currentItemName = if (currentAutoReadIndex in currentItems.indices) currentItems[currentAutoReadIndex].rhyme else "",
                onStart = { onStartAutoRead(currentMode) },
                onStop = onStopAutoRead
            )

            // ── GRID OF ALPHABET POSTER CARDS ──
            LazyVerticalGrid(
                state = listState,
                columns = GridCells.Adaptive(minSize = if (currentMode == AlphabetMode.THAI_VOWELS) 105.dp else 100.dp),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(currentItems) { index, item ->
                    val isCurrentReading = isAutoReading && (currentAutoReadIndex == index)
                    AlphabetPosterCard(
                        item = item,
                        aspectRatio = if (currentMode == AlphabetMode.THAI_VOWELS) 1.73f else 1.38f,
                        isHighlight = isCurrentReading,
                        showWriteIcon = (currentMode == AlphabetMode.THAI),
                        onClick = { onCardClick(item, index) },
                        onLongClick = { onCardLongClick(item, index) },
                        onSoundClick = { onSoundClick(item, index) },
                        onPracticeWriteClick = { onPracticeWriteClick(item, index) }
                    )
                }

                // ── BOTTOM BANNER SECTIONS ──
                if (currentMode == AlphabetMode.THAI) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ThaiObsoleteNoticeBanner(text = "💡 พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว")
                    }
                } else if (currentMode == AlphabetMode.THAI_VOWELS) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ThaiVowelLegendBanner()
                    }
                }
            }
        }
    }
}

/**
 * ส่วนหัวแบนเนอร์ด้านบนสไตล์โปสเตอร์
 */
@Composable
private fun AlphabetTopHeader(
    currentMode: AlphabetMode,
    onModeChange: (AlphabetMode) -> Unit,
    onBackClick: () -> Unit
) {
    val headerGradient = when (currentMode) {
        AlphabetMode.THAI -> listOf(Color(0xFFFF4081), Color(0xFFE91E63))
        AlphabetMode.THAI_VOWELS -> listOf(Color(0xFFFF9800), Color(0xFFF57C00))
        AlphabetMode.ENGLISH -> listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = headerGradient.first(),
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = headerGradient))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "กลับ",
                            tint = Color(0xFF333333)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    val titleText = when (currentMode) {
                        AlphabetMode.THAI -> "หัดอ่าน ก-ฮ KARAOKE"
                        AlphabetMode.THAI_VOWELS -> "สระ และ วรรณยุกต์ไทย"
                        AlphabetMode.ENGLISH -> "Learn English ABC (A-Z)"
                    }
                    val subtitleText = when (currentMode) {
                        AlphabetMode.THAI -> "แตะที่รูปเพื่อฟังเสียง 🔊 หรือกดค้างเพื่อฝึกเขียน ✏️"
                        AlphabetMode.THAI_VOWELS -> "สระ 32 เสียง + วรรณยุกต์ 4 รูป (แตะเพื่อฟังเสียง 🔊)"
                        AlphabetMode.ENGLISH -> "Touch cards to hear pronunciation 🔊"
                    }

                    Text(
                        text = titleText,
                        fontSize = 18.sp,
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
                        text = subtitleText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFF0F5)
                    )
                }

                // Mascot / Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(width = 54.dp, height = 40.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (currentMode == AlphabetMode.THAI_VOWELS) R.drawable.thai_vowel_header_banner else R.drawable.thai_header_mascot
                        ),
                        contentDescription = "Mascot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 3-TAB SELECTOR (ก-ฮ, สระ-วรรณยุกต์, A-Z) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Thai ก-ฮ
                ModeTabButton(
                    title = "🇹🇭 ก - ฮ",
                    isSelected = (currentMode == AlphabetMode.THAI),
                    modifier = Modifier.weight(1f),
                    onClick = { onModeChange(AlphabetMode.THAI) }
                )

                // Tab 2: Thai Vowels สระ-วรรณยุกต์
                ModeTabButton(
                    title = "📖 สระ-วรรณยุกต์",
                    isSelected = (currentMode == AlphabetMode.THAI_VOWELS),
                    modifier = Modifier.weight(1.25f),
                    onClick = { onModeChange(AlphabetMode.THAI_VOWELS) }
                )

                // Tab 3: English A-Z
                ModeTabButton(
                    title = "🇬🇧 A - Z",
                    isSelected = (currentMode == AlphabetMode.ENGLISH),
                    modifier = Modifier.weight(1f),
                    onClick = { onModeChange(AlphabetMode.ENGLISH) }
                )
            }
        }
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFD81B60) else Color.White,
        label = "tabText"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * แถบควบคุมระบบอ่านออกเสียงอัตโนมัติ (Auto Read Bar)
 */
@Composable
private fun AutoReadControlBar(
    isAutoReading: Boolean,
    currentIndex: Int,
    totalCount: Int,
    currentItemName: String,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sound Wave Icon
                Surface(
                    shape = CircleShape,
                    color = if (isAutoReading) Color(0xFFFF5252) else Color(0xFF00B4D8),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isAutoReading) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.AutoAwesome,
                            contentDescription = "Auto Read",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAutoReading) "กำลังอ่านออกเสียงอัตโนมัติ" else "ระบบช่วยอ่าน (Auto Read)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3142)
                    )
                    Text(
                        text = if (isAutoReading && currentItemName.isNotEmpty())
                            "กำลังอ่าน: $currentItemName"
                        else
                            "แตะ Start เพื่อฟังเสียงอ่านทีละตัวตามลำดับ",
                        fontSize = 11.sp,
                        color = if (isAutoReading) Color(0xFFE63946) else Color(0xFF6C757D),
                        fontWeight = if (isAutoReading) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Start / Stop Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAutoReading) Color(0xFFFF4D4D) else Color(0xFF00C853),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        if (isAutoReading) onStop() else onStart()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAutoReading) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoReading) "หยุด" else "เริ่ม",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoReading) "Stop" else "Start",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Progress Bar (Visible when reading)
            AnimatedVisibility(visible = isAutoReading) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ความคืบหน้า",
                            fontSize = 10.5.sp,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "${(currentIndex + 1).coerceAtLeast(0)} / $totalCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00B4D8)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    val progressFraction = if (totalCount > 0) ((currentIndex + 1).toFloat() / totalCount).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFF5252),
                        trackColor = Color(0xFFFFEBEE)
                    )
                }
            }
        }
    }
}

/**
 * การ์ดตัวอักษรตัดตรงจากโปสเตอร์สีสันสดใส ก-ฮ, สระ-วรรณยุกต์ และ A-Z
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlphabetPosterCard(
    item: AlphabetCardItem,
    aspectRatio: Float = 1.38f,
    isHighlight: Boolean = false,
    showWriteIcon: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSoundClick: () -> Unit = {},
    onPracticeWriteClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val scale = if (isHighlight) pulseScale else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .scale(scale)
            .shadow(
                elevation = if (isHighlight) 8.dp else 2.5.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isHighlight) BorderStroke(3.dp, Color(0xFFFFB300)) else BorderStroke(1.dp, Color(0x1F000000))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── The Cropped Poster Card Image ──
            Image(
                painter = painterResource(id = if (item.posterCardRes != 0) item.posterCardRes else item.imageRes),
                contentDescription = item.name,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

/**
 * แบนเนอร์แจ้งเตือนด้านล่าง
 */
@Composable
private fun ThaiObsoleteNoticeBanner(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
        border = BorderStroke(1.5.dp, Color(0xFF0288D1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF01579B),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * แบนเนอร์อธิบายประเภทสระและวรรณยุกต์
 */
@Composable
private fun ThaiVowelLegendBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(1.5.dp, Color(0xFFFFB300)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "★ สระเสียงสั้น",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = "★ สระเสียงยาว",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
                Text(
                    text = "★ วรรณยุกต์",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "💡 สระ ฦ และ ฦๅ ในปัจจุบันเลิกใช้แล้ว",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF795548)
            )
        }
    }
}
