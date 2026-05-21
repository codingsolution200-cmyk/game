plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    // ✅ google-services HATAVI — benchmark ne jarur nathi
}

android {
    namespace = "com.example.benchmark"
    compileSdk = 35  // ✅ 36 → 35 (app sathe match karo)

    defaultConfig {
        minSdk = 24
        targetSdk = 35  // ✅ 36 → 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)

}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}