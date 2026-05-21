package com.example.game.presentation.ui.ads

import android.app.Activity
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class BannerAdController(
    private val activity: Activity,
    private val adView: AdView,
    private val isAdsEnabled: () -> Boolean = { true }
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val connectivityManager =
        activity.getSystemService(ConnectivityManager::class.java)

    private var isRegistered = false
    private var isLoading = false
    private var hasLoadedAd = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            refreshOnMain()
        }

        override fun onLost(network: Network) {
            hideOnMain()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            refreshOnMain()
        }
    }

    fun start() {
        registerNetworkCallback()
        adView.visibility = View.GONE
        adView.post { refresh() }
    }

    fun resume() {
        try { adView.resume() } catch (_: Exception) {}
        refresh()
    }

    fun pause() {
        try { adView.pause() } catch (_: Exception) {}
    }

    fun destroy() {
        unregisterNetworkCallback()
        try { adView.destroy() } catch (_: Exception) {}
    }

    fun refresh() {
        if (!isAdsEnabled() || !NetworkUtils.isAvailable(activity)) {
            isLoading = false
            hasLoadedAd = false
            adView.visibility = View.GONE
            return
        }

        if (isLoading || hasLoadedAd) return

        isLoading = true
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isLoading = false
                hasLoadedAd = true
                adView.visibility =
                    if (isAdsEnabled() && NetworkUtils.isAvailable(activity)) View.VISIBLE else View.GONE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                isLoading = false
                hasLoadedAd = false
                adView.visibility = View.GONE
            }
        }

        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (_: Exception) {
            isLoading = false
            hasLoadedAd = false
            adView.visibility = View.GONE
        }
    }

    private fun registerNetworkCallback() {
        if (isRegistered) return
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
            isRegistered = true
        } catch (_: Exception) {
            isRegistered = false
        }
    }

    private fun unregisterNetworkCallback() {
        if (!isRegistered) return
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        } finally {
            isRegistered = false
        }
    }

    private fun refreshOnMain() {
        mainHandler.post { refresh() }
    }

    private fun hideOnMain() {
        mainHandler.post {
            isLoading = false
            hasLoadedAd = false
            adView.visibility = View.GONE
        }
    }
}
