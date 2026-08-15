package com.pongploydev.priyankvex.paintapp.compose

// Import สำหรับ AdMob
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.AbcMatchGameActivity
import com.pongploydev.priyankvex.paintapp.activity.KidsWebViewActivity
import com.pongploydev.priyankvex.paintapp.activity.MenuActivityAZ
import com.pongploydev.priyankvex.paintapp.activity.QuizActivity
import com.pongploydev.priyankvex.paintapp.activity.QuizLeaderboardActivity
import com.pongploydev.priyankvex.paintapp.activity.learning.LeaningActvity
import com.pongploydev.priyankvex.paintapp.activity.learning.showKids.ColoringGameActivity
import com.pongploydev.priyankvex.paintapp.activity.thailetters.MenuThaiLettersActivity
import com.pongploydev.priyankvex.paintapp.dialog.AboutDialog
import com.pongploydev.videowatchfeature.utils.ads.InterstitialAdManager
import com.pongploydev.livefeature.LiveListActivity
import kotlinx.coroutines.delay

// สีสันสำหรับเด็กๆ
val KidsColorPalette =
    listOf(
        Color(0xFFFF6B6B), // สีแดงสด
        Color(0xFF4ECDC4), // สีฟ้าอมเขียว
        Color(0xFFFFD166), // สีเหลือง
        Color(0xFF06D6A0), // สีเขียวสด
        Color(0xFF118AB2), // สีน้ำเงิน
        Color(0xFFEF476F), // สีชมพู
        Color(0xFF7209B7), // สีม่วง
        Color(0xFFF15BB5) // สีชมพูอมม่วง
    )

val ActivityColorPalette =
    listOf(
        Color(0xFFFF9E6D), // สีส้ม
        Color(0xFF6A67CE), // สีม่วงอ่อน
        Color(0xFF4CD7D0), // สีฟ้าใส
        Color(0xFFFF78C8), // สีชมพูเจล
        Color(0xFF9DDE1B), // สีเขียวเหลือง
        Color(0xFFE8A0B0) // สีชมพูพาสเทล
    )

val GradientBackground =
    Brush.verticalGradient(
        colors =
            listOf(
                Color(0xFF87CEEB), // สีฟ้าอ่อน
                Color(0xFF98FB98) // สีเขียวอ่อน
            )
    )

val RainbowGradient =
    Brush.horizontalGradient(
        colors =
            listOf(
                Color(0xFFFF6B6B), // แดง
                Color(0xFFFFD166), // เหลือง
                Color(0xFF06D6A0), // เขียว
                Color(0xFF118AB2), // น้ำเงิน
                Color(0xFF7209B7) // ม่วง
            )
    )

@Composable
fun DashboardSection(
    title: String,
    onViewAllClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3047)
            )
            Text(
                text = "View All >",
                fontSize = 14.sp,
                color = Color(0xFF8E78FF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(
    userName: String,
    onSearchChange: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8E78FF), Color(0xFFB4A5FF))
                )
            )
            .padding(top = 24.dp, bottom = 40.dp)
            .padding(horizontal = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi, $userName!",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ready to learn and have fun?",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
                // Profile Image Placeholder
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leraning_kid),
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchChange(it)
                },
                placeholder = { Text("Search anything", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                trailingIcon = {
                    Surface(
                        modifier = Modifier.size(36.dp).padding(end = 4.dp),
                        shape = CircleShape,
                        color = Color(0xFFF0F0F0)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sort),
                            contentDescription = "Filter",
                            modifier = Modifier.padding(8.dp),
                            tint = Color.DarkGray
                        )
                    }
                },
                singleLine = true
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(240.dp)
            .padding(start = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3047)
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF2D3047).copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp).align(Alignment.End),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ExploreCategoriesSection(onCategoryClick: (Int) -> Unit) {
    DashboardSection(title = "Explore Categories") {
        LazyRow(
            contentPadding = PaddingValues(end = 24.dp)
        ) {
            item {
                CategoryCard(
                    title = "Math",
                    subtitle = "Numbers &\nProblem Solving",
                    imageRes = R.drawable.card_one,
                    backgroundColor = Color(0xFFE8E4FF),
                    onClick = { onCategoryClick(1) }
                )
            }
            item {
                CategoryCard(
                    title = "English",
                    subtitle = "Phonics &\nVocabulary",
                    imageRes = R.drawable.card_two,
                    backgroundColor = Color(0xFFE2F9E9),
                    onClick = { onCategoryClick(2) }
                )
            }
            item {
                CategoryCard(
                    title = "Art",
                    subtitle = "Coloring &\nDrawing",
                    imageRes = R.drawable.home_color,
                    backgroundColor = Color(0xFFFFF0B4),
                    onClick = { onCategoryClick(3) }
                )
            }
        }
    }
}

