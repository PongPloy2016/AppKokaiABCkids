package com.pongploydev.kidswebviewfeature.compose

import android.os.Build
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsWebViewScreen(
    url: String = "https://www.youngciety.com/",
    title: String = "Youngciety",
    onBackClick: () -> Unit = {}
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentTitle by remember { mutableStateOf(title) }
    var pageTitle by remember { mutableStateOf(title) }

    var canGoBack by remember { mutableStateOf(false) }

    val handleBackPress = {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
            canGoBack = webView?.canGoBack() == true
        } else {
            canGoBack = false
            onBackClick()
        }
    }

    // Intercept system back button / gesture ONLY when webView has back history
    BackHandler(enabled = canGoBack) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
            canGoBack = webView?.canGoBack() == true
        } else {
            canGoBack = false
            onBackClick()
        }
    }

    // Handle JavaScript events
    val onJavaScriptEvent = { type: String, data: String ->
        when (type) {
            "title" -> {
                pageTitle = data
                if (data.isNotEmpty() && data != "Youngciety") {
                    currentTitle = data
                }
            }
            "button_click" -> {
                println("Button clicked: $data")
                // Handle button clicks from web
            }
            "video_play" -> {
                println("Video playing: $data")
                // Handle video playback
            }
            "analytics" -> {
                println("Analytics: $data")
                // Send analytics data
            }
        }
    }

    // Inject custom JavaScript when screen loads
    LaunchedEffect(Unit) {
        // You can add initial JavaScript injection here if needed
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            KidsWebViewTopBar(
                title = currentTitle,
                isLoading = isLoading,
                progress = progress,
                onBackClick = {
                    handleBackPress()
                },
                onCloseClick = onBackClick,
                onRefreshClick = {
                    isLoading = true
                    progress = 0
                    errorMessage = null
                    webView?.reload()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            ComposeWebView(
                url = url,
                modifier = Modifier.fillMaxSize(),
                onCreated = { instance ->
                    webView = instance
                    canGoBack = instance.canGoBack()
                },
                onPageStarted = {
                    isLoading = true
                    progress = 0
                    errorMessage = null
                    canGoBack = webView?.canGoBack() == true
                },
                onPageFinished = {
                    isLoading = false
                    progress = 100
                    canGoBack = webView?.canGoBack() == true
                },
                onReceivedError = { error ->
                    errorMessage = error ?: "เกิดข้อผิดพลาดในการโหลดหน้าเว็บ"
                    isLoading = false
                },
                onProgressChanged = { newProgress ->
                    progress = newProgress
                    if (newProgress == 100) {
                        isLoading = false
                    }
                },
                onUrlLoading = { targetUrl ->
                    // Allow only youngciety.com domains and subdomains
                    val allowedDomains = listOf(
                        "youngciety.com",
                        "www.youngciety.com",
                        "games.youngciety.com",
                        "videos.youngciety.com"
                    )
                    val isAllowed = allowedDomains.any { domain ->
                        targetUrl.contains(domain)
                    }
                    !isAllowed // Return true to block, false to allow
                },
                onTitleReceived = { newTitle ->
                    pageTitle = newTitle
                    if (newTitle.isNotEmpty() && newTitle != "Youngciety") {
                        currentTitle = newTitle
                    }
                },
                onJavaScriptEvent = onJavaScriptEvent
            )

            // Loading Indicator
            if (isLoading && progress < 100) {
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Error Message
            errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "😢",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "ไม่สามารถโหลดหน้าเว็บได้",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = message,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        // Retry button
                        androidx.compose.material3.Button(
                            onClick = {
                                isLoading = true
                                progress = 0
                                errorMessage = null
                                webView?.reload() ?: webView?.loadUrl(url)
                            },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "ลองอีกครั้ง",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsWebViewTopBar(
    title: String,
    isLoading: Boolean,
    progress: Int,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit = onBackClick,
    onRefreshClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "ย้อนกลับ",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "รีเฟรช",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onCloseClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "ปิด",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )

        // Progress Bar
        if (isLoading && progress < 100) {
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
