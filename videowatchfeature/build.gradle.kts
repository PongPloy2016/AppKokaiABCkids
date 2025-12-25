plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pongploydev.videowatchfeature"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":custom-ui"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Architecture Components
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Utilities
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("org.jsoup:jsoup:1.16.2")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.picasso:picasso:2.8")

    // Network Status
    implementation("org.imaginativeworld.oopsnointernet:oopsnointernet:2.0.0")

    // YouTube Player
    //implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.2")
   // implementation("com.pierfrancescosoffritti.androidyoutubeplayer:custom-ui:12.1.2")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("androidx.activity:activity:1.12.0")

    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:extension-ima:2.19.1")
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}