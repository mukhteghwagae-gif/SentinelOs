plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "com.sentinel.os"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sentinel.os"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    packagingOptions {
        resources {
           excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Jetpack Compose & Material 3
    implementation("androidx.compose.ui:ui:1.6.1")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")
    implementation("androidx.compose.foundation:foundation:1.6.1")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Foreground Service
    implementation("androidx.core:core:1.12.0")

    // Bluetooth LE
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // WiFi Direct & Network
    implementation("androidx.core:core:1.12.0")

    // Sensors
    implementation("androidx.core:core-ktx:1.12.0")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // FFT & Signal Processing
    implementation("com.github.psambit9791:jdsp:0.4.0") {
      exclude(group = "org.apache.maven.surefire")
      exclude(group = "org.junit.jupiter")
      exclude(group = "org.junit.platform")
      exclude(group = "junit")
      exclude(group = "org.apache.pdfbox")
      exclude(group = "org.apache.fontbox")
    }
}
    // OpenStreetMap (OSMDroid)
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // Encryption & Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
}
