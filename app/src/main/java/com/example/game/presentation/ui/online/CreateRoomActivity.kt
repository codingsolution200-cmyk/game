package com.example.game.presentation.ui.online

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.NetworkUtils
import com.google.firebase.database.FirebaseDatabase
import com.example.game.presentation.ui.online.WaitingLobbyActivity


class CreateRoomActivity : AppCompatActivity() {

    private lateinit var roomCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)
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

        if (!NetworkUtils.isAvailable(this)) {
            showNoNetworkDialog()
            return
        }

        roomCode = generateRoomCode()
        findViewById<TextView>(R.id.tvRoomCode).text = roomCode

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.btnShare).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Join my Snake & Ladder game! Room Code: $roomCode"
            )
            startActivity(Intent.createChooser(shareIntent, "Share Room Code"))
        }

        findViewById<android.view.View>(R.id.btnStartOnline).setOnClickListener {

            if (!NetworkUtils.isAvailable(this)) {
                showNoNetworkDialog()
                return@setOnClickListener
            }

            val name = findViewById<EditText>(R.id.etPlayerName)
                .text.toString().trim().ifEmpty { "Player 1" }

            if (name.length < 2) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseDatabase.getInstance().reference
                .child("rooms").child(roomCode)
                .updateChildren(mapOf(
                    "hostName" to name,
                    "p1Pos"    to 1,
                    "p2Pos"    to 1,
                    "currentTurn" to 0,
                    "winner"   to -1,
                    "lastDice" to 0
                ))

            // ✅ Fixed — apply nahi, alag intent variable
            val intent = Intent(this, WaitingLobbyActivity::class.java)
            intent.putExtra("ROOM_CODE", roomCode)
            intent.putExtra("IS_HOST", true)
            intent.putExtra("HOST_NAME", name)
            startActivity(intent)
        }
    }

    private fun showNoNetworkDialog() {
        AlertDialog.Builder(this)
            .setTitle("📶 No Internet!")
            .setMessage("Online game requires an active internet connection.\n\nPlease turn on WiFi or Mobile Data and try again.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
