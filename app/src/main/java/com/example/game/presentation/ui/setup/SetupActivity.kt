package com.example.game.presentation.ui.setup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.game.GameActivity

class SetupActivity : AppCompatActivity() {

    private var playerCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
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

        val tvCount   = findViewById<TextView>(R.id.tvPlayerCount)
        val etPlayer1 = findViewById<EditText>(R.id.etPlayer1)
        val etPlayer2 = findViewById<EditText>(R.id.etPlayer2)
        val etPlayer3 = findViewById<EditText>(R.id.etPlayer3)
        val etPlayer4 = findViewById<EditText>(R.id.etPlayer4)
        val labelP3   = findViewById<TextView>(R.id.labelPlayer3)
        val labelP4   = findViewById<TextView>(R.id.labelPlayer4)

        AdManager.preload(this)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.btnMinus).setOnClickListener {
            if (playerCount > 2) {
                playerCount--
                tvCount.text = playerCount.toString()
                updatePlayerFields(playerCount, etPlayer3, etPlayer4, labelP3, labelP4)
            }
        }

        findViewById<android.view.View>(R.id.btnPlus).setOnClickListener {
            if (playerCount < 4) {
                playerCount++
                tvCount.text = playerCount.toString()
                updatePlayerFields(playerCount, etPlayer3, etPlayer4, labelP3, labelP4)
            }
        }

        findViewById<android.view.View>(R.id.btnStartGame).setOnClickListener {
            val name1 = etPlayer1.text.toString().trim().ifEmpty { "Player 1" }
            val name2 = etPlayer2.text.toString().trim().ifEmpty { "Player 2" }
            val name3 = etPlayer3.text.toString().trim().ifEmpty { "Player 3" }
            val name4 = etPlayer4.text.toString().trim().ifEmpty { "Player 4" }

            startActivity(Intent(this, GameActivity::class.java).apply {
                putExtra("PLAYER_COUNT", playerCount)
                putExtra("PLAYER1_NAME", name1)
                putExtra("PLAYER2_NAME", name2)
                putExtra("PLAYER3_NAME", name3)
                putExtra("PLAYER4_NAME", name4)
            })
        }
    }

    private fun updatePlayerFields(
        count: Int,
        etP3: EditText, etP4: EditText,
        labelP3: TextView, labelP4: TextView
    ) {
        labelP3.visibility = if (count >= 3) View.VISIBLE else View.GONE
        etP3.visibility    = if (count >= 3) View.VISIBLE else View.GONE
        labelP4.visibility = if (count >= 4) View.VISIBLE else View.GONE
        etP4.visibility    = if (count >= 4) View.VISIBLE else View.GONE
    }
}
