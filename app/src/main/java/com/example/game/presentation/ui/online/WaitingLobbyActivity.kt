package com.example.game.presentation.ui.online

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.ads.BannerAdController
import com.example.game.presentation.ui.ads.NetworkUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class WaitingLobbyActivity : AppCompatActivity() {

    private var roomRef: DatabaseReference? = null
    private var lobbyListener: ValueEventListener? = null
    private var gameStarted = false
    private lateinit var adView: AdView
    private lateinit var bannerAdController: BannerAdController
    private lateinit var tvStatus: TextView
    private lateinit var tvRoomCode: TextView

    private var roomCode = ""
    private var isHost   = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_lobby)
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

        roomCode   = intent.getStringExtra("ROOM_CODE") ?: "------"
        isHost     = intent.getBooleanExtra("IS_HOST", false)
        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvStatus   = findViewById(R.id.tvStatus)
        adView     = findViewById(R.id.adView)
        bannerAdController = BannerAdController(this, adView, ::isAdsEnabled)

        tvRoomCode.text = "Room: $roomCode"
        tvStatus.text   = if (isHost) "⏳ Waiting for opponent..." else "⏳ Joining room..."

        AdManager.preload(this)
        bannerAdController.start()

        roomRef = FirebaseDatabase.getInstance().reference.child("rooms").child(roomCode)

        if (isHost) {
            lobbyListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val guestName = snapshot.child("guestName").getValue(String::class.java)
                    if (!guestName.isNullOrEmpty() && !gameStarted) {
                        tvStatus.text = "✅ $guestName joined! Starting..."
                        gameStarted = true
                        lobbyListener?.let { roomRef?.removeEventListener(it) }
                        tvRoomCode.postDelayed({ startGame() }, 150)
                    }
                }
                // ✅ Fix 1 — Network cut dialog
                override fun onCancelled(e: DatabaseError) {
                    showNetworkLostDialog()
                }
            }
            roomRef?.addValueEventListener(lobbyListener!!)

        } else {
            roomRef?.child("hostName")
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val hostName = snapshot.getValue(String::class.java)
                        if (!hostName.isNullOrEmpty()) {
                            tvStatus.text = "✅ Room found! Starting..."
                            tvRoomCode.postDelayed({ startGame() }, 150)
                        } else {
                            tvStatus.text = "❌ Room not found!"
                        }
                    }
                    override fun onCancelled(e: DatabaseError) {
                        tvStatus.text = "❌ Connection error!"
                        showNetworkLostDialog()
                    }
                })
        }

        // ✅ Fix 2 — Hardware back button handle
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

        findViewById<View>(R.id.btnBack).setOnClickListener   { showExitDialog() }
        findViewById<View>(R.id.btnCancel).setOnClickListener { showExitDialog() }
    }

    // ══════════ DIALOGS ══════════

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("🚪 Leave Lobby?")
            .setMessage(
                if (isHost) "Leaving will delete the room.\nAre you sure?"
                else        "Leaving will cancel joining.\nAre you sure?"
            )
            .setPositiveButton("Yes, Leave") { _, _ -> cleanupAndFinish() }
            .setNegativeButton("Stay")       { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showNetworkLostDialog() {
        if (isFinishing) return
        AlertDialog.Builder(this)
            .setTitle("📶 Connection Lost!")
            .setMessage("Internet connection was lost.\nPlease check WiFi or Mobile Data.")
            .setPositiveButton("Exit") { _, _ -> cleanupAndFinish() }
            .setCancelable(false)
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

    // ══════════ BANNER AD ══════════
    private fun loadBannerAd() {
        if (!NetworkUtils.isAvailable(this)) {
            adView.visibility = View.GONE
            return
        }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                adView.visibility = View.GONE
            }
        }
        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            adView.visibility = View.GONE
        }
    }

    // ══════════ LIFECYCLE ══════════
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
        lobbyListener?.let { roomRef?.removeEventListener(it) }
        super.onDestroy()
    }

    // ══════════ NAVIGATION ══════════
    private fun startGame() {
        if (isFinishing) return
        val intent = Intent(this, OnlineGameActivity::class.java)
        intent.putExtra("ROOM_CODE", roomCode)
        intent.putExtra("IS_HOST", isHost)
        startActivity(intent)
        finish()
    }

    // ✅ Fix 4 — Sirf HOST room delete kare, Guest nahi
    private fun cleanupAndFinish() {
        lobbyListener?.let { roomRef?.removeEventListener(it) }
        if (isHost) roomRef?.removeValue()
        finish()
    }
}
