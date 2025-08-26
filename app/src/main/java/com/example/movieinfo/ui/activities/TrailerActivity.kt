package com.example.movieinfo.ui.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.movieinfo.R

@Suppress("DEPRECATION")
class TrailerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingOverlay: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trailer)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        val videoKey = intent.getStringExtra("VIDEO_KEY") ?: return

        webView = findViewById(R.id.webView)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val fadeOut = AlphaAnimation(1f, 0f).apply {
                    duration = 500
                    fillAfter = true
                }
                loadingOverlay.startAnimation(fadeOut)

                loadingOverlay.postDelayed({
                    loadingOverlay.visibility = View.GONE
                }, 500)
            }
        }

        val videoUrl = "https://www.youtube.com/embed/$videoKey?autoplay=1&modestbranding=1&rel=0"
        webView.loadUrl(videoUrl)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
