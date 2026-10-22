package com.yosrhammami.socialclub.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onLogoutClick() {
        // A second tap while logging out is ignored (AC1): one logout, one navigation.
        if (_uiState.value is SettingsUiState.LoggingOut) return
        _uiState.value = SettingsUiState.LoggingOut
        viewModelScope.launch {
            // Never back to Idle: logout can't fail (spec), and the auth observer is about to
            // navigate away, destroying this ViewModel. Idle again would re-enable the button for a moment.
            logoutUseCase()
        }
    }
}
