package com.yosrhammami.socialclub.ui.navigation

import androidx.lifecycle.ViewModel
import com.yosrhammami.socialclub.data.session.DeliberateLogoutFlag
import com.yosrhammami.socialclub.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

/*
The one place that reacts to the user becoming logged out, whatever the reason (plan.md: the Settings
screen doesn't navigate). Activity-scoped (obtained outside the NavHost), so it outlives every screen.
It only remembers the previous logged-in value and hands the decision to the pure AuthNavigationDecider.
 */
@HiltViewModel
class AuthObserverViewModel @Inject constructor(
    sessionManager: SessionManager,
    logoutFlag: DeliberateLogoutFlag
) : ViewModel() {

    val navigation: Flow<AuthNavigation> = sessionManager.currentAttendee
        .map { it != null }
        .distinctUntilChanged()
        // (wasLoggedIn, isLoggedIn) pairs. The seed counts as "logged out", so a fresh collector
        // (app start, rotation) never sees a logged-in → logged-out edge that didn't happen (AC7).
        .scan(false to false) { (_, wasLoggedIn), isLoggedIn -> wasLoggedIn to isLoggedIn }
        .drop(1)
        .map { (wasLoggedIn, isLoggedIn) ->
            AuthNavigationDecider.decide(
                wasLoggedIn = wasLoggedIn,
                isLoggedIn = isLoggedIn,
                // Consumed only on an actual logout edge, so the flag can't be eaten by an unrelated emission.
                wasDeliberateLogout = wasLoggedIn && !isLoggedIn && logoutFlag.consume()
            )
        }
        .filterNot { it == AuthNavigation.None }
}
