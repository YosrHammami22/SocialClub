package com.yosrhammami.socialclub.core.util

import timber.log.Timber
import javax.inject.Inject

/**
 * Production [Logger], backed by Timber.
 *
 * No `BuildConfig.DEBUG` check here: whether anything is written is decided by which
 * Trees are planted in `SocialClubApp` (a DebugTree in debug, none in release), so this
 * class stays free of android.* imports.
 *
 * Explicit tag: without it, DebugTree derives the tag from the calling class, which would
 * always be TimberLogger itself. "So_Social" keeps existing logcat filters working.
 */
class TimberLogger @Inject constructor() : Logger {

    override fun i(message: String) {
        Timber.tag(TAG).i(message)
    }

    override fun d(message: String) {
        Timber.tag(TAG).d(message)
    }

    override fun e(message: String, throwable: Throwable?) {
        Timber.tag(TAG).e(throwable, message)
    }

    private companion object {
        const val TAG = "So_Social"
    }
}