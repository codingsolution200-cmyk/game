package com.example.game.presentation.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdManager — Singleton
 * - Ad ko background mein preload karta hai
 * - Navigation PEHLE hoti hai, ad BAAD mein aati hai
 * - WiFi + Mobile network dono pe fast load
 * - Game ke beech mein kabhi interrupt nahi karta
 */
object AdManager {

    private const val AD_UNIT_ID = "ca-app-pub-8366632129348322/6580959052"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isInitialized = false

    // ── Initialize (Application ya MainActivity mein ek baar call karo) ──
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        MobileAds.initialize(context) {
            preload(context)
        }
    }

    // ── Preload — background mein silently load karo ──────────────────
    fun preload(context: Context) {
        if (isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Show ad — agar ready hai to dikhao, nahi to seedha [afterAd] call karo
     * Navigation logic [afterAd] mein likho
     * Ad SHOW hone ke BAAD next screen pe jao — game beech mein interrupt nahi hogi
     */
    fun showIfReady(activity: Activity, afterAd: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preload(activity)   // Next ad preload shuru karo
                    afterAd()
                }
                override fun onAdFailedToShowFullScreenContent(e: AdError) {
                    interstitialAd = null
                    preload(activity)
                    afterAd()
                }
                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                }
            }
            ad.show(activity)
        } else {
            preload(activity)   // Load nahi thi, ab preload shuru karo
            afterAd()           // Seedha navigate karo
        }
    }

    fun isReady(): Boolean = interstitialAd != null
}
