package com.example.game.presentation.ui

import android.app.Application
import com.example.game.presentation.ui.ads.AdManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // AdMob init
        AdManager.init(this)

        val remoteConfig = Firebase.remoteConfig

        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
        )

        // ✅ Pehle defaults set karo, PHIR fetch karo
        remoteConfig.setDefaultsAsync(
            mapOf("show_ads" to true)
        ).addOnCompleteListener {
            // Defaults ready hone ke baad fetch + activate
            remoteConfig.fetchAndActivate()
        }
    }
}