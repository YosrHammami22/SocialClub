package com.yosrhammami.socialclub.ui.guestDetail

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState

sealed interface AttendeeDetailUiState {
    object Idle : AttendeeDetailUiState
    object Loading : AttendeeDetailUiState
    data class Success(
        val guestAttendee: Attendee,
        val contactButtonState: ContactRequestButtonState
    ) : AttendeeDetailUiState
    object AttendeeNotFound : AttendeeDetailUiState
    data class Error(val message: String) : AttendeeDetailUiState
}