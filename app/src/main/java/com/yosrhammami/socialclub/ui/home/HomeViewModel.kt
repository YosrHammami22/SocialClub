package com.yosrhammami.socialclub.ui.home

import androidx.lifecycle.ViewModel
import com.yosrhammami.socialclub.core.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logger: Logger
): ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _uiState = MutableStateFlow<FindAttendeeUiState>(FindAttendeeUiState.Idle)
    val uiState: StateFlow<FindAttendeeUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        _emailError.value = null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun onSubmitClick(onValidEmail: (String) -> Unit) {
        val currentEmail = _email.value

        if (!isValidEmail(currentEmail)) {
            _emailError.value = "Please enter a valid email address"
            return
        }
        onValidEmail(currentEmail)
    }
}