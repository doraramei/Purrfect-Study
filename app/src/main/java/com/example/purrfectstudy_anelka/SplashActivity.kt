package com.example.purrfectstudy_anelka

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashHandler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        val targetClass = if (StudyRepository.isLoggedIn(this)) {
            HomeActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, targetClass))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashRoot = findViewById<LinearLayout>(R.id.splashRoot)
        splashRoot.alpha = 0f
        splashRoot.animate()
            .alpha(1f)
            .setDuration(450L)
            .start()

        splashHandler.postDelayed(navigateRunnable, 1200L)
    }

    override fun onDestroy() {
        super.onDestroy()
        splashHandler.removeCallbacks(navigateRunnable)
    }
}
