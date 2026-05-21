package com.example.game.presentation.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils
import com.example.game.presentation.ui.ads.AdManager
import com.example.game.presentation.ui.ads.BannerAdController
import com.example.game.presentation.ui.ads.NetworkUtils
import com.example.game.presentation.ui.computer.ComputerGameActivity
import com.example.game.presentation.ui.legal.LegalLinks
import com.example.game.presentation.ui.online.OnlineMenuActivity
import com.example.game.presentation.ui.rules.RulesActivity
import com.example.game.presentation.ui.settings.SettingsActivity
import com.example.game.presentation.ui.setup.SetupActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

class HomeActivity : AppCompatActivity() {

    private var navClickCount = 0
    private lateinit var adView: AdView
    private lateinit var bannerAdController: BannerAdController

    companion object {
        private const val SHOW_AD_EVERY_N_CLICKS = 3
        private const val PREFS_NAME = "sapsidi_prefs"
        private const val KEY_BATTERY_ASKED = "battery_asked"
        private const val KEY_AUTOSTART_ASKED = "autostart_asked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        FullscreenUtils.apply(window)

        // ── Fullscreen ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        adView = findViewById(R.id.adView)
        bannerAdController = BannerAdController(this, adView, ::isAdsEnabled)
        AdManager.preload(this)
        bannerAdController.start()
        setupButtonListeners()
        runEntryAnimations()
        startParticleAnimations()

        // ── Permission Flow: Battery first → then AutoStart ──
    }

    // ══════════════════════════════════════════════════════
    // PERMISSION FLOW — Sequential: Battery → AutoStart
    // ══════════════════════════════════════════════════════

    private fun prepareOnlinePermissions(onReady: () -> Unit) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val batteryAsked = prefs.getBoolean(KEY_BATTERY_ASKED, false)
        val autostartAsked = prefs.getBoolean(KEY_AUTOSTART_ASKED, false)

        val needsBattery = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !isBatteryOptimizationIgnored()

