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

data class SubMenuItemData(
    val mode: AlphabetMode,
    val iconEmoji: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageRes: Int,
    val gradientColors: List<Color>,
    val borderColor: Color,
    val badgeBgColor: Color,
    val badgeTextColor: Color
)

@Composable
fun SubMenuComposeScreen(
    languageCategory: LanguageHubCategory,
    onSelectMode: (AlphabetMode) -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val isThai = (languageCategory == LanguageHubCategory.THAI)

    val headerGradient = if (isThai) {
        listOf(Color(0xFFFF5252), Color(0xFFFF7A00), Color(0xFFFFB74D))
    } else {
        listOf(Color(0xFF0288D1), Color(0xFF00B0FF), Color(0xFF00E676))
    }

    val menuItems = if (isThai) {
        listOf(
            SubMenuItemData(
                mode = AlphabetMode.THAI,
                iconEmoji = "🇹🇭",
                title = "ก - ฮ พยัญชนะไทย",
                subtitle = "พยัญชนะไทย ๔๔ ตัว",
                description = "ฝึกอ่าน ก ไก่ ถึง ฮ นกฮูก พร้อมเสียงอ่านคาราโอเกะแท้",
                imageRes = R.drawable.thai_card_1,
                gradientColors = listOf(Color(0xFFFF4081), Color(0xFFE91E63)),
                borderColor = Color(0xFFFF4081),
                badgeBgColor = Color(0xFFFFEBEE),
                badgeTextColor = Color(0xFFC2185B)
            ),
            SubMenuItemData(
                mode = AlphabetMode.THAI_VOWELS,
                iconEmoji = "📖",
                title = "สระ และ วรรณยุกต์",
                subtitle = "สระ ๓๒ เสียง + วรรณยุกต์ ๔ รูป",
                description = "เรียนรู้สระเสียงสั้น-ยาว วรรณยุกต์ พร้อมคำอ่านตัวอย่าง",
                imageRes = R.drawable.thai_vowel_1,
                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFF57C00)),
                borderColor = Color(0xFFFF9800),
                badgeBgColor = Color(0xFFFFF3E0),
                badgeTextColor = Color(0xFFE65100)
            ),
            SubMenuItemData(
                mode = AlphabetMode.TONE_GROUPS,
                iconEmoji = "📚",
                title = "อักษร ๓ หมู่ (ไตรยางศ์)",
                subtitle = "กลาง ๙, สูง ๑๑, ต่ำ ๒๔",
                description = "แบ่งกลุ่มสีชัดเจน พร้อมบทช่วยจำ ไก่จิกเด็กตายฯ",
                imageRes = R.drawable.thai_card_27,
                gradientColors = listOf(Color(0xFF7E57C2), Color(0xFF5E35B1)),
                borderColor = Color(0xFF7E57C2),
                badgeBgColor = Color(0xFFEDE7F6),
                badgeTextColor = Color(0xFF4527A0)
            ),
            SubMenuItemData(
                mode = AlphabetMode.NUMBERS,
                iconEmoji = "🔢",
                title = "ตัวเลขไทย (๑ - ๑๐)",
                subtitle = "นับและอ่านเลขไทย ๑-๑๐ และ ๐",
                description = "ฝึกนับจำนวนสิ่งของและอ่านออกเสียงเลขไทยแสนสนุก",
                imageRes = R.drawable.thai_numbers_banner,
                gradientColors = listOf(Color(0xFF00C853), Color(0xFF00897B)),
                borderColor = Color(0xFF00C853),
                badgeBgColor = Color(0xFFE8F5E9),
                badgeTextColor = Color(0xFF1B5E20)
            ),
            SubMenuItemData(
                mode = AlphabetMode.COLORS_DAYS,
                iconEmoji = "🎨",
                title = "๗ วัน ๗ สี & สีสัน",
                subtitle = "Colors & 7 Days in a Week",
                description = "วันจันทร์สีเหลือง วันอังคารสีชมพู พร้อมสีสันรอบตัว",
                imageRes = R.drawable.yellow,
                gradientColors = listOf(Color(0xFFEC407A), Color(0xFFAB47BC)),
                borderColor = Color(0xFFEC407A),
                badgeBgColor = Color(0xFFFCE4EC),
                badgeTextColor = Color(0xFF880E4F)
            ),
            SubMenuItemData(
                mode = AlphabetMode.SPELLING,
                iconEmoji = "🗣️",
                title = "แจกลูกสะกดคำ",
                subtitle = "ฝึกผสมคำ สระอา สระอี สระอู",
                description = "ก + า = กา, ต + า = ตา ฝึกอ่านออกเขียนได้จริง",
                imageRes = R.drawable.k_one,
                gradientColors = listOf(Color(0xFFFF7043), Color(0xFFF4511E)),
                borderColor = Color(0xFFFF7043),
                badgeBgColor = Color(0xFFFBE9E7),
                badgeTextColor = Color(0xFFBF360C)
            ),
            SubMenuItemData(
                mode = AlphabetMode.VEHICLES,
                iconEmoji = "🚗",
                title = "ยานพาหนะน่ารู้",
                subtitle = "Vehicles & Transport",
                description = "รถยนต์, รถไฟ ปู๊นๆ, เครื่องบิน, รถพยาบาล, เรือ",
                imageRes = R.drawable.car,
                gradientColors = listOf(Color(0xFF00ACC1), Color(0xFF00838F)),
                borderColor = Color(0xFF00ACC1),
                badgeBgColor = Color(0xFFE0F7FA),
                badgeTextColor = Color(0xFF006064)
            ),
            SubMenuItemData(
                mode = AlphabetMode.BODY_PARTS,
                iconEmoji = "✋",
                title = "ร่างกายของฉัน",
                subtitle = "My Body Parts",
                description = "ตา, หู, จมูก, ปาก, มือ, แขน, ขา, เท้า พร้อมเสียง ๒ ภาษา",
                imageRes = R.drawable.eye,
                gradientColors = listOf(Color(0xFF26A69A), Color(0xFF00695C)),
                borderColor = Color(0xFF26A69A),
                badgeBgColor = Color(0xFFE0F2F1),
                badgeTextColor = Color(0xFF004D40)
            )
        )
    } else {
        listOf(
            SubMenuItemData(
                mode = AlphabetMode.ENGLISH,
                iconEmoji = "🇬🇧",
                title = "Alphabet A - Z",
                subtitle = "26 English Letters",
                description = "Learn ABC with phonics pronunciation & cute vocabulary",
                imageRes = R.drawable.words_a,
                gradientColors = listOf(Color(0xFF0288D1), Color(0xFF0097A7)),
                borderColor = Color(0xFF0288D1),
                badgeBgColor = Color(0xFFE1F5FE),
                badgeTextColor = Color(0xFF01579B)
            ),
            SubMenuItemData(
                mode = AlphabetMode.NUMBERS,
                iconEmoji = "🔢",
                title = "Numbers 1 - 10",
                subtitle = "Counting & Words",
                description = "Count 1 to 10 with illustrated objects & English words",
                imageRes = R.drawable.n1,
                gradientColors = listOf(Color(0xFF00C853), Color(0xFF00897B)),
                borderColor = Color(0xFF00C853),
                badgeBgColor = Color(0xFFE8F5E9),
                badgeTextColor = Color(0xFF1B5E20)
            ),
            SubMenuItemData(
                mode = AlphabetMode.ANIMALS,
                iconEmoji = "🐶",
                title = "Animals & Sounds",
                subtitle = "20 Cute Animals",
                description = "Hear real animal sounds and learn English names",
                imageRes = R.drawable.elephant,
                gradientColors = listOf(Color(0xFFFF7043), Color(0xFFD84315)),
                borderColor = Color(0xFFFF7043),
                badgeBgColor = Color(0xFFFBE9E7),
                badgeTextColor = Color(0xFFBF360C)
            ),
            SubMenuItemData(
                mode = AlphabetMode.FRUITS,
                iconEmoji = "🍎",
                title = "Fruits & Vegetables",
                subtitle = "18 Healthy Fruits & Veggies",
                description = "Discover tasty fruits with vivid pictures & words",
                imageRes = R.drawable.watermelon,
                gradientColors = listOf(Color(0xFFE91E63), Color(0xFF880E4F)),
                borderColor = Color(0xFFE91E63),
                badgeBgColor = Color(0xFFFCE4EC),
                badgeTextColor = Color(0xFF880E4F)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isThai) listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFCE4EC),
                        Color(0xFFE8F5E9)
                    ) else listOf(
                        Color(0xFFE1F5FE),
                        Color(0xFFE8F5E9),
                        Color(0xFFFFF9C4)
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
                color = headerGradient.first(),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(colors = headerGradient))
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
                                contentDescription = "กลับหน้าหลัก",
                                tint = Color(0xFF333333)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isThai) "🇹🇭 ห้องเรียนภาษาไทย" else "🇬🇧 English Kids Hub",
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
                            text = if (isThai) "เลือกบทเรียนภาษาไทยที่ต้องการฝึกฝน 🎨" else "Select an English category to learn 🌟",
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
                            painter = painterResource(
                                id = if (isThai) R.drawable.thai_header_mascot else R.drawable.elephant
                            ),
                            contentDescription = "Mascot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── PROMPT SUBTITLE ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isThai) "แตะเลือกหมวดที่ต้องการเรียนรู้ 📖" else "Tap a category to start learning 🚀",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF37474F),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isThai) "ฝึกอ่าน ออกเสียง พร้อมมินิเกมสุ่มทายสะสมดาว" else "Audio pronunciations with interactive quizzes",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF78909C),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── MENU CARDS LIST ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                menuItems.forEach { item ->
                    SubMenuCategoryCard(
                        item = item,
                        onClick = { onSelectMode(item.mode) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── BOTTOM BADGE ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isThai) "มีระบบอ่านต่อเนื่อง (Auto Read) และเกมสุ่มทาย (Quiz)" else "Auto-Read & Interactive Listening Quiz",
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

@Composable
private fun SubMenuCategoryCard(
    item: SubMenuItemData,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .shadow(elevation = 4.5.dp, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.8.dp, item.borderColor.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(item.gradientColors))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.iconEmoji,
                            fontSize = 19.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.title,
                            fontSize = 15.5.sp,
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
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "เข้าสู่บทเรียน",
                                tint = item.gradientColors.first(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Card Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mascot Thumbnail
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = item.badgeBgColor,
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Texts
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = item.badgeBgColor,
                        border = BorderStroke(0.6.dp, item.borderColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = item.subtitle,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.badgeTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF455A64),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
