package com.example.game.presentation.ui.online

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.game.presentation.ui.win.WinActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var boardView: BoardView
    private lateinit var tvCurrentPlayer: TextView
    private lateinit var tvDiceResult: TextView
    private lateinit var tvDiceFace: TextView
    private lateinit var tvOpponentStatus: TextView
    private lateinit var btnRollDice: Button
    private lateinit var adView: AdView
    private lateinit var bannerAdController: BannerAdController

    private var roomCode  = ""
    private var isHost    = false
    private var myTurn    = false
    private var isAnimating = false

    private var roomRef: DatabaseReference? = null
    private var gameListener: ValueEventListener? = null

    private val diceEmojis = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

    private val STEP_DELAY_MS   = 300L
    private val SNAKE_PAUSE_MS  = 700L
    private val LADDER_PAUSE_MS = 600L

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
        setContentView(R.layout.activity_online_game)
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

        roomCode = intent.getStringExtra("ROOM_CODE") ?: ""
        isHost   = intent.getBooleanExtra("IS_HOST", false)

        boardView        = findViewById(R.id.boardView)
        tvCurrentPlayer  = findViewById(R.id.tvCurrentPlayer)
        tvDiceResult     = findViewById(R.id.tvDiceResult)
        tvDiceFace       = findViewById(R.id.tvDiceFace)
        tvOpponentStatus = findViewById(R.id.tvOpponentStatus)
        btnRollDice      = findViewById(R.id.btnRollDice)
        adView           = findViewById(R.id.adView)
        bannerAdController = BannerAdController(this, adView, ::isAdsEnabled)

        boardView.playerCount     = 2
        boardView.playerPositions = mutableListOf(1, 1, 1, 1)

        AdManager.preload(this)
        bannerAdController.start()

        roomRef = FirebaseDatabase.getInstance().reference.child("rooms").child(roomCode)

        gameListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val p1Pos       = snapshot.child("p1Pos").getValue(Int::class.java) ?: 1
                val p2Pos       = snapshot.child("p2Pos").getValue(Int::class.java) ?: 1
                val currentTurn = snapshot.child("currentTurn").getValue(Int::class.java) ?: 0
                val lastDice    = snapshot.child("lastDice").getValue(Int::class.java) ?: 0
                val winner      = snapshot.child("winner").getValue(Int::class.java) ?: -1
                val hostName    = snapshot.child("hostName").getValue(String::class.java) ?: "Host"
                val guestName   = snapshot.child("guestName").getValue(String::class.java) ?: "Guest"

                boardView.playerPositions = mutableListOf(p1Pos, p2Pos, 1, 1)
                boardView.invalidate()

                if (winner != -1) {
                    gameListener?.let { roomRef?.removeEventListener(it) }
                    startActivity(Intent(this@OnlineGameActivity, WinActivity::class.java).apply {
                        putExtra("WINNER", winner)
                        putExtra("WINNER_NAME", if (winner == 1) hostName else guestName)
                    })
                    finish()
                    return
                }

                if (lastDice > 0) tvDiceFace.text = diceEmojis[lastDice - 1]

                myTurn = (isHost && currentTurn == 0) || (!isHost && currentTurn == 1)

                if (isHost) {
                    tvOpponentStatus.text = "🟡 Opponent: $guestName"
                    tvCurrentPlayer.text  = if (myTurn) "🎯 🔴 $hostName's Turn" else "⏳ 🟡 $guestName's Turn..."
                } else {
                    tvOpponentStatus.text = "🔴 Opponent: $hostName"
                    tvCurrentPlayer.text  = if (myTurn) "🎯 🟡 $guestName's Turn" else "⏳ 🔴 $hostName's Turn..."
                }

                btnRollDice.isEnabled = myTurn && !isAnimating
                btnRollDice.alpha     = if (myTurn && !isAnimating) 1f else 0.5f
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        roomRef?.addValueEventListener(gameListener!!)

        btnRollDice.setOnClickListener {
            if (!myTurn || isAnimating) return@setOnClickListener
            btnRollDice.isEnabled = false
            DiceAnimationHelper.animateRollStart(btnRollDice, tvDiceFace)
            rollDiceOnline()
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

    private fun rollDiceOnline() {
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
                    handler.postDelayed({ movePlayerOnline(dice) }, 600)
                }
            }
        }
        handler.post(runnable)
    }

    private fun movePlayerOnline(steps: Int) {
        roomRef?.get()?.addOnSuccessListener { snapshot ->
            val currentTurn = snapshot.child("currentTurn").getValue(Int::class.java) ?: 0
            val posKey      = if (isHost) "p1Pos" else "p2Pos"
            val currentPos  = snapshot.child(posKey).getValue(Int::class.java) ?: 1
            val nextTurn    = if (currentTurn == 0) 1 else 0
            val playerIndex = if (isHost) 0 else 1
            val rawPos      = currentPos + steps

            roomRef?.child("lastDice")?.setValue(steps)

            if (rawPos > 100) {
                tvDiceResult.text = "Too far! Need exact number 🎯"
                roomRef?.child("currentTurn")?.setValue(nextTurn)
                return@addOnSuccessListener
            }

            isAnimating = true
            btnRollDice.isEnabled = false
            animateOnlineSteps(playerIndex, currentPos, rawPos, 0, steps, posKey, nextTurn)
        }
    }

    private fun animateOnlineSteps(
        playerIndex: Int, currentPos: Int, targetPos: Int,
        stepNo: Int, totalSteps: Int, posKey: String, nextTurn: Int
    ) {
        if (stepNo >= totalSteps) {
            handleOnlineFinalCell(playerIndex, targetPos, posKey, nextTurn)
            return
        }
        boardView.playerPositions[playerIndex] = currentPos + stepNo + 1
        boardView.invalidate()
        boardView.postDelayed({
            animateOnlineSteps(playerIndex, currentPos, targetPos, stepNo + 1, totalSteps, posKey, nextTurn)
        }, STEP_DELAY_MS)
    }

    private fun handleOnlineFinalCell(playerIndex: Int, cell: Int, posKey: String, nextTurn: Int) {
        when {
            cell == 100 -> {
                boardView.playerPositions[playerIndex] = 100
                boardView.invalidate()
                roomRef?.updateChildren(mapOf(
                    posKey to 100,
                    "winner" to if (isHost) 1 else 2,
                    "currentTurn" to nextTurn
                ))
                isAnimating = false
            }
            snakes.containsKey(cell) -> {
                val tail = snakes[cell]!!
                tvDiceResult.text = "🐍 Snake! $cell → $tail"
                boardView.postDelayed({
                    boardView.playerPositions[playerIndex] = tail
                    boardView.invalidate()
                    boardView.postDelayed({
                        roomRef?.updateChildren(mapOf(posKey to tail, "currentTurn" to nextTurn))
                        isAnimating = false
                    }, SNAKE_PAUSE_MS)
                }, 350L)
            }
            ladders.containsKey(cell) -> {
                val top = ladders[cell]!!
                tvDiceResult.text = "🪜 Ladder! $cell → $top"
                boardView.postDelayed({
                    boardView.playerPositions[playerIndex] = top
                    boardView.invalidate()
                    boardView.postDelayed({
                        roomRef?.updateChildren(mapOf(posKey to top, "currentTurn" to nextTurn))
                        isAnimating = false
                    }, LADDER_PAUSE_MS)
                }, 350L)
            }
            else -> {
                tvDiceResult.text = "➡ Moved to $cell"
                roomRef?.updateChildren(mapOf(posKey to cell, "currentTurn" to nextTurn))
                isAnimating = false
            }
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
        gameListener?.let { roomRef?.removeEventListener(it) }
        super.onDestroy()
    }
}
