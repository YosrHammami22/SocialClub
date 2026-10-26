package com.yosrhammami.socialclub.ui.createPassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.usecase.CreateAttendeeAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePasswordViewModel @Inject constructor(
    private val createAttendeeAccountUseCase: CreateAttendeeAccountUseCase,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val attendeeId: String = checkNotNull(savedStateHandle["attendeeId"])
    val email: String = checkNotNull(savedStateHandle["email"])

    // Deliberately plain MutableStateFlow, not SavedStateHandle: a password must never be written
    // to the saved-instance-state bundle. It survives rotation (ViewModel) but not process death.
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    private val _uiState = MutableStateFlow<CreatePasswordUiState>(CreatePasswordUiState.Idle)
    val uiState: StateFlow<CreatePasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = null
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _confirmPasswordError.value = null
    }

    fun onSubmitClick() {
        val currentPassword = _password.value

        if (currentPassword.length < MIN_PASSWORD_LENGTH) {
            _passwordError.value = "Password must be at least $MIN_PASSWORD_LENGTH characters"
            return
        }
        if (currentPassword != _confirmPassword.value) {
            _confirmPasswordError.value = "Passwords don't match"
            return
        }
        // A second tap while the account is being created would try to create it twice.
        // Success is also final: the screen is about to navigate away.
        val state = _uiState.value
        if (state is CreatePasswordUiState.Loading || state is CreatePasswordUiState.Success) return

        viewModelScope.launch {
            _uiState.value = CreatePasswordUiState.Loading
            createAttendeeAccountUseCase(attendeeId, currentPassword)
                .onSuccess { _uiState.value = CreatePasswordUiState.Success }
                .onFailure { e ->
                    logger.e(
                        "Error creating attendee account",
                        e
                    )
                    _uiState.value = CreatePasswordUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    companion object {
        // Firebase's own floor is 6; 8 is this app's stricter rule.
        const val MIN_PASSWORD_LENGTH = 8
    }
}