@Composable
fun LearningCard(
    title: String,
    subject: String,
    progress: Float,
    imageRes: Int,
    backgroundColor: Color,
    progressColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(start = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = subject,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3047),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.size(60.dp).align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                    modifier = Modifier.width(36.dp)
                )
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_play_circle_kids),
                    contentDescription = null,
                    tint = progressColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ContinueLearningSection(onLessonClick: (Int) -> Unit) {
    DashboardSection(title = "Continue Learning") {
        LazyRow(
            contentPadding = PaddingValues(end = 24.dp)
        ) {
            item {
                LearningCard(
                    title = "Addition\nAdventure",
                    subject = "Math",
                    progress = 0.65f,
                    imageRes = R.drawable.card_one,
                    backgroundColor = Color(0xFFE2F0FF),
                    progressColor = Color(0xFF0085FF),
                    onClick = { onLessonClick(1) }
                )
            }
            item {
                LearningCard(
                    title = "Animal\nFriends",
                    subject = "English",
                    progress = 0.77f,
                    imageRes = R.drawable.card_three,
                    backgroundColor = Color(0xFFFFE2E2),
                    progressColor = Color(0xFFFF4B4B),
                    onClick = { onLessonClick(2) }
                )
            }
            item {
                LearningCard(
                    title = "Plant Life\nCycle",
                    subject = "Science",
                    progress = 0.46f,
                    imageRes = R.drawable.leraning_kid,
                    backgroundColor = Color(0xFFE2FFF0),
                    progressColor = Color(0xFF00C853),
                    onClick = { onLessonClick(3) }
                )
            }
        }
    }
}

