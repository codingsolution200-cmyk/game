package com.example.game.presentation.ui.online

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.ads.NetworkUtils

class OnlineMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_menu)
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

        AdManager.preload(this)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.btnCreateRoom).setOnClickListener {
            if (!NetworkUtils.isAvailable(this)) {
                showNoNetworkDialog()
                return@setOnClickListener
            }
            startActivity(Intent(this, CreateRoomActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnJoinRoom).setOnClickListener {
            if (!NetworkUtils.isAvailable(this)) {
                showNoNetworkDialog()
                return@setOnClickListener
            }
            startActivity(Intent(this, JoinRoomActivity::class.java))
        }
    }

    private fun showNoNetworkDialog() {
        AlertDialog.Builder(this)
            .setTitle("📶 No Internet!")
            .setMessage("Online game requires an active internet connection.\n\nPlease turn on WiFi or Mobile Data and try again.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }
}
