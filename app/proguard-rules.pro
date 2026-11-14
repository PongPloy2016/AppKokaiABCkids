# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/priyank/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

-keep class androidx.compose.foundation.lazy.** { *; }
-keep interface androidx.compose.foundation.lazy.** { *; }

# Keep Attributes for Reflection, Retrofit, and Gson (CRITICAL for s uspend functions)
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Retrofit Keep Rules
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Gson Keep Rules
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Kotlin Coroutines Continuations
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.coroutines.jvm.internal.ContinuationImpl { *; }

# Keep Data Models & API Services (Firebase, Supabase, Profile, Video)
-keep class com.pongploydev.kokaiabckids.model.** { *; }
-keep class com.pongploydev.profilefeature.auth.** { *; }
-keep class com.pongploydev.profilefeature.data.** { *; }
-keep class com.pongploydev.videowatchfeaturesupabase.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}

# Keep Supabase / Ktor / Serialization
-keep class io.github.jan.supabase.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep Glide Modules
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.generated.GeneratedAppGlideModule
-keep class com.pongploydev.kokaiabckids.MyAppGlideModule { *; }

# Keep WebView JavaScript Interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Google Play Billing Library
-keep class com.android.billingclient.api.** { *; }
-keep class com.pongploydev.profilefeature.billing.** { *; }