@Composable
fun DailyGoalSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            text = "Daily Goal",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3047)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7F9))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.card_one), // Placeholder for dino
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Great job, Emma! 🎉",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3047)
                    )
                    Text(
                        text = "2 / 3 Lessons",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.66f,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00C853),
                        trackColor = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.card_four), // Placeholder for gift
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}
    val context = LocalContext.current
    var isMusicEnabled by remember {
        mutableStateOf(
            com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance()
                .isMusicEnabled(context)
        )
    }
    val viewMode = remember { mutableStateOf(ViewMode.GRID) }
    val menuItems = remember { getMenuItems() }

    // สร้าง Interstitial Ad Manager
    val interstitialAdManager = remember { InterstitialAdManager(context) }

    // State สำหรับ tracking
    var isAdLoading by remember { mutableStateOf(false) }
    var adLoadAttempt by remember { mutableStateOf(0) }

    // State สำหรับนับรอบ
    var visitCount by remember { mutableStateOf(0) }
    val VISITS_BEFORE_AD = 3 // แสดงโฆษณาทุก 3 รอบ

    // ฟังก์ชันโหลดโฆษณา
    val loadInterstitialAd: (InterstitialAdManager) -> Unit = { manager ->
        if (!isAdLoading && adLoadAttempt < 3) {
            isAdLoading = true
            adLoadAttempt++
            manager.loadInterstitialAd(
                onAdLoaded = {
                    isAdLoading = false
                    adLoadAttempt = 0
                    println("✅ Interstitial ad loaded successfully")
                },
                onAdFailed = {
                    isAdLoading = false
                    println(
                        "❌ Failed to load interstitial ad (attempt $adLoadAttempt)"
                    )
                }
            )
        }
    }

    // ฟังก์ชันแสดงโฆษณา
    val showInterstitialAd: (MenuItem) -> Unit = { item ->
        val activity = context as? Activity
        if (activity != null) {
            interstitialAdManager.showInterstitialAd(
                activity = activity,
                onAdDismissed = {
                    // โฆษณาปิดแล้ว - เปิด Activity ปลายทาง
                    showAdSuccessMessage(context)
                    handleItemClickDirectly(context, item, navController)
                },
                onAdFailed = {
                    // โฆษณาแสดงไม่สำเร็จ - เปิด Activity ปลายทาง
                    showAdFailedMessage(context)
                    handleItemClickDirectly(context, item, navController)
                }
            )
        } else {
            handleItemClickDirectly(context, item, navController)
        }
    }

    // ฟังก์ชันจัดการการคลิก
    val handleItemClick: (MenuItem) -> Unit = { item ->
        // เพิ่มจำนวนรอบ
        visitCount++

        // ตรวจสอบว่าถึงรอบที่ต้องแสดงโฆษณาหรือยัง
        val shouldShowAd =
            visitCount >= VISITS_BEFORE_AD && interstitialAdManager.isAdLoaded()

        if (shouldShowAd) {
            // แสดงโฆษณาและรีเซ็ตนับรอบ
            showInterstitialAd(item)
            visitCount = 0
        } else {
            // เปิดหน้าปกติ
            handleItemClickDirectly(context, item, navController)

            // ถ้ายังไม่ถึงรอบแสดงโฆษณา แต่โฆษณาพร้อมอยู่แล้ว ให้โหลดใหม่เพื่อรอบถัดไป
            if (interstitialAdManager.isAdLoaded()) {
                loadInterstitialAd(interstitialAdManager)
            }
        }

        // พยายามโหลดโฆษณาใหม่ถ้ายังไม่พร้อม
        if (!interstitialAdManager.isAdLoaded()) {
            loadInterstitialAd(interstitialAdManager)
        }
    }

    // โหลดโฆษณาเมื่อเริ่มต้น
    LaunchedEffect(Unit) { loadInterstitialAd(interstitialAdManager) }

    // Background Music
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val musicManager =
            com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> musicManager.startMusic(context)
                Lifecycle.Event.ON_PAUSE -> musicManager.pauseMusic()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    brush = RainbowGradient,
                                    shape =
                                        RoundedCornerShape(
                                            50
                                        ) // More rounded,
                                    // pill/cloud
                                    // shape
                                )
                                .padding(
                                    horizontal = 24.dp,
                                    vertical = 8.dp
                                )
                    ) {
                        Text(
                            text = "ฝึกเขียน ก ไก่-ABC",
                            fontWeight = FontWeight.ExtraBold, // Bolder
                            fontSize = 24.sp, // Larger font
                            color = Color(0xFF2D3047)
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF87CEEB)
                    ),
                actions = {
                    // Animated Settings Button
                    AnimatedIconButton(
                        onClick = {
                            showSettingsDialog(
                                context,
                                isMusicEnabled
                            ) { enabled ->
                                isMusicEnabled = enabled
                                com.pongploydev.priyankvex.paintapp
                                    .utils.MusicManager
                                    .getInstance()
                                    .setMusicEnabled(
                                        context,
                                        enabled
                                    )
                            }
                        },
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        buttonColor = Color(0xFFFF6B6B),
                        size = 48.dp // Larger button
                    )

                    Spacer(modifier = Modifier.width(8.dp)) // Add spacing

                    // Animated Info Button
                    AnimatedIconButton(
                        onClick = { AboutDialog(context).show() },
                        icon = Icons.Default.Info,
                        contentDescription = "About",
                        buttonColor = Color(0xFF4ECDC4),
                        size = 48.dp // Larger button
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Add spacing end
                }
            )
        },
        //        bottomBar = {
        //            // Banner Ad ใน bottomBar ของ Scaffold
        //            ChildDirectedBannerAd(adUnitId =
        // "ca-app-pub-3279793802315716/1117791498")
        //        },
        content = { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(brush = GradientBackground)
            ) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)) {
                    // แสดงจำนวนรอบจนกว่าจะถึงโฆษณา
                    //                    VisitCounter(
                    //                        currentCount = visitCount,
                    //                        maxCount = VISITS_BEFORE_AD,
                    //                        isAdLoaded =
                    // interstitialAdManager.isAdLoaded()
                    //                    )

                    //                    ViewModeSelector(
                    //                        currentMode = viewMode.value,
                    //                        onModeSelected = { mode ->
                    //                            viewMode.value = mode
                    //                        }
                    //                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ใช้ Box กับ weight เพื่อให้เนื้อหาเต็มพื้นที่
                    Box(modifier = Modifier.weight(1f)) {
                        when (viewMode.value) {
                            ViewMode.GRID -> {
                                GridView(
                                    items = menuItems,
                                    onItemClick = { item ->
                                        handleItemClick(
                                            item
                                        )
                                    }
                                )
                            }

                            ViewMode.LIST -> {
                                ListView(
                                    items = menuItems,
                                    onItemClick = { item ->
                                        handleItemClick(
                                            item
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // แสดงสถานะการโหลดโฆษณาเต็มจอ
                    //                    AdLoadingStatus(
                    //                        isAdLoading = isAdLoading,
                    //                        isAdLoaded =
                    // interstitialAdManager.isAdLoaded(),
                    //                        adLoadAttempt = adLoadAttempt
                    //                    )

                    // เพิ่ม Spacer เล็กน้อยเพื่อไม่ให้เนื้อหาติดกับ Banner
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}

// ==================== Banner Ad Functions ====================
//
// @Composable
// fun ChildDirectedBannerAd(
//    adUnitId: String = "ca-app-pub-3940256099942544/6300978111",
//    adSize: AdSize = AdSize.BANNER,
//    modifier: Modifier = Modifier.fillMaxWidth().height(60.dp)
// ) {
//    val context = LocalContext.current
//    var showFallback by remember { mutableStateOf(false) }
//
//    // ตั้งค่าสำหรับเด็ก
//    LaunchedEffect(Unit) {
//        try {
//            MobileAds.setRequestConfiguration(
//                RequestConfiguration.Builder()
//
// .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
//                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
//                    .build()
//            )
//        } catch (e: Exception) {
//            Log.w("ChildAd", "Failed to set child config: ${e.message}")
//        }
//    }
//
//    if (!adUnitId.startsWith("ca-app-pub-") || showFallback) {
//        BannerAdFallback()
//        return
//    }
//
//    AndroidView(
//        factory = { ctx ->
//            AdView(ctx).apply {
//                this.adUnitId = adUnitId
//                setAdSize(adSize)
//                setBackgroundColor(android.graphics.Color.TRANSPARENT)
//
//                adListener = object : AdListener() {
//                    override fun onAdLoaded() {
//                        showFallback = false
//                        Log.d("ChildAd", "Ad loaded successfully")
//                    }
//
//                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                        showFallback = true
//                        Log.w("ChildAd", "Ad failed: ${loadAdError.message}")
//                    }
//                }
//
//                try {
//                    loadAd(AdRequest.Builder().build())
//                } catch (e: Exception) {
//                    showFallback = true
//                    Log.e("ChildAd", "Exception: ${e.message}")
//                }
//            }
//        },
//        modifier = modifier
//    )
// }
@Composable
fun BannerAdFallback() {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3047))
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable {
                        Toast.makeText(
                            context,
                            "ขอบคุณที่สนับสนุนนักพัฒนา! 🙏",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ad Icon
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFFFF6B6B),
                                shape = RoundedCornerShape(8.dp)
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

                // Ad Content
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)) {
                    Text(
                        text = "สนับสนุนนักพัฒนา",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ขอบคุณที่ใช้แอปพลิเคชันของเรา",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp
                    )
                }

                // Support Badge
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = Color(0xFF06D6A0),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
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
    }
}

// ==================== Other Composable Functions ====================

@Composable
fun VisitCounter(currentCount: Int, maxCount: Int, isAdLoaded: Boolean) {
    val remaining = maxCount - currentCount
    val progress = currentCount.toFloat() / maxCount.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Text
        Text(
            text =
                when {
                    currentCount >= maxCount && isAdLoaded ->
                        "🎉 พร้อมแสดงโฆษณา!"

                    currentCount >= maxCount -> "⏳ รอโฆษณาโหลด..."
                    else -> "📊 อีก $remaining ครั้งจะแสดงโฆษณา"
                },
            color =
                when {
                    currentCount >= maxCount && isAdLoaded -> Color(0xFF06D6A0)
                    currentCount >= maxCount -> Color(0xFFFF6B6B)
                    else -> Color(0xFF666666)
                },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        // Progress Bar
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(2.dp)
                    )
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(
                            color =
                                when {
                                    currentCount >= maxCount &&
                                            isAdLoaded ->
                                        Color(0xFF06D6A0)

                                    currentCount >= maxCount ->
                                        Color(0xFFFF6B6B)

                                    else -> Color(0xFF4ECDC4)
                                },
                            shape = RoundedCornerShape(2.dp)
                        )
            )
        }

        // Support Message
        Text(
            text = "ขอบคุณที่สนับสนุนนักพัฒนา! 🙏",
            color = Color(0xFF888888),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AdLoadingStatus(isAdLoading: Boolean, isAdLoaded: Boolean, adLoadAttempt: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isAdLoading ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color(0xFF4ECDC4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text =
                            "กำลังโหลดโฆษณาเต็มจอ... (ครั้งที่ $adLoadAttempt)",
                        color = Color(0xFF666666),
                        fontSize = 12.sp
                    )
                }
            //            isAdLoaded -> Text(
            //                text = "✅ โฆษณาเต็มจอพร้อมแสดง",
            //                color = Color(0xFF06D6A0),
            //                fontSize = 12.sp
            //            )
            //            else -> Text(
            //                text = "❌ โฆษณาเต็มจอยังไม่พร้อม",
            //                color = Color(0xFFFF6B6B),
            //                fontSize = 12.sp
            //            )
        }
    }
}

