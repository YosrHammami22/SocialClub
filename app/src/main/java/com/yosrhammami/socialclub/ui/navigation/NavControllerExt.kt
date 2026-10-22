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
