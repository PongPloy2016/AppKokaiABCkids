# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/priyank/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class androidx.compose.foundation.lazy.** { *; }
-keep interface androidx.compose.foundation.lazy.** { *; }

# Keep Data Models (Firebase & Supabase)
-keep class com.pongploydev.priyankvex.paintapp.model.** { *; }
-keep class com.pongploydev.profilefeature.data.** { *; }
-keep class com.pongploydev.videowatchfeaturesupabase.databases.models.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}

# Keep Supabase / Ktor / Serialization
-keep class io.github.jan.supabase.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep Glide Modules
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.generated.GeneratedAppGlideModule
-keep class com.pongploydev.priyankvex.paintapp.MyAppGlideModule { *; }

# Keep WebView JavaScript Interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

