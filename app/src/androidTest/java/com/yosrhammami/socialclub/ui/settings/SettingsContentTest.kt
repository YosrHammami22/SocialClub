package com.yosrhammami.socialclub.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

/**
 * T6 · Presentation (UI) · Covers AC1 (double tap)
 *
 * Tests the stateless SettingsContent composable, not the full screen:
 * we pass a state in and check what is drawn. No ViewModel, no Hilt.
 */
class SettingsContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loggingOut_disablesLogoutButton_andShowsLoader() {
        composeRule.setContent {
            SettingsContent(uiState = SettingsUiState.LoggingOut, onLogoutClick = {})
        }

        composeRule.onNodeWithTag(SettingsTestTags.LOGOUT_BUTTON).assertIsNotEnabled()
        composeRule.onNodeWithTag(SettingsTestTags.LOGOUT_LOADER).assertIsDisplayed()
    }

    @Test
    fun idle_enablesLogoutButton_andHidesLoader() {
        composeRule.setContent {
            SettingsContent(uiState = SettingsUiState.Idle, onLogoutClick = {})
        }

        composeRule.onNodeWithTag(SettingsTestTags.LOGOUT_BUTTON).assertIsEnabled()
        composeRule.onNodeWithTag(SettingsTestTags.LOGOUT_LOADER).assertDoesNotExist()
    }
}
