package com.yosrhammami.socialclub.ui.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * T13 · Navigation · Covers AC9 (Back on the attendee screen closes the app)
 *
 * Once identified, the attendee screen must be the ONLY screen on the back stack,
 * so Back can't return to the email or password step. Mirror of T8.
 */
class LoginNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Serializable private object TestHomeRoute
    @Serializable private object TestSignInRoute

    private lateinit var navController: TestNavHostController

    @Test
    fun afterSignIn_attendeeIsTheOnlyScreenOnTheBackStack() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = navController, startDestination = TestHomeRoute) {
                composable<TestHomeRoute> { }
                composable<TestSignInRoute> { }
                composable<AttendeeRoute> { }
            }
        }

        composeRule.runOnUiThread {
            navController.navigate(TestSignInRoute)                                    // Home → SignIn
            navController.navigateToAttendeeClearingBackStack("jane.doe@example.com") // signed in
        }

        assertTrue(navController.currentDestination?.hasRoute<AttendeeRoute>() == true)
        assertNull(
            "Nothing should be left under the attendee screen, so Back closes the app",
            navController.previousBackStackEntry
        )
    }
}
