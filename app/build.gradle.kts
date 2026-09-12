plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pongploydev.priyankvex.paintapp"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.pongploydev.priyankvex.paintapp"
        minSdk = 24
        //noinspection EditedTargetSdkVersion,OldTargetApi
        targetSdk = 35
        versionCode = 45
        versionName = "3.0.1"

        buildConfigField("boolean", "IS_CHILD_APP", "true")
        buildConfigField("String", "APP_NAME", "\"Paint App for Kids\"")
        buildConfigField("String", "REAL_AD_UNIT_ID", "\"ca-app-pub-9784556672708249/1773111585\"")
        buildConfigField("String", "TEST_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            buildConfigField("boolean", "SHOW_TEST_ADS", "true")
            buildConfigField("String", "BUILD_MODE", "\"DEBUG\"")
            //            firebaseCrashlytics {
            //                mappingFileUploadEnabled = false
            //            }
        }

        release {
            isDebuggable = false
            isMinifyEnabled = false // ⭐ เปิด minify แต่ต้องมี proguard rules ถูกต้อง
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro" // ⭐ ต้องมีไฟล์นี้
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

    composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }

    configurations.all {
        resolutionStrategy {
            force("androidx.activity:activity:1.8.2")
            force("androidx.activity:activity-ktx:1.8.2")
            force("androidx.activity:activity-compose:1.8.2")
            force("androidx.core:core:1.12.0")
            force("androidx.core:core-ktx:1.12.0")
            force("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
            force("androidx.navigation:navigation-compose:2.7.5")
            force("androidx.recyclerview:recyclerview:1.3.2")
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":videowatchfeatureSupabase"))
    implementation(project(":livefeature"))
    implementation(project(":kidswebviewfeature"))

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")

    // MikePenz
    implementation("com.mikepenz:aboutlibraries:10.8.3")
    implementation("com.mikepenz:materialdrawer:9.0.1")
    implementation("com.mikepenz:iconics-core:5.4.0")
    implementation("com.mikepenz:google-material-typeface:4.0.0.2-kotlin@aar")
    implementation("com.mikepenz:fontawesome-typeface:5.9.0.0-kotlin@aar")

    implementation("com.facebook.rebound:rebound:0.3.8")

    // ✅ แก้ไข: เพิ่ม easing library
    implementation("com.daimajia.androidanimations:library:2.4@aar")
    implementation("com.daimajia.easing:library:2.4@aar") // ⭐ เพิ่มบรรทัดนี้

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")

    // Firebase
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Ads
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    // App Update
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    // Lottie
    implementation("com.airbnb.android:lottie:6.1.0")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.relex:circleindicator:2.1.6")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.compose.foundation:foundation")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Dialogs
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.github.eltos:simpledialogfragments:v3.10")
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")

    // OneSignal
    implementation("com.onesignal:OneSignal:[5.1.0, 5.1.99]")
}
