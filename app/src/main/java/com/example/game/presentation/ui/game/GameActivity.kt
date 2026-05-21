package com.example.game.presentation.ui.game

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
import com.example.game.presentation.ui.settings.SettingsActivity
import com.example.game.presentation.ui.win.WinActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class GameActivity : AppCompatActivity() {

    private lateinit var boardView: BoardView
    private lateinit var tvCurrentPlayer: TextView
    private lateinit var tvDiceResult: TextView
    private lateinit var tvDiceFace: TextView
    private lateinit var btnRollDice: Button
    private lateinit var adView: AdView
    private lateinit var bannerAdController: BannerAdController

    private var playerCount = 2
    private var playerNames = mutableListOf("Player 1", "Player 2", "Player 3", "Player 4")
    private var playerPositions = mutableListOf(1, 1, 1, 1)
    private var currentPlayerIndex = 0
    private var isAnimating = false

    private val STEP_DELAY_MS      = 300L
    private val SNAKE_PAUSE_MS     = 700L
    private val LADDER_PAUSE_MS    = 600L
    private val DICE_SPIN_STEPS    = 14
    private val DICE_SPIN_START_MS = 50L
    private val DICE_SPIN_END_MS   = 180L

    private val diceEmojis = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

    private val snakes  = mapOf(99 to 21, 87 to 24, 73 to 44, 62 to 19, 49 to 11, 36 to 6, 27 to 5)
    private val ladders = mapOf(3 to 20, 4 to 38, 9 to 31, 16 to 26, 22 to 42, 28 to 84, 51 to 67, 71 to 91, 78 to 98)

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

        playerCount    = intent.getIntExtra("PLAYER_COUNT", 2)
        playerNames[0] = intent.getStringExtra("PLAYER1_NAME") ?: "Player 1"
        playerNames[1] = intent.getStringExtra("PLAYER2_NAME") ?: "Player 2"
        playerNames[2] = intent.getStringExtra("PLAYER3_NAME") ?: "Player 3"
        playerNames[3] = intent.getStringExtra("PLAYER4_NAME") ?: "Player 4"

        boardView.playerCount     = playerCount
        boardView.playerPositions = playerPositions

        AdManager.preload(this)
        bannerAdController.start()

        updateTurnUI()

        btnRollDice.setOnClickListener {
            if (isAnimating) return@setOnClickListener
            btnRollDice.isEnabled = false
            vibratePhone(50)
            DiceAnimationHelper.animateRollStart(btnRollDice, tvDiceFace)
            rollDice()
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

    private fun rollDice() {
        val handler = Handler(Looper.getMainLooper())
        var step = 0
        val finalDice = (1..6).random()

        fun scheduleNextSpin() {
            val progress = step.toFloat() / DICE_SPIN_STEPS
            val delay = (DICE_SPIN_START_MS + (DICE_SPIN_END_MS - DICE_SPIN_START_MS) * progress).toLong()
            handler.postDelayed({
                if (step < DICE_SPIN_STEPS) {
                    DiceAnimationHelper.spinDiceFace(tvDiceFace, diceEmojis.random(), 100L)
                    step++
                    scheduleNextSpin()
                } else {
                    DiceAnimationHelper.showFinalDice(tvDiceFace, diceEmojis[finalDice - 1])
                    handler.postDelayed({ startPlayerMove(finalDice) }, 900)
                }
            }, delay)
        }
        scheduleNextSpin()
    }

    private fun startPlayerMove(steps: Int) {
        val fromPos = playerPositions[currentPlayerIndex]
        val rawPos  = fromPos + steps
        if (rawPos > 100) {
            tvDiceResult.text = "Too far! Need exact number 🎯"
            vibratePhone(80)
            nextTurn()
            return
        }
        isAnimating = true
        animateStepByStep(currentPlayerIndex, fromPos, rawPos, 0, steps)
    }

    private fun animateStepByStep(playerIdx: Int, fromCell: Int, toCell: Int, stepsDone: Int, totalSteps: Int) {
        if (stepsDone >= totalSteps) {
            onReachedCell(playerIdx, toCell)
            return
        }
        playerPositions[playerIdx] = fromCell + stepsDone + 1
        boardView.playerPositions = playerPositions
        boardView.invalidate()
        boardView.postDelayed({
            animateStepByStep(playerIdx, fromCell, toCell, stepsDone + 1, totalSteps)
        }, STEP_DELAY_MS)
    }

    private fun onReachedCell(playerIdx: Int, cell: Int) {
        when {
            cell == 100 -> {
                playerPositions[playerIdx] = 100
                boardView.playerPositions = playerPositions
                boardView.invalidate()
                vibratePhone(300)
                isAnimating = false
                AdManager.showIfReady(this) { goToWinScreen(playerIdx) }
            }
            snakes.containsKey(cell) -> {
                val tail = snakes[cell]!!
                tvDiceResult.text = "🐍 Snake! $cell → $tail"
                vibratePhone(200)
                boardView.postDelayed({
                    playerPositions[playerIdx] = tail
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
                    playerPositions[playerIdx] = top
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
        currentPlayerIndex = (currentPlayerIndex + 1) % playerCount
        updateTurnUI()
        btnRollDice.isEnabled = true
    }

    private fun updateTurnUI() {
        val playerEmojis = listOf("🔴", "🟡", "🟢", "🟣")
        tvCurrentPlayer.text = "🎯 ${playerEmojis[currentPlayerIndex]} ${playerNames[currentPlayerIndex]}'s Turn"
    }

    private fun goToWinScreen(winnerIndex: Int) {
        startActivity(Intent(this, WinActivity::class.java).apply {
            putExtra("WINNER", winnerIndex + 1)
            putExtra("WINNER_NAME", playerNames[winnerIndex])
        })
        finish()
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
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                .vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
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
