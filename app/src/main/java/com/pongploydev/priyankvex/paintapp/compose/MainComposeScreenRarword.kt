//package com.pongploydev.priyankvex.paintapp.compose
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.view.Gravity
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.pongploydev.priyankvex.paintapp.R
//import com.pongploydev.priyankvex.paintapp.activity.MenuActivity
//import com.pongploydev.priyankvex.paintapp.activity.MenuActivityAZ
//import com.pongploydev.priyankvex.paintapp.activity.learning.LeaningActvity
//import com.pongploydev.priyankvex.paintapp.activity.learning.showKids.ColoringGameActivity
//import com.pongploydev.priyankvex.paintapp.ads.AdMobManager
//import com.pongploydev.priyankvex.paintapp.dialog.AboutDialog
//import kotlinx.coroutines.delay
//
//// สีสันสำหรับเด็กๆ
//val KidsColorPalette = listOf(
//    Color(0xFFFF6B6B), // สีแดงสด
//    Color(0xFF4ECDC4), // สีฟ้าอมเขียว
//    Color(0xFFFFD166), // สีเหลือง
//    Color(0xFF06D6A0), // สีเขียวสด
//    Color(0xFF118AB2), // สีน้ำเงิน
//    Color(0xFFEF476F), // สีชมพู
//    Color(0xFF7209B7), // สีม่วง
//    Color(0xFFF15BB5)  // สีชมพูอมม่วง
//)
//
//val ActivityColorPalette = listOf(
//    Color(0xFFFF9E6D), // สีส้ม
//    Color(0xFF6A67CE), // สีม่วงอ่อน
//    Color(0xFF4CD7D0), // สีฟ้าใส
//    Color(0xFFFF78C8), // สีชมพูเจล
//    Color(0xFF9DDE1B), // สีเขียวเหลือง
//    Color(0xFFE8A0B0)  // สีชมพูพาสเทล
//)
//
//val GradientBackground = Brush.verticalGradient(
//    colors = listOf(
//        Color(0xFF87CEEB), // สีฟ้าอ่อน
//        Color(0xFF98FB98)  // สีเขียวอ่อน
//    )
//)
//
//val RainbowGradient = Brush.horizontalGradient(
//    colors = listOf(
//        Color(0xFFFF6B6B), // แดง
//        Color(0xFFFFD166), // เหลือง
//        Color(0xFF06D6A0), // เขียว
//        Color(0xFF118AB2), // น้ำเงิน
//        Color(0xFF7209B7)  // ม่วง
//    )
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainComposeScreen(navController: NavController? = null) {
//    val context = LocalContext.current
//    val viewMode = remember { mutableStateOf(ViewMode.GRID) }
//    val menuItems = remember { getMenuItems() }
//
//    // สร้าง AdMob Manager
//    val adMobManager = remember {
//        AdMobManager(context)
//    }
//
//    // State สำหรับ tracking
//    var isAdLoading by remember { mutableStateOf(false) }
//    var showAdDialog by remember { mutableStateOf(false) }
//    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
//
//    // โหลดโฆษณาเมื่อเริ่มต้น
//    LaunchedEffect(Unit) {
//        //loadAd(adMobManager)
//    }
//
//    // ฟังก์ชันโหลดโฆษณา
//    fun loadAd(manager: AdMobManager) {
//        if (!isAdLoading) {
//            isAdLoading = true
//            manager.loadRewardedAd(
//                onAdLoaded = {
//                    isAdLoading = false
//                    println("✅ Rewarded ad loaded successfully")
//                },
//                onAdFailed = {
//                    isAdLoading = false
//                    println("❌ Failed to load rewarded ad")
//                }
//            )
//        }
//    }
//
//    // ฟังก์ชันจัดการการคลิก
//    fun handleItemClick(item: MenuItem) {
//        selectedItem = item
//
//        if (adMobManager.isAdLoaded()) {
//            showAdDialog = true
//        } else {
//            // ถ้าโฆษณายังไม่พร้อม ให้เปิดหน้าปกติ
//            handleItemClickDirectly(context, item, navController)
//            // พยายามโหลดโฆษณาใหม่สำหรับครั้งถัดไป
//            loadAd(adMobManager)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        text = "ฝึกเขียน ก ไก่-ABC",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 20.sp,
//                        color = Color(0xFF2D3047),
//                        modifier = Modifier.background(
//                            brush = RainbowGradient,
//                            shape = RoundedCornerShape(12.dp)
//                        )
//                            .padding(horizontal = 16.dp, vertical = 4.dp)
//                    )
//                },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = Color(0xFF87CEEB)
//                ),
//                actions = {
//                    // Animated Settings Button
//                    AnimatedIconButton(
//                        onClick = { showSettingsDialog(context) },
//                        icon = Icons.Default.Settings,
//                        contentDescription = "Settings",
//                        buttonColor = Color(0xFFFF6B6B)
//                    )
//
//                    // Animated Info Button
//                    AnimatedIconButton(
//                        onClick = { AboutDialog(context).show() },
//                        icon = Icons.Default.Info,
//                        contentDescription = "About",
//                        buttonColor = Color(0xFF4ECDC4)
//                    )
//                }
//            )
//        },
//        content = { paddingValues ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(brush = GradientBackground)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues)
//                ) {
//                    ViewModeSelector(
//                        currentMode = viewMode.value,
//                        onModeSelected = { mode ->
//                            viewMode.value = mode
//                        }
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    when (viewMode.value) {
//                        ViewMode.GRID -> {
//                            GridView(
//                                items = menuItems,
//                                onItemClick = { item ->
//                                    handleItemClick(item)
//                                }
//                            )
//                        }
//                        ViewMode.LIST -> {
//                            ListView(
//                                items = menuItems,
//                                onItemClick = { item ->
//                                    handleItemClick(item)
//                                }
//                            )
//                        }
//                    }
//
//                    // แสดงสถานะการโหลดโฆษณา (สำหรับ debugging)
//                    if (isAdLoading) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "กำลังโหลดโฆษณา...",
//                                color = Color(0xFF666666),
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//                }
//
//                // Ad Confirmation Dialog
//                if (showAdDialog) {
//                    AlertDialog(
//                        onDismissRequest = {
//                            showAdDialog = false
//                            selectedItem?.let { item ->
//                                handleItemClickDirectly(context, item, navController)
//                            }
//                        },
//                        title = {
//                            Text(
//                                text = "🎁 รับรางวัลพิเศษ!",
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF2D3047)
//                            )
//                        },
//                        text = {
//                            Text(
//                                "ดูโฆษณาสั้นๆ เพื่อสนับสนุนนักพัฒนา?\n\nคุณจะได้รับเนื้อหาเพิ่มเติมและฟีเจอร์พิเศษ!",
//                                color = Color(0xFF4A4A4A)
//                            )
//                        },
//                        confirmButton = {
//                            Button(
//                                onClick = {
//                                    showAdDialog = false
//                                    selectedItem?.let { item ->
//                                        val activity = context as? Activity
//                                        if (activity != null) {
//                                            adMobManager.showRewardedAd(
//                                                activity = activity,
//                                                onUserEarnedReward = {
//                                                    // ได้รับรางวัล
//                                                    showRewardSuccessMessage(context)
//                                                    handleItemClickWithReward(context, item, navController)
//                                                },
//                                                onAdDismissed = {
//                                                    // โฆษณาปิดแล้ว
//                                                    handleItemClickWithReward(context, item, navController)
//                                                    // โหลดโฆษณาใหม่สำหรับครั้งถัดไป
//                                                    loadAd(adMobManager)
//                                                }
//                                            )
//                                        } else {
//                                            handleItemClickDirectly(context, item, navController)
//                                        }
//                                    }
//                                },
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFF4ECDC4)
//                                )
//                            ) {
//                                Text("ดูโฆษณา", color = Color.White)
//                            }
//                        },
//                        dismissButton = {
//                            TextButton(
//                                onClick = {
//                                    showAdDialog = false
//                                    selectedItem?.let { item ->
//                                        handleItemClickDirectly(context, item, navController)
//                                    }
//                                }
//                            ) {
//                                Text("ข้ามไป", color = Color(0xFF6B6B6B))
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    )
//}
//
//@Composable
//fun AnimatedIconButton(
//    onClick: () -> Unit,
//    icon: ImageVector,
//    contentDescription: String,
//    buttonColor: Color
//) {
//    var isClicked by remember { mutableStateOf(false) }
//    val scale by animateFloatAsState(
//        targetValue = if (isClicked) 0.8f else 1f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "icon button scale"
//    )
//
//    val rotation by animateFloatAsState(
//        targetValue = if (isClicked) 360f else 0f,
//        animationSpec = tween(durationMillis = 500),
//        label = "icon button rotation"
//    )
//
//    IconButton(
//        onClick = {
//            isClicked = true
//            onClick()
//        }
//    ) {
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .background(
//                    color = buttonColor,
//                    shape = RoundedCornerShape(10.dp)
//                )
//                .graphicsLayer {
//                    scaleX = scale
//                    scaleY = scale
//                    rotationZ = rotation
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = contentDescription,
//                tint = Color.White,
//                modifier = Modifier.size(20.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun ViewModeSelector(currentMode: ViewMode, onModeSelected: (ViewMode) -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(20.dp))
//                .background(
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(
//                            Color(0xFFFFD166),
//                            Color(0xFFFF6B6B)
//                        )
//                    )
//                )
//                .padding(4.dp)
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.Center
//            ) {
//                // Animated Grid View Button
//                AnimatedSelectorButton(
//                    text = "แบบตาราง",
//                    isSelected = currentMode == ViewMode.GRID,
//                    onClick = { onModeSelected(ViewMode.GRID) },
//                    selectedColor = Color(0xFF7209B7),
//                    unselectedColor = Color.Transparent
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                // Animated List View Button
//                AnimatedSelectorButton(
//                    text = "แบบรายการ",
//                    isSelected = currentMode == ViewMode.LIST,
//                    onClick = { onModeSelected(ViewMode.LIST) },
//                    selectedColor = Color(0xFF7209B7),
//                    unselectedColor = Color.Transparent
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun AnimatedSelectorButton(
//    text: String,
//    isSelected: Boolean,
//    onClick: () -> Unit,
//    selectedColor: Color,
//    unselectedColor: Color
//) {
//    var isClicked by remember { mutableStateOf(false) }
//    val scale by animateFloatAsState(
//        targetValue = if (isClicked) 0.95f else 1f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "selector button scale"
//    )
//
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(16.dp))
//            .background(
//                color = if (isSelected) selectedColor else unselectedColor
//            )
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//            }
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//            .clickableNoRipple {
//                isClicked = true
//                onClick()
//            }
//    ) {
//        Text(
//            text = text,
//            color = if (isSelected) Color.White else Color(0xFF2D3047),
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(4.dp)
//        )
//    }
//}
//
//@Composable
//fun GridView(items: List<MenuItem>, onItemClick: (MenuItem) -> Unit) {
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(2),
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//        horizontalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(items) { item ->
//            AnimatedGridItem(item = item, onClick = { onItemClick(item) })
//        }
//    }
//}
//
//@Composable
//fun ListView(items: List<MenuItem>, onItemClick: (MenuItem) -> Unit) {
//    LazyColumn(
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(items) { item ->
//            AnimatedListItem(item = item, onClick = { onItemClick(item) })
//        }
//    }
//}
//
//@Composable
//fun AnimatedGridItem(item: MenuItem, onClick: () -> Unit) {
//    var isHovered by remember { mutableStateOf(false) }
//    var isClicked by remember { mutableStateOf(false) }
//
//    // เลือกสีตาม ID ของเมนู
//    val cardColor = when (item.targetActivity) {
//        4 -> ActivityColorPalette[(item.id - 1) % ActivityColorPalette.size] // กิจกรรมเสริมใช้สีต่างออกไป
//        else -> KidsColorPalette[(item.id - 1) % KidsColorPalette.size]
//    }
//
//    val darkerColor = Color(
//        red = (cardColor.red * 0.8f).coerceIn(0f, 1f),
//        green = (cardColor.green * 0.8f).coerceIn(0f, 1f),
//        blue = (cardColor.blue * 0.8f).coerceIn(0f, 1f)
//    )
//
//    // Scale animation when hovered
//    val scale by animateFloatAsState(
//        targetValue = when {
//            isClicked -> 0.95f
//            isHovered -> 1.05f
//            else -> 1f
//        },
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "grid item scale"
//    )
//
//    // Infinite floating animation
//    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
//    val floatAnimation by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(2000, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ),
//        label = "float animation"
//    )
//
//    // Calculate vertical offset for floating effect
//    val floatOffset = (floatAnimation - 0.5f) * 8f // ±4dp movement
//
//    // Click animation - bounce effect
//    val bounceScale = remember { Animatable(1f) }
//
//    LaunchedEffect(isClicked) {
//        if (isClicked) {
//            bounceScale.animateTo(
//                0.9f,
//                animationSpec = spring(
//                    dampingRatio = Spring.DampingRatioLowBouncy,
//                    stiffness = Spring.StiffnessMedium
//                )
//            )
//            bounceScale.animateTo(1f)
//            isClicked = false
//        }
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .graphicsLayer {
//                scaleX = scale * bounceScale.value
//                scaleY = scale * bounceScale.value
//                translationY = floatOffset
//                rotationZ = if (isClicked) 2f else 0f
//            }
//            .clickable {
//                isClicked = true
//                onClick()
//            },
//        shape = RoundedCornerShape(24.dp),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = when {
//                isClicked -> 12.dp
//                isHovered -> 8.dp
//                else -> 4.dp
//            }
//        ),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isClicked) darkerColor else cardColor
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            // Animated Image with pulse effect
//            PulsingImage(
//                painter = painterResource(id = item.iconRes),
//                contentDescription = item.title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(160.dp)
//                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
//            )
//
//            Text(
//                text = item.title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                textAlign = TextAlign.Center,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}
//
//@Composable
//fun AnimatedListItem(item: MenuItem, onClick: () -> Unit) {
//    var isHovered by remember { mutableStateOf(false) }
//    var isClicked by remember { mutableStateOf(false) }
//
//    // เลือกสีตาม ID ของเมนู
//    val cardColor = when (item.targetActivity) {
//        4 -> ActivityColorPalette[(item.id - 1) % ActivityColorPalette.size] // กิจกรรมเสริมใช้สีต่างออกไป
//        else -> KidsColorPalette[(item.id - 1) % KidsColorPalette.size]
//    }
//
//    val darkerColor = Color(
//        red = (cardColor.red * 0.8f).coerceIn(0f, 1f),
//        green = (cardColor.green * 0.8f).coerceIn(0f, 1f),
//        blue = (cardColor.blue * 0.8f).coerceIn(0f, 1f)
//    )
//
//    // Scale animation
//    val scale by animateFloatAsState(
//        targetValue = when {
//            isClicked -> 0.98f
//            isHovered -> 1.02f
//            else -> 1f
//        },
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "list item scale"
//    )
//
//    // Slide in animation for list items
//    val slideOffset = remember { Animatable(100f) }
//
//    // Click animation - press effect
//    val pressOffset = remember { Animatable(0f) }
//
//    LaunchedEffect(Unit) {
//        slideOffset.animateTo(
//            targetValue = 0f,
//            animationSpec = spring(
//                dampingRatio = Spring.DampingRatioLowBouncy,
//                stiffness = Spring.StiffnessMedium
//            )
//        )
//    }
//
//    LaunchedEffect(isClicked) {
//        if (isClicked) {
//            pressOffset.animateTo(4f, tween(100))
//            pressOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
//            isClicked = false
//        }
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .graphicsLayer {
//                scaleX = scale
//                scaleY = scale
//                translationX = slideOffset.value + pressOffset.value
//                translationY = pressOffset.value * 0.5f
//            }
//            .clickable {
//                isClicked = true
//                onClick()
//            },
//        shape = RoundedCornerShape(24.dp),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = when {
//                isClicked -> 12.dp
//                isHovered -> 8.dp
//                else -> 4.dp
//            }
//        ),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isClicked) darkerColor else cardColor
//        )
//    ) {
//        Column(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            // Fade in image animation
//            FadeInImage(
//                painter = painterResource(id = item.iconRes),
//                contentDescription = item.title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(160.dp)
//                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
//            )
//
//            Text(
//                text = item.title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                textAlign = TextAlign.Center,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}
//
//@Composable
//fun PulsingImage(
//    painter: Painter,
//    contentDescription: String?,
//    modifier: Modifier = Modifier
//) {
//    val infiniteTransition = rememberInfiniteTransition(label = "pulse transition")
//    val pulseScale by infiniteTransition.animateFloat(
//        initialValue = 0.98f,
//        targetValue = 1.02f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(1500, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ),
//        label = "pulse scale"
//    )
//
//    Box(
//        modifier = modifier
//            .background(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color.White.copy(alpha = 0.3f),
//                        Color.Transparent
//                    )
//                )
//            )
//    ) {
//        Image(
//            painter = painter,
//            contentDescription = contentDescription,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(160.dp)
//                .graphicsLayer {
//                    scaleX = pulseScale
//                    scaleY = pulseScale
//                },
//            contentScale = ContentScale.Crop
//        )
//    }
//}
//
//@Composable
//fun FadeInImage(
//    painter: Painter,
//    contentDescription: String?,
//    modifier: Modifier = Modifier
//) {
//    var isVisible by remember { mutableStateOf(false) }
//    val alpha by animateFloatAsState(
//        targetValue = if (isVisible) 1f else 0f,
//        animationSpec = tween(durationMillis = 800),
//        label = "fade in animation"
//    )
//
//    LaunchedEffect(Unit) {
//        delay(100)
//        isVisible = true
//    }
//
//    Box(
//        modifier = modifier
//            .background(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color.White.copy(alpha = 0.3f),
//                        Color.Transparent
//                    )
//                )
//            )
//    ) {
//        Image(
//            painter = painter,
//            contentDescription = contentDescription,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(160.dp)
//                .graphicsLayer {
//                    this.alpha = alpha
//                },
//            contentScale = ContentScale.Crop
//        )
//    }
//}
//
//// Extension function for clickable without ripple
//fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
//    Modifier.clickable(
//        interactionSource = MutableInteractionSource(),
//        indication = null
//    ) { onClick() }
//)
//
//// Data classes and enums
//data class MenuItem(
//    val id: Int,
//    val title: String,
//    val iconRes: Int,
//    val targetActivity: Int
//)
//
//enum class ViewMode {
//    GRID, LIST
//}
//
//// Helper functions
//private fun getMenuItems(): List<MenuItem> {
//    return listOf(
//        MenuItem(1, "ฝึกอ่าน", R.drawable.doing_learn_01, 0),
//        MenuItem(2, "ฝึกเขียน ก - ฮ", R.drawable.card_one, 1),
//        MenuItem(3, "ฝึกเขียน A - Z", R.drawable.card_two, 2),
//        MenuItem(4, "กิจกรรมเสริม", R.drawable.card_three, 3),
//        MenuItem(5, "จัดทำโดย", R.drawable.card_four, 4)
//    )
//}
//
//// เปิด Activity พร้อมรางวัล (ถ้ามี)
//private fun handleItemClickWithReward(
//    context: Context,
//    item: MenuItem,
//    navController: NavController?
//) {
//    when (item.targetActivity) {
//        0 -> {
//            val intent = Intent(context, LeaningActvity::class.java).apply {
//                putExtra("REWARD_UNLOCKED", true)
//            }
//            context.startActivity(intent)
//        }
//        1 -> {
//            val intent = Intent(context, MenuActivity::class.java).apply {
//                putExtra("REWARD_UNLOCKED", true)
//            }
//            context.startActivity(intent)
//        }
//        2 -> {
//            val intent = Intent(context, MenuActivityAZ::class.java).apply {
//                putExtra("REWARD_UNLOCKED", true)
//            }
//            context.startActivity(intent)
//        }
//        3 -> {
//            showKidsActivitiesDialog(context)
//        }
//        4 -> {
//            AboutDialog(context).show()
//        }
//    }
//}
//
//// เปิด Activity แบบปกติ (ไม่มีรางวัล)
//private fun handleItemClickDirectly(
//    context: Context,
//    item: MenuItem,
//    navController: NavController?
//) {
//    when (item.targetActivity) {
//        0 -> {
//            val intent = Intent(context, LeaningActvity::class.java)
//            context.startActivity(intent)
//        }
//        1 -> {
//            val intent = Intent(context, MenuActivity::class.java)
//            context.startActivity(intent)
//        }
//        2 -> {
//            val intent = Intent(context, MenuActivityAZ::class.java)
//            context.startActivity(intent)
//        }
//        3 -> {
//            showKidsActivitiesDialog(context)
//        }
//        4 -> {
//            AboutDialog(context).show()
//        }
//    }
//}
//
//// แสดงข้อความเมื่อได้รับรางวัล
//private fun showRewardSuccessMessage(context: Context) {
//    Toast.makeText(
//        context,
//        "🎉 ขอบคุณที่สนับสนุน! คุณได้รับเนื้อหาแบบพิเศษแล้ว",
//        Toast.LENGTH_LONG
//    ).show()
//}
//
//private fun showKidsActivitiesDialog(context: Context) {
//    val activities = listOf(
//        "🎨 ระบายสีสร้างสรรค์" to { startColoringActivity(context) },
//    )
//
//    val layout = LinearLayout(context).apply {
//        orientation = LinearLayout.VERTICAL
//        setPadding(50, 30, 50, 30)
//    }
//
//    val titleText = TextView(context).apply {
//        text = "เลือกกิจกรรมที่สนใจ:"
//        textSize = 18f
//        setTextColor(android.graphics.Color.BLACK)
//        gravity = Gravity.CENTER
//        setPadding(0, 0, 0, 20)
//    }
//    layout.addView(titleText)
//
//    activities.forEach { (activityName, onClick) ->
//        val button = android.widget.Button(context).apply {
//            text = activityName
//            textSize = 16f
//            setBackgroundColor(getRandomColor())
//            setTextColor(android.graphics.Color.WHITE)
//            gravity = Gravity.CENTER
//            setPadding(20, 30, 20, 30)
//
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            ).apply {
//                setMargins(0, 8, 0, 8)
//            }
//
//            setOnClickListener {
//                onClick()
//            }
//        }
//        layout.addView(button)
//    }
//
//    AlertDialog.Builder(context)
//        .setTitle("🎪 กิจกรรมเสริมสำหรับเด็ก")
//        .setView(layout)
//        .setPositiveButton("ปิด", null)
//        .show()
//}
//
//private fun startColoringActivity(context: Context) {
//    val intent = Intent(context, ColoringGameActivity::class.java)
//    context.startActivity(intent)
//}
//
//private fun getRandomColor(): Int {
//    val colors = listOf(
//        android.graphics.Color.parseColor("#FF6B6B"), // สีแดง
//        android.graphics.Color.parseColor("#4ECDC4"), // สีฟ้าอมเขียว
//        android.graphics.Color.parseColor("#FFD166"), // สีเหลือง
//        android.graphics.Color.parseColor("#06D6A0"), // สีเขียว
//        android.graphics.Color.parseColor("#118AB2"), // สีน้ำเงิน
//        android.graphics.Color.parseColor("#7209B7")  // สีม่วง
//    )
//    return colors.random()
//}
//
//private fun showSettingsDialog(context: Context) {
//    android.app.AlertDialog.Builder(context)
//        .setTitle("ตั้งค่า")
//        .setMessage("ฟังก์ชันตั้งค่าจะถูกเพิ่มในเวอร์ชันถัดไป")
//        .setPositiveButton("ตกลง", null)
//        .show()
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewMainComposeScreen() {
//    MainComposeScreen()
//}