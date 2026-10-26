package com.yosrhammami.socialclub.core.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching], but rethrows [CancellationException].
 *
 * Plain runCatching turns cancellation into an ordinary Result.failure: the coroutine keeps
 * running after it was cancelled, and callers treat "user left the screen" as a real error
 * (logging it, showing a Toast). Use this for any suspend call wrapped into a Result.
 */
inline fun <T> runSuspendCatching(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
