package com.yosrhammami.socialclub.ui.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
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
 * T8 · Navigation · Covers AC1 (Back closes the app) and helps AC5
 *
 * After logout, SignIn must be the ONLY screen on the back stack.
 * Then Back has nowhere to go, so the app closes with no special code,
 * and the old screens' ViewModels (holding the previous user's data) are destroyed.
 *
 * Uses a small test graph (fake Home/Settings routes + your real SignInRoute)
 * so the test checks the navigation rule, not your whole app.
 * Needs: androidTestImplementation("androidx.navigation:navigation-testing")
 */
class LogoutNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Serializable private object TestHomeRoute
    @Serializable private object TestSettingsRoute

    private lateinit var navController: TestNavHostController

    @Test
    fun afterLogout_signInIsTheOnlyScreenOnTheBackStack() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = navController, startDestination = TestHomeRoute) {
                composable<TestHomeRoute> { }
                composable<TestSettingsRoute> { }
                composable<SignInRoute> { }
            }
        }

        composeRule.runOnUiThread {
            navController.navigate(TestSettingsRoute)       // Home → Settings
            navController.navigateToSignInClearingBackStack() // logout
        }

        assertTrue(navController.currentDestination?.hasRoute<SignInRoute>() == true)
        assertNull(
            "Nothing should be left under SignIn, so Back closes the app",
            navController.previousBackStackEntry
        )
    }
}
