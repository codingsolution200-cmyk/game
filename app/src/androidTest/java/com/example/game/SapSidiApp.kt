package com.example.game

import android.app.Application
import com.google.android.gms.ads.MobileAds

class SapSidiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // AdMob Initialize
        MobileAds.initialize(this) {}
    }
}