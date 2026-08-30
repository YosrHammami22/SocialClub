plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android) // 1. Apply the Hilt Plugin
    id("kotlin-kapt")               // 2. Apply KAPT (required for Hilt)
    id("kotlinx-serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.yosrhammami.socialclub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yosrhammami.socialclub"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // retrofit dependencies
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    // Hilt dependencies
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    // Hilt with Compose Navigation
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    // serialization
    implementation(libs.kotlinx.serialization.json)
    // handle image
    implementation(libs.coil.compose)
    // Import the BoM using the catalog reference
    implementation(platform(libs.firebase.bom))
    // Import Kotlin  coroutines-Play-Services bridge
    implementation(libs.kotlinx.coroutines.play.services)
    // Add Firestore and Auth (versions are managed by the BoM)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}