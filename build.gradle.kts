// Top-level build file
plugins {
    alias(libs.plugins.android.application)        apply false
    alias(libs.plugins.kotlin.android)             apply false
    alias(libs.plugins.hilt)                       apply false
    alias(libs.plugins.google.gms.google.services) apply false   // ← libs thi levo, hardcode nahi
    id("com.google.firebase.crashlytics")          version "3.0.3" apply false

}