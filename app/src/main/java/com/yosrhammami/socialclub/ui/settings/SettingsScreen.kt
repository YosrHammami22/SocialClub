package com.yosrhammami.socialclub.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.components.AppPrimaryButton
import com.yosrhammami.socialclub.ui.components.AppTopBar
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

object SettingsTestTags {
    const val LOGOUT_BUTTON = "settings_logout_button"
    const val LOGOUT_LOADER = "settings_logout_loader"
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onLogoutClick = viewModel::onLogoutClick,
        onBackClick = onBackClick
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {

    val isLoggingOut = uiState is SettingsUiState.LoggingOut
    Column(modifier = modifier.fillMaxSize()) {
        // Back is hidden while logging out: leaving Settings would destroy the ViewModel and
        // cancel viewModelScope mid-logout, possibly between "clear session" and signOut().
        AppTopBar(
            title = stringResource(R.string.settings_title),
            onBackClick = onBackClick,
            enabled = !isLoggingOut
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Disabled while logging out so a second tap can't start a second logout (AC1).
            // Not AppPrimaryButton's isLoading: that spinner sits inside the Button, whose merged
            // semantics hide it, so it couldn't be found (by tests) or announced on its own.
            AppPrimaryButton(
                text = stringResource(R.string.settings_logout_button),
                onClick = onLogoutClick,
                enabled = !isLoggingOut,
                modifier = Modifier.testTag(SettingsTestTags.LOGOUT_BUTTON)
            )

            if (isLoggingOut) {
                val loggingOut = stringResource(R.string.settings_logging_out)
                Spacer(Modifier.height(Spacing.md))
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag(SettingsTestTags.LOGOUT_LOADER)
                        .semantics { contentDescription = loggingOut }
                )
            }
        }
    }


}

@ThemePreviews
@Composable
fun PreviewSettingsContentIdle() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsContent(uiState = SettingsUiState.Idle, onLogoutClick = {}, onBackClick = {})
        }
    }
}

@ThemePreviews
@Composable
fun PreviewSettingsContentLoggingOut() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsContent(uiState = SettingsUiState.LoggingOut, onLogoutClick = {}, onBackClick = {})
        }
    }
}
