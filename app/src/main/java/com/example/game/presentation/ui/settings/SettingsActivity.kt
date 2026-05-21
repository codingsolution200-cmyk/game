package com.example.game.presentation.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.legal.LegalActivity

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "SapSidiPrefs"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_VIBRATION = "vibration_enabled"

        fun isSoundEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_SOUND, true)

        fun isVibrationEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_VIBRATION, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
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

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        AdManager.preload(this)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        val switchSound = findViewById<android.widget.Switch>(R.id.switchSound)
        switchSound.isChecked = prefs.getBoolean(KEY_SOUND, true)
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_SOUND, isChecked).apply()
        }

        val switchVibration = findViewById<android.widget.Switch>(R.id.switchVibration)
        switchVibration.isChecked = prefs.getBoolean(KEY_VIBRATION, true)
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_VIBRATION, isChecked).apply()
        }

        findViewById<android.view.View>(R.id.tvLegal).setOnClickListener {
            startActivity(Intent(this, LegalActivity::class.java))
        }

        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            findViewById<android.widget.TextView>(R.id.tvVersion).text = "Version $versionName"
        } catch (e: Exception) {
            findViewById<android.widget.TextView>(R.id.tvVersion).text = "Version 1.0.0"
        }
    }
}
