// Top-level build.gradle.kts
buildscript {
    extra.apply {
        set("kotlin_version", "1.9.23")
        set("agp_version", "8.2.2")
        set("compose_compiler_version", "1.5.11")
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2") // ✅ Reverted to stable version
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
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