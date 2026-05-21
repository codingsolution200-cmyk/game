package com.example.game

import android.app.Application
import com.example.game.presentation.ui.ads.AdManager
import com.google.firebase.database.FirebaseDatabase

class SapSidiApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Firebase
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (_: Exception) {
        }

        // AdMob — background mein initialize + preload
        AdManager.init(this)
    }
}