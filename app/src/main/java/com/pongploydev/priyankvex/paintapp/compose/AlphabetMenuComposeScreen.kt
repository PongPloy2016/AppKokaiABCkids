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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
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
    NUMBERS,
    ENGLISH
}

@Composable
fun AlphabetMenuComposeScreen(
    initialMode: AlphabetMode = AlphabetMode.THAI,
    isAutoReading: Boolean = false,
    currentAutoReadIndex: Int = -1,
    isQuizMode: Boolean = false,
    quizScore: Int = 0,
    lastCorrectClickedId: Int = -1,
    lastWrongClickedId: Int = -1,
    onStartAutoRead: (AlphabetMode) -> Unit = {},
    onStopAutoRead: () -> Unit = {},
    onStartQuiz: (AlphabetMode) -> Unit = {},
    onStopQuiz: () -> Unit = {},
    onRepeatQuizSound: () -> Unit = {},
    onCardClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onCardLongClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onPracticeWriteClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onSoundClick: (AlphabetCardItem, Int) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var currentMode by remember { mutableStateOf(initialMode) }
    val thaiItems = remember { ThaiAlphabetRepository.getThaiAlphabetList() }
    val vowelItems = remember { ThaiAlphabetRepository.getThaiVowelList() }
    val numberItems = remember { ThaiAlphabetRepository.getNumbersList() }
    val englishItems = remember { ThaiAlphabetRepository.getEnglishAlphabetList() }

    val currentItems = when (currentMode) {
        AlphabetMode.THAI -> thaiItems
        AlphabetMode.THAI_VOWELS -> vowelItems
        AlphabetMode.NUMBERS -> numberItems
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
                    if (isQuizMode) onStopQuiz()
                    currentMode = newMode
                },
                onBackClick = onBackClick
            )

            // ── DUAL CONTROL PANEL: AUTO READ & RANDOM QUIZ GAME ──
            MainGameControlBar(
                isAutoReading = isAutoReading,
                currentIndex = currentAutoReadIndex,
                totalCount = currentItems.size,
                currentItemName = if (currentAutoReadIndex in currentItems.indices) currentItems[currentAutoReadIndex].rhyme else "",
                isQuizMode = isQuizMode,
                quizScore = quizScore,
                onStartAutoRead = { onStartAutoRead(currentMode) },
                onStopAutoRead = onStopAutoRead,
                onStartQuiz = { onStartQuiz(currentMode) },
                onStopQuiz = onStopQuiz,
                onRepeatQuizSound = onRepeatQuizSound
            )

            // ── GRID OF ALPHABET / NUMBERS POSTER CARDS ──
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
                    val isCorrect = isQuizMode && (lastCorrectClickedId == item.id)
                    val isWrong = isQuizMode && (lastWrongClickedId == item.id)

                    val cardAspect = when (currentMode) {
                        AlphabetMode.THAI_VOWELS -> 1.73f
                        AlphabetMode.NUMBERS -> 1.55f
                        else -> 1.38f
                    }

                    AlphabetPosterCard(
                        item = item,
                        aspectRatio = cardAspect,
                        isHighlight = isCurrentReading,
                        isQuizCorrect = isCorrect,
                        isQuizWrong = isWrong,
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
                } else if (currentMode == AlphabetMode.NUMBERS) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ThaiNumberBannerFooter()
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
        AlphabetMode.NUMBERS -> listOf(Color(0xFF00C853), Color(0xFF00897B))
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
                .padding(horizontal = 12.dp, vertical = 10.dp)
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

                Spacer(modifier = Modifier.width(10.dp))

                // Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    val titleText = when (currentMode) {
                        AlphabetMode.THAI -> "หัดอ่าน ก-ฮ KARAOKE"
                        AlphabetMode.THAI_VOWELS -> "สระ และ วรรณยุกต์ไทย"
                        AlphabetMode.NUMBERS -> "ตัวเลขไทยและอารบิก (๑ - ๑๐)"
                        AlphabetMode.ENGLISH -> "Learn English ABC (A-Z)"
                    }
                    val subtitleText = when (currentMode) {
                        AlphabetMode.THAI -> "แตะรูปฟังเสียง 🔊 หรือเล่นเกมสุ่มทาย 🎮"
                        AlphabetMode.THAI_VOWELS -> "สระ 32 เสียง + วรรณยุกต์ 4 รูป 🎨"
                        AlphabetMode.NUMBERS -> "นับเลข 1-10 ไทย & อังกฤษ พร้อมเสียงอ่าน 🌟"
                        AlphabetMode.ENGLISH -> "Touch cards to hear pronunciation 🔊"
                    }

                    Text(
                        text = titleText,
                        fontSize = 17.sp,
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
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFF0F5)
                    )
                }

                // Mascot / Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(width = 52.dp, height = 38.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = when (currentMode) {
                                AlphabetMode.THAI_VOWELS -> R.drawable.thai_vowel_header_banner
                                AlphabetMode.NUMBERS -> R.drawable.thai_numbers_banner
                                else -> R.drawable.thai_header_mascot
                            }
                        ),
                        contentDescription = "Mascot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 4-TAB SCROLLABLE/FLEX SELECTOR ──
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    Triple(AlphabetMode.THAI, "🇹🇭 ก - ฮ", "ไทย"),
                    Triple(AlphabetMode.THAI_VOWELS, "📖 สระ-วรรณยุกต์", "สระ"),
                    Triple(AlphabetMode.NUMBERS, "🔢 ๑-๑๐ ตัวเลข", "เลข"),
                    Triple(AlphabetMode.ENGLISH, "🇬🇧 A - Z", "อังกฤษ")
                )

                items(tabs) { (mode, title, _) ->
                    ModeTabButton(
                        title = title,
                        isSelected = (currentMode == mode),
                        onClick = { onModeChange(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00695C) else Color.White,
        label = "tabText"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * แผงควบคุมระบบ Auto-Read และมินิเกมสุ่มทายเสียง (Quiz Mode)
 */
@Composable
private fun MainGameControlBar(
    isAutoReading: Boolean,
    currentIndex: Int,
    totalCount: Int,
    currentItemName: String,
    isQuizMode: Boolean,
    quizScore: Int,
    onStartAutoRead: () -> Unit,
    onStopAutoRead: () -> Unit,
    onStartQuiz: () -> Unit,
    onStopQuiz: () -> Unit,
    onRepeatQuizSound: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // ── IF IN QUIZ MODE ──
            if (isQuizMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Replay Audio Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF9800),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onRepeatQuizSound() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "ฟังเสียงคำถามซ้ำ",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Quiz Text & Star Score
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎮 ทายเสียง: แตะเลือกการ์ดที่ถูก",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "คะแนนสะสม: $quizScore ข้อ (แตะ 🔁 เพื่อฟังซ้ำ)",
                                fontSize = 11.sp,
                                color = Color(0xFF424242),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stop Quiz Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF4D4D),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { onStopQuiz() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "หยุดเกม",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "หยุด",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // ── NORMAL MODE: AUTO READ + START QUIZ BUTTONS ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sound Icon
                    Surface(
                        shape = CircleShape,
                        color = if (isAutoReading) Color(0xFFFF5252) else Color(0xFF00B4D8),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isAutoReading) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.AutoAwesome,
                                contentDescription = "Auto Read",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAutoReading) "กำลังอ่านออกเสียง..." else "ระบบช่วยอ่าน & เกมทายเสียง",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3142)
                        )
                        Text(
                            text = if (isAutoReading && currentItemName.isNotEmpty())
                                "กำลังอ่าน: $currentItemName"
                            else
                                "แตะ Start อ่านต่อเนื่อง หรือกด 🎮 สุ่มทาย",
                            fontSize = 10.5.sp,
                            color = if (isAutoReading) Color(0xFFE63946) else Color(0xFF6C757D),
                            fontWeight = if (isAutoReading) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // 1. Auto Read Start/Stop
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAutoReading) Color(0xFFFF4D4D) else Color(0xFF00C853),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable {
                            if (isAutoReading) onStopAutoRead() else onStartAutoRead()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAutoReading) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isAutoReading) "หยุด" else "อ่าน",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isAutoReading) "Stop" else "อ่าน",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 2. Quiz Button
                    if (!isAutoReading) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF8F00),
                            shadowElevation = 2.dp,
                            modifier = Modifier.clickable { onStartQuiz() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Extension,
                                    contentDescription = "สุ่มทาย",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "สุ่มทาย",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Progress Bar (Visible when Auto-reading)
                AnimatedVisibility(visible = isAutoReading) {
                    Column(modifier = Modifier.padding(top = 5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ความคืบหน้า",
                                fontSize = 10.sp,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = "${(currentIndex + 1).coerceAtLeast(0)} / $totalCount",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B4D8)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val progressFraction = if (totalCount > 0) ((currentIndex + 1).toFloat() / totalCount).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF5252),
                            trackColor = Color(0xFFFFEBEE)
                        )
                    }
                }
            }
        }
    }
}

