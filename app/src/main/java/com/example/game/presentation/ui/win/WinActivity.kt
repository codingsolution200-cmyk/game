package com.example.game.presentation.ui.win

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.ads.NetworkUtils
import com.example.game.presentation.ui.home.HomeActivity
import com.example.game.presentation.ui.setup.SetupActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class WinActivity : AppCompatActivity() {

    private val playerColorEmoji = listOf("🔴", "🟡", "🟢", "🟣")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win)
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

        val winnerNum   = intent.getIntExtra("WINNER", 1)
        val winnerName  = intent.getStringExtra("WINNER_NAME") ?: "Player $winnerNum"
        val emoji       = playerColorEmoji[(winnerNum - 1).coerceIn(0, 3)]

        val tvWinner     = findViewById<TextView>(R.id.tvWinner)
        val tvWinSub     = findViewById<TextView>(R.id.tvWinSub)
        val btnPlayAgain = findViewById<Button>(R.id.btnPlayAgain)
        val btnHome      = findViewById<Button>(R.id.btnHome)

        tvWinner.text = "🏆 $winnerName Wins!"
        tvWinSub.text = "$emoji Player $winnerNum • Winner"

        playEntryAnimation(tvWinner, tvWinSub, btnPlayAgain, btnHome)

        // Preload — sirf tab jab ads enabled ho aur internet ho
        if (isAdsEnabled() && NetworkUtils.isAvailable(this)) {
            AdManager.preload(this)
        }

        // PLAY AGAIN — Remote Config + Network check karke ad dikhao
        btnPlayAgain.setOnClickListener {
            btnPlayAgain.isEnabled = false
            btnHome.isEnabled = false
            if (isAdsEnabled() && NetworkUtils.isAvailable(this) && AdManager.isReady()) {
                AdManager.showIfReady(this) {
                    AdManager.preload(this)
                    startActivity(Intent(this, SetupActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this, SetupActivity::class.java))
                finish()
            }
        }

        // MAIN MENU — Remote Config + Network check karke ad dikhao
        btnHome.setOnClickListener {
            btnPlayAgain.isEnabled = false
            btnHome.isEnabled = false
            if (isAdsEnabled() && NetworkUtils.isAvailable(this) && AdManager.isReady()) {
                AdManager.showIfReady(this) {
                    AdManager.preload(this)
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    finish()
                }
            } else {
                startActivity(Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                finish()
            }
        }
    }

    // ══════════ REMOTE CONFIG — Ads Control ══════════
    private fun isAdsEnabled(): Boolean {
        return try {
            val value = Firebase.remoteConfig.getValue("show_ads")
            if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) true
            else value.asBoolean()
        } catch (e: Exception) {
            true
        }
    }

    // ══════════ ENTRY ANIMATIONS ══════════
    private fun playEntryAnimation(
        tvWinner: TextView,
        tvWinSub: TextView,
        btnPlayAgain: Button,
        btnHome: Button
    ) {
        tvWinner.scaleX = 0.3f
        tvWinner.scaleY = 0.3f
        tvWinner.alpha = 0f
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvWinner, "scaleX", 0.3f, 1f),
                ObjectAnimator.ofFloat(tvWinner, "scaleY", 0.3f, 1f),
                ObjectAnimator.ofFloat(tvWinner, "alpha", 0f, 1f)
            )
            duration = 600; interpolator = OvershootInterpolator(2.5f); start()
        }

        tvWinSub.translationY = 40f; tvWinSub.alpha = 0f
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvWinSub, "translationY", 40f, 0f),
                ObjectAnimator.ofFloat(tvWinSub, "alpha", 0f, 1f)
            )
            duration = 450; startDelay = 400; interpolator = DecelerateInterpolator(); start()
        }

        listOf(btnPlayAgain, btnHome).forEachIndexed { i, btn ->
            btn.translationY = 60f; btn.alpha = 0f
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(btn, "translationY", 60f, 0f),
                    ObjectAnimator.ofFloat(btn, "alpha", 0f, 1f)
                )
                duration = 400; startDelay = 600L + i * 120L
                interpolator = DecelerateInterpolator(); start()
            }
        }
    }
}
