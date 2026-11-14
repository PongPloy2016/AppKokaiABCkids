import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

android {
    namespace = "com.pongploydev.kokaiabckids"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.pongploydev.kokaiabckids"
        minSdk = 24
        //noinspection EditedTargetSdkVersion,OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("boolean", "IS_CHILD_APP", "true")
        buildConfigField("String", "APP_NAME", "\"ก ไก่ ABC หรรษา - ฝึกเขียนอ่านระบายสี\"")
        buildConfigField("String", "REAL_AD_UNIT_ID", "\"ca-app-pub-3279793802315716/8581063137\"")
        buildConfigField("String", "TEST_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                val storeFilePath = keystoreProperties.getProperty("storeFile")
                if (!storeFilePath.isNullOrBlank()) {
                    storeFile = rootProject.file(storeFilePath)
                }
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            extra["enableCrashlytics"] = false
            buildConfigField("boolean", "SHOW_TEST_ADS", "true")
            buildConfigField("String", "BUILD_MODE", "\"DEBUG\"")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            buildConfigField("boolean", "SHOW_TEST_ADS", "false")
            buildConfigField("String", "BUILD_MODE", "\"RELEASE\"")
            //            firebaseCrashlytics {
            //                mappingFileUploadEnabled = true
            //            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":videowatchfeatureSupabase"))
    implementation(project(":livefeature"))
    implementation(project(":kidswebviewfeature"))
    implementation(project(":newsfeature"))
    implementation(project(":profilefeature"))
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.13.0")

    // MikePenz
    implementation("com.mikepenz:aboutlibraries:10.10.0")
    implementation("com.mikepenz:materialdrawer:9.0.1")
    implementation("com.mikepenz:iconics-core:5.4.0")
    implementation("com.mikepenz:google-material-typeface:4.0.0.2-kotlin@aar")
    implementation("com.mikepenz:fontawesome-typeface:5.9.0.0-kotlin@aar")

    implementation("com.facebook.rebound:rebound:0.3.8")

    // ✅ แก้ไข: เพิ่ม easing library
    implementation("com.daimajia.androidanimations:library:2.4@aar")
    implementation("com.daimajia.easing:library:2.4@aar")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Firebase
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")

    // Ads
    implementation("com.google.android.gms:play-services-ads:24.9.0")

    // App Update & Review
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")

    // Lottie
    implementation("com.airbnb.android:lottie:6.6.6")

    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("me.relex:circleindicator:2.1.6")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.compose.foundation:foundation")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Dialogs
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.github.eltos:simpledialogfragments:v3.10")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")

    // OneSignal
    implementation("com.onesignal:OneSignal:5.1.35")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
