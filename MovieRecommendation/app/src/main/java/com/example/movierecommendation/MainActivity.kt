package com.example.movierecommendation  // ✅ Change to your actual package name

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {      // ✅ Make sure to extend AppCompatActivity

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)// ✅ R.layout will work only if XML exists

        val logo = findViewById<ImageView>(R.id.netflixLogo)

        // Animation
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 2000
            fillAfter = true
        }
        logo.startAnimation(fadeIn)

        // Play logo sound
        mediaPlayer = MediaPlayer.create(this, R.raw.netflix_intro)
        mediaPlayer?.start()

        // Navigate to Login after delay
        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer?.release()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3500)
    }
}
