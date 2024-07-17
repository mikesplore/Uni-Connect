package com.mike.uniadmin


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mike.uniadmin.CommonComponents as CC

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(link: String,) {
    var backEnabled by remember { mutableStateOf(false) }
    var forwardEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxHeight()) { // Ensure it fills the parent container height
        var webViewInstance: WebView? by remember { mutableStateOf(null) }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            backEnabled = view?.canGoBack() ?: false
                            forwardEnabled = view?.canGoForward() ?: false
                        }

                        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                            backEnabled = view?.canGoBack() ?: false
                            forwardEnabled = view?.canGoForward() ?: false
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress / 100f
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl(link)
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize(), // Use fillMaxSize to take up the full available space
            update = { webView ->
                webViewInstance = webView
            }
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .background(CC.primary())
                    .fillMaxWidth()
                    .height(4.dp),
                color = CC.primary(),
            )
        }


    }
}

@Preview
@Composable
fun PreviewWebViewScreen() {
    Column(modifier = Modifier.height(500.dp)) {
        WebViewScreen("https://wakatime.com/dashboard")
    }
}