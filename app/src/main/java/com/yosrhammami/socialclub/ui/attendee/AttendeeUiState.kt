package com.yosrhammami.socialclub.ui.attendee

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.RegistrationWithEvent

sealed interface AttendeeUiState {
    object Idle : AttendeeUiState
    object Loading : AttendeeUiState
    data class Success(
        val attendee: Attendee,
        val registrations: List<RegistrationWithEvent>
    ) : AttendeeUiState
    object AttendeeNotFound : AttendeeUiState
    data class Error(val message: String) : AttendeeUiState
}

sealed interface EventAttendeesUiState {
    object Loading : EventAttendeesUiState
    data class Success(val attendees: List<Attendee>) : EventAttendeesUiState
    data class Error(val message: String) : EventAttendeesUiState
}