package com.yosrhammami.socialclub.ui.navigation

import androidx.navigation.NavController

/*
After logout, Home (the email step) becomes the ONLY destination (spec AC1): popping the whole graph means
Back has nowhere to go and closes the app with no special code, and every popped screen's ViewModel is
destroyed, dropping the previous user's data from memory (AC5). Not SignInRoute: it needs an email, and
passing the previous user's would break AC4 (plan.md).
 */
fun NavController.navigateToHomeClearingBackStack() {
    navigate(HomeRoute) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/*
Mirror of the above for the way in (spec AC9): once identified, the attendee screen is the ONLY destination,
so Back closes the app instead of returning to the email or password step. Clearing the whole graph (not just
SignIn/CreatePassword) also destroys Home's ViewModel, so the typed email doesn't linger in memory.
 */
fun NavController.navigateToAttendeeClearingBackStack(email: String) {
    navigate(AttendeeRoute(email = email)) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