@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    buttonColor: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by
    animateFloatAsState(
        targetValue = if (isClicked) 0.8f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "icon button scale"
    )

    val rotation by
    animateFloatAsState(
        targetValue = if (isClicked) 360f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "icon button rotation"
    )

    IconButton(
        onClick = {
            isClicked = true
            onClick()
        }
    ) {
        Box(
            modifier =
                Modifier
                    .size(size)
                    .background(
                        color = buttonColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                    },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ViewModeSelector(currentMode: ViewMode, onModeSelected: (ViewMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFD166),
                                        Color(0xFFFF6B6B)
                                    )
                            )
                    )
                    .padding(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                AnimatedSelectorButton(
                    text = "แบบตาราง",
                    isSelected = currentMode == ViewMode.GRID,
                    onClick = { onModeSelected(ViewMode.GRID) },
                    selectedColor = Color(0xFF7209B7),
                    unselectedColor = Color.Transparent
                )

                Spacer(modifier = Modifier.width(8.dp))

                AnimatedSelectorButton(
                    text = "แบบรายการ",
                    isSelected = currentMode == ViewMode.LIST,
                    onClick = { onModeSelected(ViewMode.LIST) },
                    selectedColor = Color(0xFF7209B7),
                    unselectedColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun AnimatedSelectorButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color
) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by
    animateFloatAsState(
        targetValue = if (isClicked) 0.95f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "selector button scale"
    )

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = if (isSelected) selectedColor else unselectedColor
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickableNoRipple {
                    isClicked = true
                    onClick()
                }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF2D3047),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
