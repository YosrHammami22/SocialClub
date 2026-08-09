package com.yosrhammami.socialclub.core.util

import android.util.Log
import com.yosrhammami.socialclub.BuildConfig
import javax.inject.Inject

class AppLog @Inject constructor(): Logger {

    private val TAG = "So_Social"

    override fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(
                TAG,
                message
            )
        }
    }

    override fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                message
            )
        }
    }

    override fun e(
        message: String,
        throwable: Throwable?
    ) {
        if (BuildConfig.DEBUG) {
            Log.e(
                TAG,
                message,
                throwable
            )
        }
    }
}