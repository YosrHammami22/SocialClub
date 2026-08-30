package com.yosrhammami.socialclub.ui.attendee

import com.yosrhammami.socialclub.domain.model.Attendee

sealed interface AttendeeUiState {
    object Idle : AttendeeUiState
    object Loading : AttendeeUiState
    data class Success(val attendee: Attendee) : AttendeeUiState
    data class Error(val message: String) : AttendeeUiState
}