fun GridView(items: List<MenuItem>, onItemClick: (MenuItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            AnimatedGridItem(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun ListView(items: List<MenuItem>, onItemClick: (MenuItem) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            AnimatedListItem(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
fun AnimatedGridItem(item: MenuItem, onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }

    val cardColor =
        when (item.targetActivity) {
            4 -> ActivityColorPalette[(item.id - 1) % ActivityColorPalette.size]
            else -> KidsColorPalette[(item.id - 1) % KidsColorPalette.size]
        }

    val darkerColor =
        Color(
            red = (cardColor.red * 0.8f).coerceIn(0f, 1f),
            green = (cardColor.green * 0.8f).coerceIn(0f, 1f),
            blue = (cardColor.blue * 0.8f).coerceIn(0f, 1f)
        )

    val scale by
    animateFloatAsState(
        targetValue =
            when {
                isClicked -> 0.95f
                isHovered -> 1.05f
                else -> 1f
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "grid item scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val floatAnimation by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
        label = "float animation"
    )

    val floatOffset = (floatAnimation - 0.5f) * 8f

    val bounceScale = remember { Animatable(1f) }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            bounceScale.animateTo(
                0.9f,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
            )
            bounceScale.animateTo(1f)
            isClicked = false
        }
    }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale * bounceScale.value
                    scaleY = scale * bounceScale.value
                    translationY = floatOffset
                    rotationZ = if (isClicked) 2f else 0f
                }
                .then(
                    if (item.isLive) {
                        Modifier.border(
                            width = 4.dp,
                            brush = Brush.sweepGradient(
                                listOf(Color.Red, Color.White, Color.Blue, Color.Red)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                    } else Modifier
                )
                .clickable {
                    isClicked = true
                    onClick()
                },
        shape = RoundedCornerShape(32.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    when {
                        isClicked -> 12.dp
                        isHovered -> 8.dp
                        else -> 4.dp
                    }
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isClicked) darkerColor else cardColor
            ),
        border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (item.isLive) {
                    KidsTvThailandLogo(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2D3047))
                    )
                    LiveBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                } else {
                    PulsingImage(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp
                                )
                            )
                    )
                }
            }

            Text(
                text = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimatedListItem(item: MenuItem, onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }

    val cardColor =
        when (item.targetActivity) {
            4 -> ActivityColorPalette[(item.id - 1) % ActivityColorPalette.size]
            else -> KidsColorPalette[(item.id - 1) % KidsColorPalette.size]
        }

    val darkerColor =
        Color(
            red = (cardColor.red * 0.8f).coerceIn(0f, 1f),
            green = (cardColor.green * 0.8f).coerceIn(0f, 1f),
            blue = (cardColor.blue * 0.8f).coerceIn(0f, 1f)
        )

    val scale by
    animateFloatAsState(
        targetValue =
            when {
                isClicked -> 0.98f
                isHovered -> 1.02f
                else -> 1f
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        label = "list item scale"
    )

    val slideOffset = remember { Animatable(100f) }

    val pressOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        slideOffset.animateTo(
            targetValue = 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
        )
    }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            pressOffset.animateTo(4f, tween(100))
            pressOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
            isClicked = false
        }
    }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = slideOffset.value + pressOffset.value
                    translationY = pressOffset.value * 0.5f
                }
                .then(
                    if (item.isLive) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(Color.Red, Color.White, Color.Blue)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                    } else Modifier
                )
                .clickable {
                    isClicked = true
                    onClick()
                },
        shape = RoundedCornerShape(32.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    when {
                        isClicked -> 12.dp
                        isHovered -> 8.dp
                        else -> 4.dp
                    }
            ),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isClicked) darkerColor else cardColor
            ),
        border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (item.isLive) {
                    KidsTvThailandLogo(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2D3047))
                    )
                    LiveBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                } else {
                    FadeInImage(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp
                                )
                            )
                    )
                }
            }

            Text(
                text = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PulsingImage(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse transition")
    val pulseScale by
    infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
        label = "pulse scale"
    )

    Box(
        modifier =
            modifier.background(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                    )
            )
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun FadeInImage(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(false) }
    val alpha by
    animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "fade in animation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier =
            modifier.background(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                    )
            )
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .graphicsLayer {
                        this.alpha = alpha
                    },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun KidsTvThailandLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo rotation"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // "Kids" Row with 3D blocks
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LogoBlock(char = "K", color = Color(0xFF0055AA))
                LogoBlock(char = "i", color = Color(0xFF0055AA))
                LogoBlock(char = "d", color = Color(0xFF0055AA))
                LogoBlock(char = "s", color = Color(0xFF0055AA))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "tv" blocks
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LogoBlock(
                    char = "t",
                    color = Color.White,
                    bgColor = Color(0xFF003388),
                    size = 45.dp
                )
                LogoBlock(
                    char = "v",
                    color = Color.White,
                    bgColor = Color(0xFF003388),
                    size = 45.dp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "CHANNEL Thailand" banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFE0E0E0))
                        )
                    )
                    .border(2.dp, Color(0xFF003388), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "CHANNEL Thailand",
                    color = Color(0xFF003388),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun LogoBlock(
    char: String,
    color: Color,
    bgColor: Color = Color.White,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                shadowElevation = 8f
                shape = RoundedCornerShape(8.dp)
                clip = true
            }
            .background(
                Brush.verticalGradient(
                    listOf(Color.Red, bgColor)
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            color = color,
            fontSize = (size.value * 0.7f).sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "live badge")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red.copy(alpha = alpha))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LIVE",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            interactionSource = MutableInteractionSource(),
            indication = null
        ) { onClick() }
    )

