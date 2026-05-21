package com.example.game.presentation.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.home.HomeActivity

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var lottieLoading: LottieAnimationView

    companion object {
        private const val SPLASH_DURATION_MS = 5000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawableResource(android.R.color.black)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FullscreenUtils.apply(window)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // ✅ Sirf lottieLoading — jo XML ma che
        lottieLoading = findViewById(R.id.lottieLoading)
        lottieLoading.speed = 1.0f
        lottieLoading.playAnimation()

        handler.postDelayed({
            goToHome()
        }, SPLASH_DURATION_MS)
    }

    private fun goToHome() {
        if (isFinishing) return
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Sirf lottieLoading cancel karo
        lottieLoading.cancelAnimation()
        handler.removeCallbacksAndMessages(null)
    }
}
