package com.yosrhammami.socialclub.ui.navigation

sealed interface AuthNavigation {
    object None : AuthNavigation
    data class ToSignIn(val showSessionExpired: Boolean) : AuthNavigation
}

/*
The "where do we go after the session changes" decision, pulled out of the auth observer as a pure
function so it can be unit-tested without Android. wasLoggedIn is what keeps app start (nobody logged
in yet) from looking like an expired session (AC7); wasDeliberateLogout silences the message (AC6).
 */
object AuthNavigationDecider {

    fun decide(wasLoggedIn: Boolean, isLoggedIn: Boolean, wasDeliberateLogout: Boolean): AuthNavigation {
        if (!wasLoggedIn || isLoggedIn) return AuthNavigation.None
        return AuthNavigation.ToSignIn(showSessionExpired = !wasDeliberateLogout)
    }
}
