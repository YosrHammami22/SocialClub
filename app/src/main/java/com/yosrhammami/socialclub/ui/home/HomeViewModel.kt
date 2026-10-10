package com.yosrhammami.socialclub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.model.FirstConnectionResult
import com.yosrhammami.socialclub.domain.usecase.CheckFirstConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkFirstConnectionUseCase: CheckFirstConnectionUseCase,
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

    // A plain Kotlin Regex instead of android.util.Patterns: Patterns.EMAIL_ADDRESS is null in JVM
    // unit tests (same family of problem as the old static logger), which made this ViewModel untestable.
    private fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun onSubmitClick() {
        val currentEmail = _email.value

        if (!isValidEmail(currentEmail)) {
            _emailError.value = "Please enter a valid email address"
            return
        }
        // Ignore taps while a lookup is already running, so a double tap can't navigate twice.
        if (_uiState.value is FindAttendeeUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = FindAttendeeUiState.Loading
            try {
                when (val result = checkFirstConnectionUseCase(currentEmail)) {
                    is FirstConnectionResult.NeedsPassword ->
                        _uiState.value = FindAttendeeUiState.NeedsPassword(result.attendee)

                    is FirstConnectionResult.AlreadyActivated ->
                        _uiState.value = FindAttendeeUiState.Success(result.attendee)

                    // "Not found" is feedback about the email field itself, so it's shown as a field
                    // error (cleared as soon as the user edits the email) rather than a screen error.
                    is FirstConnectionResult.AttendeeNotFound -> {
                        _emailError.value = "We couldn't find an attendee with this email."
                        _uiState.value = FindAttendeeUiState.Idle
                    }
                }
            }
            catch (e: Exception) {
                logger.e(
                    "Error checking first connection",
                    e
                )
                _uiState.value = FindAttendeeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /*
    NeedsPassword/Success are navigation triggers held in state. The screen calls this right after
    navigating; without it, coming Back to Home would re-collect the same state and navigate forward again.
     */
    fun onNavigationHandled() {
        _uiState.value = FindAttendeeUiState.Idle
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")
    }
}
