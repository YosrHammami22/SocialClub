package com.yosrhammami.socialclub.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * T7 · Navigation · Covers AC6
 *
 * Navigation itself is hard to unit-test, so the DECISION
 * ("where do we go, and do we show a message?") is extracted
 * into a pure function with no Android in it.
 *
 * Note the third test: while writing these tests, a new edge case appeared.
 * At app start nobody is logged in, and that must NOT show "session expired".
 * That's why the decision needs to know whether the user WAS logged in before.
 */
class AuthNavigationDeciderTest {

    @Test
    fun `deliberate logout goes to sign in with no message`() {
        val result = AuthNavigationDecider.decide(
            wasLoggedIn = true,
            isLoggedIn = false,
            wasDeliberateLogout = true
        )

        assertEquals(AuthNavigation.ToSignIn(showSessionExpired = false), result)
    }

    @Test
    fun `session lost without logout goes to sign in with session expired message`() {
        val result = AuthNavigationDecider.decide(
            wasLoggedIn = true,
            isLoggedIn = false,
            wasDeliberateLogout = false
        )

        assertEquals(AuthNavigation.ToSignIn(showSessionExpired = true), result)
    }

    @Test
    fun `nobody was logged in at app start, so nothing happens`() {
        val result = AuthNavigationDecider.decide(
            wasLoggedIn = false,
            isLoggedIn = false,
            wasDeliberateLogout = false
        )

        assertEquals(AuthNavigation.None, result)
    }

    @Test
    fun `user still logged in, so nothing happens`() {
        val result = AuthNavigationDecider.decide(
            wasLoggedIn = true,
            isLoggedIn = true,
            wasDeliberateLogout = false
        )

        assertEquals(AuthNavigation.None, result)
    }
}
