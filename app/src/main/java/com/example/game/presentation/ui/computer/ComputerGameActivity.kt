package com.example.game.presentation.ui.computer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.ads.BannerAdController
import com.example.game.presentation.ui.ads.NetworkUtils
import com.example.game.presentation.ui.game.BoardView
import com.example.game.presentation.ui.game.DiceAnimationHelper
import com.example.game.presentation.ui.settings.SettingsActivity
import com.example.game.presentation.ui.win.WinActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class ComputerGameActivity : AppCompatActivity() {

    private lateinit var boardView: BoardView
    private lateinit var tvCurrentPlayer: TextView
    private lateinit var tvDiceResult: TextView
    private lateinit var tvDiceFace: TextView
    private lateinit var btnRollDice: Button
    private lateinit var adView: AdView
    private lateinit var bannerAdController: BannerAdController

    // 0 = Human (P1🔴), 1 = Computer (P2🟡)
    private var currentPlayerIndex = 0
    private var playerPositions    = mutableListOf(1, 1, 1, 1)
    private var isAnimating        = false

    private val STEP_DELAY_MS   = 300L
    private val SNAKE_PAUSE_MS  = 700L
    private val LADDER_PAUSE_MS = 600L

    private val diceEmojis = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

    private val snakes = mapOf(
        99 to 21, 87 to 24, 73 to 44,
        62 to 19, 49 to 11, 36 to 6, 27 to 5
    )
    private val ladders = mapOf(
        3 to 20, 4 to 38, 9 to 31,
        16 to 26, 22 to 42, 28 to 84,
        51 to 67, 71 to 91, 78 to 98
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
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

        boardView       = findViewById(R.id.boardView)
        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer)
        tvDiceResult    = findViewById(R.id.tvDiceResult)
        tvDiceFace      = findViewById(R.id.tvDiceFace)
        btnRollDice     = findViewById(R.id.btnRollDice)
        adView          = findViewById(R.id.adView)
        bannerAdController = BannerAdController(this, adView, ::isAdsEnabled)

        boardView.playerCount     = 2
        boardView.playerPositions = playerPositions

        AdManager.preload(this)
        bannerAdController.start()

        updateTurnUI()

        btnRollDice.setOnClickListener {
            if (isAnimating || currentPlayerIndex != 0) return@setOnClickListener
            btnRollDice.isEnabled = false
            vibratePhone(50)
            DiceAnimationHelper.animateRollStart(btnRollDice, tvDiceFace)
            rollDice(isComputer = false)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { showExitDialog() }
        })
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this, R.style.ExitDialogTheme)
            .setTitle("🚪 Exit Game?")
            .setMessage("Your progress will not be saved.\nAre you sure you want to quit?")
            .setPositiveButton("Yes, Quit") { _, _ -> finish() }
            .setNegativeButton("No, Continue") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun rollDice(isComputer: Boolean) {
        var count = 0
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                DiceAnimationHelper.spinDiceFace(tvDiceFace, diceEmojis.random(), 100L)
                count++
                if (count < 12) {
                    handler.postDelayed(this, 80)
                } else {
                    val dice = (1..6).random()
                    DiceAnimationHelper.showFinalDice(tvDiceFace, diceEmojis[dice - 1])
                    handler.postDelayed({ startMove(dice) }, 600)
                }
            }
        }
        handler.post(runnable)
    }

    private fun startMove(steps: Int) {
        val from = playerPositions[currentPlayerIndex]
        val raw  = from + steps
        if (raw > 100) {
            tvDiceResult.text = "Too far! Need exact number 🎯"
            vibratePhone(80)
            nextTurn()
            return
        }
        isAnimating = true
        animateSteps(currentPlayerIndex, from, raw, 0, steps)
    }

    private fun animateSteps(idx: Int, from: Int, target: Int, stepDone: Int, total: Int) {
        if (stepDone >= total) {
            onReachedCell(idx, target)
            return
        }
        playerPositions[idx] = from + stepDone + 1
        boardView.playerPositions = playerPositions
        boardView.invalidate()
        boardView.postDelayed({
            animateSteps(idx, from, target, stepDone + 1, total)
        }, STEP_DELAY_MS)
    }

    private fun onReachedCell(idx: Int, cell: Int) {
        when {
            cell == 100 -> {
                playerPositions[idx] = 100
                boardView.playerPositions = playerPositions
                boardView.invalidate()
                vibratePhone(300)
                isAnimating = false
                AdManager.showIfReady(this) {
                    startActivity(Intent(this, WinActivity::class.java).apply {
                        putExtra("WINNER", idx + 1)
                        putExtra("WINNER_NAME", if (idx == 0) "You" else "Computer")
                    })
                    finish()
                }
            }
            snakes.containsKey(cell) -> {
                val tail = snakes[cell]!!
                tvDiceResult.text = "🐍 Snake! $cell → $tail"
                vibratePhone(200)
                boardView.postDelayed({
                    playerPositions[idx] = tail
                    boardView.playerPositions = playerPositions
                    boardView.invalidate()
                    boardView.postDelayed({ isAnimating = false; nextTurn() }, SNAKE_PAUSE_MS)
                }, 350L)
            }
            ladders.containsKey(cell) -> {
                val top = ladders[cell]!!
                tvDiceResult.text = "🪜 Ladder! $cell → $top"
                vibratePhone(100)
                boardView.postDelayed({
                    playerPositions[idx] = top
                    boardView.playerPositions = playerPositions
                    boardView.invalidate()
                    boardView.postDelayed({ isAnimating = false; nextTurn() }, LADDER_PAUSE_MS)
                }, 350L)
            }
            else -> {
                tvDiceResult.text = "➡ Moved to $cell"
                isAnimating = false
                Handler(Looper.getMainLooper()).postDelayed({ nextTurn() }, 500L)
            }
        }
    }

    private fun nextTurn() {
        currentPlayerIndex = if (currentPlayerIndex == 0) 1 else 0
        updateTurnUI()
        if (currentPlayerIndex == 1) {
            Handler(Looper.getMainLooper()).postDelayed({
                DiceAnimationHelper.animateRollStart(btnRollDice, tvDiceFace)
                rollDice(isComputer = true)
            }, 1000L)
        } else {
            btnRollDice.isEnabled = true
        }
    }

    private fun updateTurnUI() {
        if (currentPlayerIndex == 0) {
            tvCurrentPlayer.text  = "🎯 🔴 Your Turn"
            btnRollDice.isEnabled = !isAnimating
            btnRollDice.alpha     = 1f
        } else {
            tvCurrentPlayer.text  = "🤖 🟡 Computer's Turn..."
            btnRollDice.isEnabled = false
            btnRollDice.alpha     = 0.5f
        }
    }

    private fun isAdsEnabled(): Boolean {
        return try {
            val config = Firebase.remoteConfig
            val value = config.getValue("show_ads")
            if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) true
            else value.asBoolean()
        } catch (e: Exception) {
            true
        }
    }

    private fun loadBannerAd() {
        if (!isAdsEnabled()) { adView.visibility = android.view.View.GONE; return }
        if (!NetworkUtils.isAvailable(this)) { adView.visibility = android.view.View.GONE; return }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() { adView.visibility = android.view.View.VISIBLE }
            override fun onAdFailedToLoad(error: LoadAdError) { adView.visibility = android.view.View.GONE }
        }

        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            adView.visibility = android.view.View.GONE
        }
    }

    private fun vibratePhone(durationMs: Long) {
        if (!SettingsActivity.isVibrationEnabled(this)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(durationMs)
        }
    }

    override fun onResume() {
        super.onResume()
        bannerAdController.resume()
        AdManager.preload(this)
    }

    override fun onPause() {
        bannerAdController.pause()
        super.onPause()
    }

    override fun onDestroy() {
        bannerAdController.destroy()
        super.onDestroy()
    }
}