        when {
            // Step 1: Battery permission pehla (if needed & not asked before)
            needsBattery && !batteryAsked -> askBatteryOptimizationForOnline {
                prefs.edit().putBoolean(KEY_BATTERY_ASKED, true).apply()
                // Battery done → check autostart
                if (!autostartAsked && isXiaomiDevice()) {
                    askAutoStart(prefs, onReady)
                } else {
                    onReady()
                }
            }

            // Step 2: Sirf AutoStart (battery already handled ya not needed)
            !autostartAsked && isXiaomiDevice() -> askAutoStart(prefs, onReady)

            // Already done — nothing
            else -> onReady()
        }
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun isXiaomiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return listOf("xiaomi", "redmi", "poco").any { manufacturer.contains(it) }
    }

    private fun isRestrictedOEM(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return listOf("xiaomi", "redmi", "poco", "realme", "oppo", "vivo", "samsung", "huawei")
            .any { manufacturer.contains(it) }
    }

    // ── Step 1: Battery Optimization ──
    private fun askBatteryOptimizationForOnline(onDone: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || isBatteryOptimizationIgnored()) {
            onDone()
            return
        }

        AlertDialog.Builder(this, R.style.ExitDialogTheme)
            .setTitle("Keep Online Game Stable")
            .setMessage(
                "If your phone closes online rooms in the background, set SapSidi battery usage to Unrestricted in App settings. You can skip this and continue playing."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
                onDone()
            }
            .setNegativeButton("Skip") { dialog, _ ->
                dialog.dismiss()
                onDone()
            }
            .setOnCancelListener { onDone() }
            .setCancelable(true)
            .show()
    }

    private fun askAutoStart(prefs: android.content.SharedPreferences, onDone: () -> Unit) {
        prefs.edit().putBoolean(KEY_AUTOSTART_ASKED, true).apply()

        AlertDialog.Builder(this, R.style.ExitDialogTheme)
            .setTitle("Enable AutoStart")
            .setMessage(
                "Xiaomi, Redmi, Mi and POCO phones may stop online rooms in the background.\n\n" +
                        "Enable AutoStart for SapSidi only if online games disconnect often. You can skip and continue playing."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                openAutoStartSettings()
                onDone()
            }
            .setNegativeButton("Skip") { dialog, _ ->
                dialog.dismiss()
                onDone()
            }
            .setOnCancelListener { onDone() }
            .setCancelable(true)
            .show()
    }

    private fun openAppSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        } catch (e: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (_: Exception) {
            }
        }
    }

    private fun askBatteryOptimization(onDone: () -> Unit = {}) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { onDone(); return }
        if (isBatteryOptimizationIgnored()) { onDone(); return }

        AlertDialog.Builder(this, R.style.ExitDialogTheme)
            .setTitle("⚡ Battery Permission Required")
            .setMessage(
                "To keep online games connected without interruption:\n\n" +
                        "Please allow SapSidi to run without battery restrictions."
            )
            .setPositiveButton("Allow ✅") { _, _ ->
                try {
                    startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .apply { data = Uri.parse("package:$packageName") }
                    )
                } catch (e: Exception) {
                    // Fallback: open general battery settings
                    try {
                        startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                    } catch (ex: Exception) { /* ignore */ }
                }
                onDone()
            }
            .setNegativeButton("Skip") { d, _ -> d.dismiss(); onDone() }
            .setCancelable(false)
            .show()
    }

    // ── Step 2: AutoStart (Xiaomi/MIUI specific — direct Settings open) ──
    private fun askAutoStart(prefs: android.content.SharedPreferences) {
        prefs.edit().putBoolean(KEY_AUTOSTART_ASKED, true).apply()

        AlertDialog.Builder(this, R.style.ExitDialogTheme)
            .setTitle("📱 AutoStart Permission")
            .setMessage(
                "Xiaomi/MIUI devices block background apps by default.\n\n" +
                        "Please enable AutoStart for SapSidi to:\n" +
                        "• Stay connected in online games\n" +
                        "• Prevent unexpected disconnections\n\n" +
                        "Tap 'Open Settings' — find SapSidi and turn it ON."
            )
            .setPositiveButton("Open Settings ⚙️") { _, _ ->
                openAutoStartSettings()
            }
            .setNegativeButton("Skip") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .show()
    }

    // ── Direct AutoStart Settings openers (Xiaomi/MIUI specific) ──
    private fun openAutoStartSettings() {
        val intents = listOf(
            // MIUI AutoStart direct page
            Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            },
            // MIUI Security app fallback
            Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.securitycenter.MainActivity"
                )
            },
            // Realme / ColorOS
            Intent().apply {
                component = ComponentName(
                    "com.coloros.oppoguardelf",
                    "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
                )
            },
            // OPPO
            Intent().apply {
                component = ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            },
            // Vivo
            Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            },
            // Generic app settings fallback
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        )

        // Try each intent until one works
        for (intent in intents) {
            try {
                startActivity(intent)
                return
            } catch (e: Exception) {
                // Try next
            }
        }
    }

    // ══════════════════════════════════════════════════════
    // REMOTE CONFIG — Ads Control
    // ══════════════════════════════════════════════════════

    private fun isAdsEnabled(): Boolean {
        return try {
            val config = Firebase.remoteConfig
            val value = config.getValue("show_ads")
            if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) true
            else value.asBoolean()
        } catch (e: Exception) {
            true
        }
    }

    // ══════════════════════════════════════════════════════
    // ENTRY ANIMATIONS
    // ══════════════════════════════════════════════════════

    private fun runEntryAnimations() {
        val tvTitle = findViewById<View>(R.id.tvTitle)
        val tvSubtitle = findViewById<View>(R.id.tvSubtitle)
        val btnLocal = findViewById<View>(R.id.btnLocalGame)
        val btnComputer = findViewById<View>(R.id.btnVsComputer)
        val btnOnline = findViewById<View>(R.id.btnOnlineGame)
        val btnRules = findViewById<View>(R.id.btnRules)
        val btnSettings = findViewById<View>(R.id.btnSettings)
        val tvPrivacy = findViewById<View>(R.id.tvPrivacyPolicy)
        val tvVersion = findViewById<View>(R.id.tvVersion)

        tvTitle.translationY = -80f
        tvTitle.alpha = 0f
        val titleAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvTitle, "translationY", -80f, 0f).apply {
                    duration = 700; interpolator = OvershootInterpolator(1.2f)
                },
                ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f).apply { duration = 500 }
            )
            startDelay = 100
        }

        tvSubtitle.translationY = 20f
        tvSubtitle.alpha = 0f
        val subtitleAnim = slideUpFade(tvSubtitle, startDelay = 350)

        btnSettings.translationX = 60f
        btnSettings.alpha = 0f
        val settingsAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(btnSettings, "translationX", 60f, 0f).apply {
                    duration = 500; interpolator = OvershootInterpolator(1.5f)
                },
                ObjectAnimator.ofFloat(btnSettings, "alpha", 0f, 1f).apply { duration = 400 }
            )
            startDelay = 200
        }

        titleAnim.start()
        subtitleAnim.start()
        settingsAnim.start()
        slideUpFade(btnLocal, startDelay = 500).start()
        slideUpFade(btnComputer, startDelay = 650).start()
        slideUpFade(btnOnline, startDelay = 700).start()
        slideUpFade(btnRules, startDelay = 820).start()
        slideUpFade(tvPrivacy, startDelay = 910).start()
        slideUpFade(tvVersion, startDelay = 980).start()
    }

    private fun slideUpFade(view: View, startDelay: Long): AnimatorSet {
        view.translationY = 60f
        view.alpha = 0f
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "translationY", 60f, 0f).apply {
                    duration = 550; interpolator = OvershootInterpolator(0.9f)
                },
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply { duration = 400 }
            )
            this.startDelay = startDelay
        }
    }

    // ══════════════════════════════════════════════════════
    // BUTTON LISTENERS + TOUCH ANIMATIONS
    // ══════════════════════════════════════════════════════

    private fun setupButtonListeners() {
        val buttons = listOf(
            findViewById<View>(R.id.btnLocalGame),
            findViewById<View>(R.id.btnVsComputer),
            findViewById<View>(R.id.btnOnlineGame),
            findViewById<View>(R.id.btnRules)
        )
        buttons.forEach { btn ->
            btn.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.93f).scaleY(0.93f)
                        .setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).start()
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f)
                        .setDuration(300).setInterpolator(OvershootInterpolator(2f)).start()
                }
                false
            }
        }

        val btnSettings = findViewById<View>(R.id.btnSettings)
        btnSettings.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(100).start()
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f)
                    .setDuration(350).setInterpolator(OvershootInterpolator(2.5f)).start()
            }
            false
        }

        findViewById<View>(R.id.btnLocalGame).setOnClickListener {
            openWithAd(Intent(this, SetupActivity::class.java))
        }
        findViewById<View>(R.id.btnVsComputer).setOnClickListener {
            openWithAd(Intent(this, ComputerGameActivity::class.java))
        }
        findViewById<View>(R.id.btnOnlineGame).setOnClickListener {
            if (!NetworkUtils.isAvailable(this)) {
                showNoNetworkDialog()
                return@setOnClickListener
            }
            prepareOnlinePermissions {
                openWithAd(Intent(this, OnlineMenuActivity::class.java))
            }
        }
        findViewById<View>(R.id.btnRules).setOnClickListener {
            openWithAd(Intent(this, RulesActivity::class.java))
        }
        findViewById<View>(R.id.tvPrivacyPolicy).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LegalLinks.PRIVACY_POLICY_URL)))
        }
        btnSettings.setOnClickListener {
            openWithAd(Intent(this, SettingsActivity::class.java))
        }
    }

    // ══════════════════════════════════════════════════════
    // FLOATING PARTICLE ANIMATIONS
    // ══════════════════════════════════════════════════════

    private fun startParticleAnimations() {
        findViewById<View>(R.id.particle1)?.let { floatParticle(it, 3200L, 0L, -18f) }
        findViewById<View>(R.id.particle2)?.let { floatParticle(it, 2800L, 600L, -14f) }
        findViewById<View>(R.id.particle3)?.let { floatParticle(it, 3600L, 300L, -20f) }
    }

    private fun floatParticle(view: View, duration: Long, delay: Long, dy: Float) {
        view.animate()
            .translationYBy(dy).alpha(view.alpha * 1.4f)
            .setDuration(duration).setStartDelay(delay)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .translationYBy(-dy).alpha(view.alpha / 1.4f)
                    .setDuration(duration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction { floatParticle(view, duration, 0L, dy) }
                    .start()
            }.start()
    }

    // ══════════════════════════════════════════════════════
    // BANNER AD — NetworkUtils + Remote Config controlled
    // ══════════════════════════════════════════════════════

    private fun showNoNetworkDialog() {
        AlertDialog.Builder(this, R.style.ExitDialogTheme)
            .setTitle("No Internet")
            .setMessage("Online game needs an active internet connection. Please turn on WiFi or mobile data and try again.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun loadBannerAd() {
        if (!isAdsEnabled()) { adView.visibility = View.GONE; return }
        if (!NetworkUtils.isAvailable(this)) { adView.visibility = View.GONE; return }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() { adView.visibility = View.VISIBLE }
            override fun onAdFailedToLoad(error: LoadAdError) { adView.visibility = View.GONE }
        }
        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            adView.visibility = View.GONE
        }
    }

    // ══════════════════════════════════════════════════════
    // INTERSTITIAL — Remote Config controlled
    // ══════════════════════════════════════════════════════

    private fun openWithAd(intent: Intent) {
        navClickCount++
        val shouldShowAd = isAdsEnabled() &&
                navClickCount % SHOW_AD_EVERY_N_CLICKS == 0 &&
                AdManager.isReady()

        if (shouldShowAd) {
            AdManager.showIfReady(this) {
                AdManager.preload(this)
                startActivity(intent)
            }
        } else {
            startActivity(intent)
        }
    }

    // ══════════════════════════════════════════════════════
    // LIFECYCLE
    // ══════════════════════════════════════════════════════

    override fun onResume() {
        super.onResume()
        bannerAdController.resume()
        AdManager.preload(this)
    }

    override fun onPause() {
        bannerAdController.pause()
        super.onPause()
    }

    override fun onDestroy() {
        bannerAdController.destroy()
        super.onDestroy()
    }
}
