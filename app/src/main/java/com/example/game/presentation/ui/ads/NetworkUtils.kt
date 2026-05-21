package com.example.game.presentation.ui.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    fun isAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(network) ?: return false

                // Connected transport is enough for app-side network availability.
                // Some Wi-Fi networks take time to report VALIDATED, which was hiding ads.
                val hasTransport =
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

                val hasInternetCapability =
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                if (hasTransport && hasInternetCapability) {
                    true
                } else {
                    @Suppress("DEPRECATION")
                    cm.activeNetworkInfo?.isConnected == true
                }

            } else {
                @Suppress("DEPRECATION")
                cm.activeNetworkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            false
        }
    }
}