data class MenuItem(
    val id: Int,
    val title: String,
    val iconRes: Int,
    val targetActivity: Int,
    val isLive: Boolean = false
)

enum class ViewMode {
    GRID,
    LIST
}

private fun getMenuItems(): List<MenuItem> {
    return listOf(
        MenuItem(1, "ฝึกอ่าน",         R.drawable.doing_learn_01,  0),
        MenuItem(2, "ฝึกเขียน ก - ฮ", R.drawable.card_one,        1),
        MenuItem(3, "ฝึกเขียน A - Z", R.drawable.card_two,        2),
        MenuItem(4, "กิจกรรมเสริม",   R.drawable.card_three,      3),
        MenuItem(5, "บทความน่ารู้",   R.drawable.leraning_kid,    4),
        MenuItem(6, "แบบทดสอบ",      R.drawable.card_three_two,  5),
        // ── เมนูใหม่ ──
        MenuItem(7, "ดู DLTV Live",   R.drawable.card_four,       6, isLive = true),
        MenuItem(8, "วาดระบายสี",    R.drawable.home_color,      7),
    )
}

private fun handleItemClickDirectly(
    context: Context,
    item: MenuItem,
    navController: NavController?
) {
    when (item.targetActivity) {
        0 -> {
            val intent = Intent(context, LeaningActvity::class.java)
            context.startActivity(intent)
        }

        1 -> {
            val intent = Intent(context, MenuThaiLettersActivity::class.java)
            context.startActivity(intent)
        }

        2 -> {
            val intent = Intent(context, MenuActivityAZ::class.java)
            context.startActivity(intent)
        }

        3 -> {
            showKidsActivitiesDialog(context)
        }

        4 -> {
            val intent =
                Intent(context, KidsWebViewActivity::class.java).apply {
                    putExtra("extra_url", "https://www.youngciety.com/")
                    putExtra("extra_title", "Youngciety - สนุกสนาน")
                }
            context.startActivity(intent)
        }

        5 -> {
            val intent = Intent(context, QuizActivity::class.java)
            context.startActivity(intent)
        }

        // ── เมนูใหม่: ดู DLTV Live ──
        6 -> {
            val intent = Intent(context, LiveListActivity::class.java)
            context.startActivity(intent)
        }

        // ── เมนูใหม่: วาดระบายสี (ColoringGameActivity) ──
        7 -> {
            val intent = Intent(context, ColoringGameActivity::class.java)
            context.startActivity(intent)
        }
    }
}

