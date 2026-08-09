// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // ... other plugins like android-application, kotlin-android
    alias(libs.plugins.hilt.android) apply false
    //  the serialization plugin
    alias(libs.plugins.kotlin.serialization)  version "1.9.0" apply false

}