package com.yosrhammami.socialclub.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.yosrhammami.socialclub.R
import org.junit.Assert.assertEquals
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

    // T12 · AC8 — the Back button is found by its spoken label, the same way TalkBack finds it.
    private val backLabel =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.navigate_back)

    @Test
    fun idle_showsBackButton_thatCallsOnBackClick() {
        var backClicks = 0
        composeRule.setContent {
            SettingsContent(
                uiState = SettingsUiState.Idle,
                onLogoutClick = {},
                onBackClick = { backClicks++ }
            )
        }

        composeRule.onNodeWithContentDescription(backLabel).performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun loggingOut_hidesBackButton() {
        composeRule.setContent {
            SettingsContent(
                uiState = SettingsUiState.LoggingOut,
                onLogoutClick = {},
                onBackClick = {}
            )
        }

        composeRule.onNodeWithContentDescription(backLabel).assertDoesNotExist()
    }
}