/**
 * การ์ดตัวอักษรตัดตรงจากโปสเตอร์สีสันสดใส ก-ฮ, สระ-วรรณยุกต์, ตัวเลข 1-10 และ A-Z
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlphabetPosterCard(
    item: AlphabetCardItem,
    aspectRatio: Float = 1.38f,
    isHighlight: Boolean = false,
    isQuizCorrect: Boolean = false,
    isQuizWrong: Boolean = false,
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

    val scale = if (isHighlight || isQuizCorrect) pulseScale else 1.0f

    val borderStroke = when {
        isQuizCorrect -> BorderStroke(3.5.dp, Color(0xFF00E676)) // Bright Green
        isQuizWrong -> BorderStroke(3.5.dp, Color(0xFFFF1744))   // Bright Red
        isHighlight -> BorderStroke(3.dp, Color(0xFFFFB300))     // Bright Amber
        else -> BorderStroke(1.dp, Color(0x1F000000))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .scale(scale)
            .shadow(
                elevation = if (isHighlight || isQuizCorrect) 8.dp else 2.5.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderStroke
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

/**
 * แบนเนอร์ด้านล่างของแท็บตัวเลข
 */
@Composable
private fun ThaiNumberBannerFooter() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.5.dp, Color(0xFF43A047)),
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
                text = "🔢 ฝึกนับจำนวน ๑ - ๑๐ ทั้งภาษาไทยและอังกฤษ",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center
            )
        }
    }
}
