package com.yosrhammami.socialclub.ui.attendee

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
class AttendeeViewModel @Inject constructor(
    private val getAttendeeUseCase: GetAttendeeUseCase,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendeeUiState>(AttendeeUiState.Idle)
    val uiState: StateFlow<AttendeeUiState> = _uiState.asStateFlow()

    fun loadAttendee(email: String) {
        viewModelScope.launch {
            _uiState.value = AttendeeUiState.Loading
            try {
                val attendee = getAttendeeUseCase.invoke(email)
                _uiState.value = if (attendee != null) {
                    AttendeeUiState.Success(attendee)
                } else {
                    AttendeeUiState.Error("Attendee not found")
                }
            } catch (e: Exception) {
                logger.e("Error loading attendee", e)
                _uiState.value = AttendeeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}