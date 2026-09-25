# Consumer proguard rules for profilefeature
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep interface * {
    @retrofit2.http.* <methods>;
}

-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.coroutines.jvm.internal.ContinuationImpl { *; }

-keep class com.pongploydev.profilefeature.auth.** { *; }
-keep class com.pongploydev.profilefeature.data.** { *; }
