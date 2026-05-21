package com.example.game.presentation.ui.legal

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.game.R
import com.example.game.presentation.ui.FullscreenUtils

class LegalActivity : AppCompatActivity() {

    private lateinit var tabPrivacy: TextView
    private lateinit var tabTerms: TextView
    private lateinit var contentContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal)
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

        tabPrivacy = findViewById(R.id.tabPrivacy)
        tabTerms = findViewById(R.id.tabTerms)
        contentContainer = findViewById(R.id.contentContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        tabPrivacy.setOnClickListener { showTab(true) }
        tabTerms.setOnClickListener { showTab(false) }

        showTab(true)
    }

    private fun showTab(isPrivacy: Boolean) {
        if (isPrivacy) {
            tabPrivacy.setBackgroundResource(R.drawable.tab_selected)
            tabPrivacy.setTextColor(0xFFFFFFFF.toInt())
            tabPrivacy.typeface = android.graphics.Typeface.DEFAULT_BOLD
            tabTerms.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabTerms.setTextColor(0xFFAAAAAA.toInt())
            tabTerms.typeface = android.graphics.Typeface.DEFAULT
        } else {
            tabTerms.setBackgroundResource(R.drawable.tab_selected)
            tabTerms.setTextColor(0xFFFFFFFF.toInt())
            tabTerms.typeface = android.graphics.Typeface.DEFAULT_BOLD
            tabPrivacy.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabPrivacy.setTextColor(0xFFAAAAAA.toInt())
            tabPrivacy.typeface = android.graphics.Typeface.DEFAULT
        }

        contentContainer.animate()
            .alpha(0f).setDuration(120)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                contentContainer.removeAllViews()
                if (isPrivacy) buildPrivacyPolicy() else buildTermsOfUse()
                contentContainer.animate()
                    .alpha(1f).setDuration(200)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }.start()
    }

    // ══════════════════════════════════════════════════════
    // PRIVACY POLICY
    // ══════════════════════════════════════════════════════
    private fun buildPrivacyPolicy() {
        val sections = listOf(
            "Privacy Policy" to null,

            "Last Updated" to
                    "May 10, 2026",

            "1. Introduction" to
                    "Welcome to SapSidi - Snakes & Ladders (App, we, our, us). " +
                    "This Privacy Policy explains how we handle information when you use our App. " +
                    "By using SapSidi, you agree to the practices described in this policy.",

            "2. Information We Do NOT Collect" to
                    "SapSidi does not collect, store, or share any personally identifiable information (PII). " +
                    "We do not require account registration. " +
                    "We do not collect your name, email address, phone number, or location data.",

            "3. Information Collected by Third Parties" to
                    "Our App uses the following third-party services that may collect data automatically:\n\n" +
                    "\u2022 Google AdMob - Displays advertisements. AdMob may collect device identifiers, " +
                    "IP address, and ad interaction data as per Google's Privacy Policy.\n\n" +
                    "\u2022 Google Firebase - Used for online multiplayer features and crash reporting. " +
                    "Firebase may collect device information and usage analytics.\n\n" +
                    "\u2022 Firebase Analytics - Collects anonymized usage data to help us improve the App.\n\n" +
                    "\u2022 Firebase Crashlytics - Collects crash logs to help us fix bugs. " +
                    "No personal data is included in crash reports.",

            "4. Online Multiplayer Data" to
                    "When you use the online multiplayer feature, temporary game room data " +
                    "(room code, game positions, turn information) is stored in Firebase Realtime Database. " +
                    "This data is automatically deleted after the game session ends. " +
                    "No username or personal identity is stored.",

            "5. Advertising" to
                    "This App uses Google AdMob to serve advertisements. " +
                    "Ads may be personalized based on your interests as per Google's ad policies.\n\n" +
                    "You can opt out of personalized ads through your device settings:\n" +
                    "Settings \u2192 Google \u2192 Ads \u2192 Opt out of Ads Personalization.",

            "6. Children's Privacy" to
                    "SapSidi is a family-friendly game suitable for all ages. " +
                    "We do not knowingly collect personal information from children under 13. " +
                    "If you believe a child has provided personal information, " +
                    "please contact us and we will delete it immediately.",

            "7. Data Security" to
                    "We implement reasonable security measures to protect any data processed through " +
                    "our third-party services. However, no method of transmission over the internet " +
                    "is 100% secure.",

            "8. Changes to This Policy" to
                    "We may update this Privacy Policy from time to time. " +
                    "Any changes will be reflected with an updated date at the top of this page. " +
                    "Continued use of the App after changes constitutes acceptance.",

            "9. Contact Us" to
                    "If you have any questions about this Privacy Policy, please contact us at:\n" +
                    "\uD83D\uDCE7 codingsolution200@gmail.com"
        )

        buildSections(sections)
    }

    // ══════════════════════════════════════════════════════
    // TERMS OF USE
    // ══════════════════════════════════════════════════════
    private fun buildTermsOfUse() {
        val sections = listOf(
            "Terms of Use" to null,

            "Last Updated" to
                    "May 10, 2026",

            "1. Acceptance of Terms" to
                    "By downloading, installing, or using SapSidi - Snakes & Ladders, " +
                    "you agree to be bound by these Terms of Use. " +
                    "If you do not agree, please do not use this App.",

            "2. License" to
                    "We grant you a limited, non-exclusive, non-transferable, revocable license " +
                    "to use this App for personal, non-commercial entertainment purposes only. " +
                    "You may not copy, modify, distribute, sell, or reverse engineer any part of this App.",

            "3. Acceptable Use" to
                    "You agree NOT to:\n" +
                    "\u2022 Use the App for any unlawful purpose\n" +
                    "\u2022 Attempt to hack, cheat, or exploit the App\n" +
                    "\u2022 Use automated bots or scripts in online multiplayer\n" +
                    "\u2022 Interfere with other players game sessions\n" +
                    "\u2022 Attempt to access Firebase data of other users",

            "4. Online Multiplayer" to
                    "The online multiplayer feature requires an active internet connection. " +
                    "Game sessions are temporary and not stored permanently. " +
                    "We are not responsible for disconnections caused by network issues, " +
                    "device restrictions, or battery optimization settings.",

            "5. Advertisements" to
                    "This App displays advertisements provided by Google AdMob. " +
                    "Ad content is controlled by Google and their advertising partners. " +
                    "We are not responsible for the content of third-party advertisements.",

            "6. Disclaimer of Warranties" to
                    "This App is provided AS IS without any warranties of any kind, " +
                    "express or implied, including but not limited to merchantability, " +
                    "fitness for a particular purpose, or non-infringement. " +
                    "We do not guarantee uninterrupted or error-free operation of the App.",

            "7. Limitation of Liability" to
                    "To the maximum extent permitted by law, we shall not be liable for any " +
                    "indirect, incidental, special, or consequential damages arising from " +
                    "your use of this App, including loss of data or game progress.",

            "8. Intellectual Property" to
                    "All content, graphics, code, and design in this App are owned by " +
                    "the developer (codingsolution200@gmail.com) and protected by applicable " +
                    "intellectual property laws. The classic Snakes & Ladders game concept " +
                    "is in the public domain.",

            "9. Modifications to Terms" to
                    "We reserve the right to modify these Terms at any time. " +
                    "Continued use of the App after any changes constitutes acceptance of the new Terms.",

            "10. Governing Law" to
                    "These Terms shall be governed by and construed in accordance with " +
                    "the laws of India. Any disputes shall be subject to the jurisdiction " +
                    "of courts in Gujarat, India.",

            "11. Contact Us" to
                    "For any questions regarding these Terms of Use, contact us at:\n" +
                    "\uD83D\uDCE7 codingsolution200@gmail.com"
        )

        buildSections(sections)
    }

    // ══════════════════════════════════════════════════════
    // BUILDER
    // ══════════════════════════════════════════════════════
    private fun buildSections(sections: List<Pair<String, String?>>) {
        val email = "codingsolution200@gmail.com"

        sections.forEachIndexed { index, (heading, body) ->
            val tvHeading = TextView(this).apply {
                text = heading
                textSize = if (index == 0) 22f else 16f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(if (index == 0) 0xFFFFFFFF.toInt() else 0xFFF59E0B.toInt())
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = if (index == 0) 8 else dpToPx(20)
                lp.bottomMargin = dpToPx(6)
                layoutParams = lp
            }
            contentContainer.addView(tvHeading)

            body?.let { text ->
                val tvBody = TextView(this).apply {
                    textSize = 14f
                    setTextColor(0xFFDDDDDD.toInt())
                    // FIX 1: lineSpacingExtra replaced with setLineSpacing()
                    setLineSpacing(dpToPx(3).toFloat(), 1.0f)
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.bottomMargin = dpToPx(4)
                    layoutParams = lp

                    if (text.contains(email)) {
                        val spannable = SpannableString(text)
                        val emailStart = text.indexOf(email)
                        val emailEnd = emailStart + email.length

                        spannable.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                // FIX 2: Uri.parse -> "mailto:$email".toUri() (KTX)
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:$email".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "SapSidi App Inquiry")
                                }
                                try {
                                    startActivity(Intent.createChooser(intent, "Send Email"))
                                    // FIX 3: unused "e" -> underscore
                                } catch (_: Exception) { }
                            }
                        }, emailStart, emailEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        spannable.setSpan(
                            ForegroundColorSpan(0xFF4FC3F7.toInt()),
                            emailStart, emailEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        this.text = spannable
                        movementMethod = LinkMovementMethod.getInstance()
                        highlightColor = android.graphics.Color.TRANSPARENT
                    } else {
                        this.text = text
                    }
                }
                contentContainer.addView(tvBody)
            }

            if (index != 0 && index != sections.size - 1 && body != null) {
                val divider = View(this).apply {
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    lp.topMargin = dpToPx(12)
                    lp.bottomMargin = dpToPx(4)
                    layoutParams = lp
                    setBackgroundColor(0x22FFFFFF)
                }
                contentContainer.addView(divider)
            }
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
