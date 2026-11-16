package com.yosrhammami.socialclub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SocialClubApp: Application() {

    override fun onCreate() {
        super.onCreate()
        // The single debug/release switch for logging: in release no Tree is planted,
        // so every Logger call is a no-op. A crash-reporting Tree could be planted here later.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}