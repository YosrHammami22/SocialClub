package com.yosrhammami.socialclub.data.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T4 · Data · Covers AC6 + the "session expired" case
 * The flag says "this logout was on purpose" exactly ONCE, then resets.
 * Risk found in Plan: if it never reset, a real session expiry later
 * would show no message.
 */
class DeliberateLogoutFlagTest {

    private val flag = DeliberateLogoutFlag()

    @Test
    fun `consume returns false when nothing was marked`() {
        assertFalse(flag.consume())
    }

    @Test
    fun `consume returns true after markDeliberate`() {
        flag.markDeliberate()

        assertTrue(flag.consume())
    }

    @Test
    fun `consume resets the flag, so the second call returns false`() {
        flag.markDeliberate()
        flag.consume()

        assertFalse(flag.consume())
    }
}
