package com.yosrhammami.socialclub.ui.settings

// No "LoggedOut" state on purpose: the app-level auth observer navigates away, not this screen (plan.md).
sealed interface SettingsUiState {
    object Idle : SettingsUiState
    object LoggingOut : SettingsUiState
}
