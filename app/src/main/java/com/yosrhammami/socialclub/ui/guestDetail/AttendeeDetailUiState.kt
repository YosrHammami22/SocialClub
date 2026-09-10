package com.yosrhammami.socialclub.ui.guestDetail

import com.yosrhammami.socialclub.domain.model.Attendee

sealed interface AttendeeDetailUiState {
    object Idle : AttendeeDetailUiState
    object Loading : AttendeeDetailUiState
    data class Success(
        val guestAttendee: Attendee
    ) : AttendeeDetailUiState
    object AttendeeNotFound : AttendeeDetailUiState
    data class Error(val message: String) : AttendeeDetailUiState
}