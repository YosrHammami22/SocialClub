package com.yosrhammami.socialclub.ui.signIn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    val email: String = checkNotNull(savedStateHandle["email"])

    // Plain MutableStateFlow, not SavedStateHandle: a password must never be written
    // to the saved-instance-state bundle (same rule as CreatePasswordViewModel).
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null
    }

    fun onSubmitClick() {
        val currentPassword = _password.value

        // No length rule here: that belongs to account creation. An existing password is whatever
        // it is, so the only local check is "not empty" (which also saves a pointless network call).
        if (currentPassword.isEmpty()) {
            _passwordError.value = "Please enter your password"
            return
        }
        // Ignore taps while a sign-in is running; Success is final, the screen is about to navigate away.
        val state = _uiState.value
        if (state is SignInUiState.Loading || state is SignInUiState.Success) return

        viewModelScope.launch {
            _uiState.value = SignInUiState.Loading
            signInUseCase(email, currentPassword)
                .onSuccess { _uiState.value = SignInUiState.Success }
                .onFailure { e ->
                    logger.e("Error signing in", e)
                    // A wrong password is feedback about the field itself, so it's a field error
                    // (cleared as soon as the user edits it), like "attendee not found" on Home.
                    // Anything else (network, throttling) is a screen-level error.
                    if (e is AuthException.InvalidCredentials) {
                        _passwordError.value = e.message
                        _uiState.value = SignInUiState.Idle
                    } else {
                        _uiState.value = SignInUiState.Error(e.message ?: "Unknown error")
                    }
                }
        }
    }
}
