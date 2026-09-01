package com.yosrhammami.socialclub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAttendeeUseCase: GetAttendeeUseCase,
    private val logger: Logger
): ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _uiState = MutableStateFlow<FindAttendeeUiState>(FindAttendeeUiState.Idle)
    val uiState: StateFlow<FindAttendeeUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        if (_uiState.value is FindAttendeeUiState.Error) {
            _uiState.value = FindAttendeeUiState.Idle
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun onSubmitClick() {
        val currentEmail = _email.value

        if (!isValidEmail(currentEmail)) {
            _uiState.value = FindAttendeeUiState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = FindAttendeeUiState.Loading
            try {
                val attendee = getAttendeeUseCase(currentEmail)
                _uiState.value = if (attendee != null) {
                    FindAttendeeUiState.Success(attendee)
                } else {
                    FindAttendeeUiState.Error("No attendee found with this email")
                }
            } catch (e: Exception) {
                logger.e("Error finding attendee", e)
                _uiState.value = FindAttendeeUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}