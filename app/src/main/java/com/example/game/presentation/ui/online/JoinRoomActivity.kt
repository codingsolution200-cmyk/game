package com.example.game.presentation.ui.online

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.ads.NetworkUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JoinRoomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_room)
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

        // Internet check — onCreate ma j
        if (!NetworkUtils.isAvailable(this)) {
            showNoNetworkDialog()
            return
        }

        AdManager.preload(this)

        val etRoomCode = findViewById<EditText>(R.id.etRoomCode)
        val etName     = findViewById<EditText>(R.id.etPlayerName)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.btnJoin).setOnClickListener {

            // Button click time pe bhi double check
            if (!NetworkUtils.isAvailable(this)) {
                showNoNetworkDialog()
                return@setOnClickListener
            }

            val code = etRoomCode.text.toString().trim().uppercase()
            val name = etName.text.toString().trim().ifEmpty { "Player 2" }

            if (code.length != 6) {
                Toast.makeText(this, "Please enter a valid 6-digit room code!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.length < 2) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roomRef = FirebaseDatabase.getInstance().reference
                .child("rooms").child(code)

            roomRef.child("hostName").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hostName = snapshot.getValue(String::class.java)

                    if (hostName.isNullOrEmpty()) {
                        Toast.makeText(
                            this@JoinRoomActivity,
                            "Room not found! Check the code.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    roomRef.child("guestName").setValue(name)

                    startActivity(
                        Intent(this@JoinRoomActivity, WaitingLobbyActivity::class.java).apply {
                            putExtra("ROOM_CODE", code)
                            putExtra("IS_HOST", false)
                            putExtra("GUEST_NAME", name)
                        }
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@JoinRoomActivity,
                        "Connection error! Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
}
