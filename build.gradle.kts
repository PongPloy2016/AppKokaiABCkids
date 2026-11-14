// Top-level build.gradle.kts
buildscript {
    extra.apply {
        set("kotlin_version", "2.1.21")
        set("agp_version", "8.10.1")
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1")
        classpath("com.google.gms:google-services:4.4.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.6")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.21")
    }
}

// ✅ ต้องมี allprojects เมื่อไม่มี dependencyResolutionManagement
// allprojects {
//     repositories {
//         google()
//         mavenCentral()
//         maven { url = uri("https://jitpack.io") }
//     }
// }

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}