private fun showAdSuccessMessage(context: Context) {
    Toast.makeText(context, "🙏 ขอบคุณสำหรับการสนับสนุน!", Toast.LENGTH_SHORT).show()
}

private fun showAdFailedMessage(context: Context) {
    Toast.makeText(context, "⚠️ โฆษณาไม่พร้อมใช้งาน", Toast.LENGTH_SHORT).show()
}

private fun showKidsActivitiesDialog(context: Context) {
    val activities =
        listOf(
            "🎨 ระบายสีสร้างสรรค์" to { startColoringActivity(context) },
            "🔤 เกมจับคู่ตัวอักษร" to { startAbcMatchGame(context) }
        )

    val layout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

    val titleText =
        TextView(context).apply {
            text = "เลือกกิจกรรมที่สนใจ:"
            textSize = 18f
            setTextColor(android.graphics.Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
    layout.addView(titleText)

    activities.forEach { (activityName, onClick) ->
        val button =
            android.widget.Button(context).apply {
                text = activityName
                textSize = 16f
                setBackgroundColor(getRandomColor())
                setTextColor(android.graphics.Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(20, 30, 20, 30)

                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                        .apply { setMargins(0, 8, 0, 8) }

                setOnClickListener { onClick() }
            }
        layout.addView(button)
    }

    AlertDialog.Builder(context)
        .setTitle("🎪 กิจกรรมเสริมสำหรับเด็ก")
        .setView(layout)
        .setPositiveButton("ปิด", null)
        .show()
}

private fun startColoringActivity(context: Context) {
    val intent = Intent(context, ColoringGameActivity::class.java)
    context.startActivity(intent)
}

private fun startAbcMatchGame(context: Context) {
    val intent = Intent(context, AbcMatchGameActivity::class.java)
    context.startActivity(intent)
}

private fun getRandomColor(): Int {
    val colors =
        listOf(
            android.graphics.Color.parseColor("#FF6B6B"),
            android.graphics.Color.parseColor("#4ECDC4"),
            android.graphics.Color.parseColor("#FFD166"),
            android.graphics.Color.parseColor("#06D6A0"),
            android.graphics.Color.parseColor("#118AB2"),
            android.graphics.Color.parseColor("#7209B7")
        )
    return colors.random()
}

private fun showSettingsDialog(
    context: Context,
    isMusicEnabled: Boolean,
    onMusicToggle: (Boolean) -> Unit
) {
    val options = arrayOf("เสียงดนตรีพื้นหลัง")
    val checkedItems = booleanArrayOf(isMusicEnabled)

    android.app.AlertDialog.Builder(context)
        .setTitle("ตั้งค่า")
        .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
            if (which == 0) {
                onMusicToggle(isChecked)
            }
        }
        .setPositiveButton("ตกลง", null)
        .show()
}

@Preview(showBackground = true)
@Composable
fun PreviewMainComposeScreen() {
    MainComposeScreen()
}
