package com.example.movieinfo.ui.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.movieinfo.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class TrailerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trailer)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        val videoKey = intent.getStringExtra("VIDEO_KEY") ?: return

        val youTubePlayerView = findViewById<YouTubePlayerView>(R.id.youtubePlayerView)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)

        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val fadeOut = AlphaAnimation(1f, 0f).apply {
                    duration = 500
                    fillAfter = true
                }
                loadingOverlay.startAnimation(fadeOut)

                loadingOverlay.postDelayed({
                    loadingOverlay.visibility = View.GONE
                }, 500)

                youTubePlayer.loadVideo(videoKey, 0f)
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
