package com.yosrhammami.socialclub.data.session

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/*
Tells the auth observer *why* the session ended: a deliberate logout shows no message, anything else
is a "session expired" (spec AC6). consume() resets the flag, otherwise a later real expiry would also
look deliberate and show nothing. AtomicBoolean so read-and-reset is one step, whatever thread calls it.
@Singleton: the repository writes it and the app-level observer reads it, so both must share one instance.
 */
@Singleton
class DeliberateLogoutFlag @Inject constructor() {
    private val deliberate = AtomicBoolean(false)

    fun markDeliberate() = deliberate.set(true)

    // Returns true at most once per markDeliberate().
    fun consume(): Boolean = deliberate.getAndSet(false)
}
