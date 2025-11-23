plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pongploydev.priyankvex.paintapp"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.pongploydev.priyankvex.paintapp"
        minSdk = 23
        targetSdk = 35
        versionCode = 16
        versionName = "1.2.9"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":videowatchfeature"))

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")

    // MikePenz libraries
    implementation("com.mikepenz:aboutlibraries:10.8.3")
    implementation("com.mikepenz:materialdrawer:9.0.1")
    implementation("com.mikepenz:iconics-core:5.4.0")
    implementation("com.mikepenz:google-material-typeface:4.0.0.2-kotlin@aar")
    implementation("com.mikepenz:fontawesome-typeface:5.9.0.0-kotlin@aar")

    implementation("com.facebook.rebound:rebound:0.3.8")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")

    // Firebase
    implementation("com.google.firebase:firebase-crashlytics:18.6.0")
    implementation("com.google.firebase:firebase-messaging:23.3.1")
    implementation("com.google.firebase:firebase-analytics:21.5.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Ads
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    // App Update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    // Lottie
    implementation("com.airbnb.android:lottie:6.1.0")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.relex:circleindicator:2.1.6")
    implementation("com.daimajia.androidanimations:library:2.4@aar")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("androidx.activity:activity-ktx:1.8.1")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Debug dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ✅ ใช้เวอร์ชันอื่นหรือลบออกชั่วคราว
    // implementation("io.github.eltos:simpledialogfragments:3.10")
    implementation("com.afollestad.material-dialogs:core:3.3.0") // ✅ ใช้แทน

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")

    // Glide compiler
    //kapt("com.github.bumptech.glide:compiler:4.16.0")
}