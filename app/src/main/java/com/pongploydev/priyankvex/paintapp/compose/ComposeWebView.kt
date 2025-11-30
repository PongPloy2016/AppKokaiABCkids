package com.pongploydev.priyankvex.paintapp.compose

import android.webkit.WebView.setWebContentsDebuggingEnabled


import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ComposeWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: (() -> Unit)? = null,
    onPageFinished: (() -> Unit)? = null,
    onReceivedError: ((String?) -> Unit)? = null,
    onProgressChanged: ((Int) -> Unit)? = null,
    onUrlLoading: ((String) -> Boolean)? = null,
    onTitleReceived: ((String) -> Unit)? = null,
    onJavaScriptEvent: ((String, String) -> Unit)? = null
) {

    val webViewClient = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageStarted?.invoke()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke()

                // Inject custom JavaScript after page loads
                view?.evaluateJavascript("""
                    // Custom JavaScript for Youngciety
                    console.log('Youngciety WebView Loaded');
                    
                    // Add smooth scrolling for kids
                    document.body.style.scrollBehavior = 'smooth';
                    
                    // Make images more kid-friendly
                    const images = document.getElementsByTagName('img');
                    for(let img of images) {
                        img.style.borderRadius = '12px';
                        img.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
                    }
                    
                    // Add click effects for buttons
                    const buttons = document.querySelectorAll('button, a');
                    buttons.forEach(btn => {
                        btn.style.transition = 'all 0.3s ease';
                        btn.addEventListener('click', function() {
                            this.style.transform = 'scale(0.95)';
                            setTimeout(() => {
                                this.style.transform = 'scale(1)';
                            }, 150);
                        });
                    });
                    
                    // Send page title to Android
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onTitleReceived(document.title);
                    }
                    
                """.trimIndent(), null)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                onReceivedError?.invoke(description)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                return onUrlLoading?.invoke(url) ?: false
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return onUrlLoading?.invoke(url ?: "") ?: false
            }
        }
    }

    val webChromeClient = remember {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressChanged?.invoke(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onTitleReceived?.invoke(title ?: "")
            }

            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                consoleMessage?.let { message ->
                    println("WebView Console: ${message.message()}")
                }
                return true
            }
        }
    }

    // JavaScript Interface for communication
    class AndroidWebInterface(private val onEvent: (String, String) -> Unit) {
        @JavascriptInterface
        fun onTitleReceived(title: String) {
            onEvent("title", title)
        }

        @JavascriptInterface
        fun onButtonClicked(buttonId: String) {
            onEvent("button_click", buttonId)
        }

        @JavascriptInterface
        fun onVideoPlay(videoUrl: String) {
            onEvent("video_play", videoUrl)
        }

        @JavascriptInterface
        fun sendAnalytics(data: String) {
            onEvent("analytics", data)
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                this.webViewClient = webViewClient
                this.webChromeClient = webChromeClient

                // Enable JavaScript and advanced features
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                   // setAppCacheEnabled(true)
                    cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                    // For modern web
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    setSupportZoom(true)

                    // Security and performance
                    safeBrowsingEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true

                    // For responsive design
                    setGeolocationEnabled(true)
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportMultipleWindows(true)

                    // Mixed content (for HTTPS sites)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                }

                // Add JavaScript Interface
                addJavascriptInterface(
                    AndroidWebInterface { type, data ->
                        onJavaScriptEvent?.invoke(type, data)
                    },
                    "AndroidInterface"
                )

                // Enable debugging in development
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    setWebContentsDebuggingEnabled(true)
                }

                // Load the URL
                loadUrl(url)
            }
        },
        update = { webView ->
            // Update WebView if URL changes
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = modifier
    )
}