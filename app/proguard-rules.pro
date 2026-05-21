# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------
# SapSidi release protection
# R8 already shrinks, optimizes, and obfuscates app code in release.
# These rules keep only framework/SDK entry points that are loaded by
# Android, Firebase, AdMob, Hilt, reflection, or native serializers.
# ------------------------------------------------------------------

# Keep useful annotation/signature metadata used by Firebase, Gson, Hilt,
# Kotlin, and AndroidX. Do not keep full source file names in release.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Keep Android component subclasses. Android creates these from the manifest.
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep custom Views created from XML layouts.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelable creators when parcelize/manual parcelables are added.
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep enum helpers used by serializers/reflection.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Gson reflective serialization support. Your app currently has no model
# classes using Gson heavily, but this prevents future model breakage.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Firebase / Google Play Services / AdMob / Crashlytics.
# Most rules are supplied by the SDKs themselves; these avoid reflection
# warnings and keep runtime entry points stable.
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.ads.**
-dontwarn org.jspecify.**

# Lottie animation classes referenced from XML/raw JSON.
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Hilt/Dagger generated classes.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class *_HiltModules_* { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-dontwarn dagger.hilt.**
-dontwarn javax.annotation.**

# Kotlin coroutines metadata/warnings.
-keepnames class kotlin.coroutines.Continuation
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.**

# AndroidX / Material optional API warnings.
-dontwarn androidx.**
-dontwarn com.google.android.material.**

# Extra hardening: strip Log calls from release where R8 can prove it is safe.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
