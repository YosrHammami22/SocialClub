package com.yosrhammami.socialclub.ui.attendee

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.Registration

sealed interface AttendeeUiState {
    object Idle : AttendeeUiState
    object Loading : AttendeeUiState
    data class Success(
        val attendee: Attendee,
        val registrations: List<Registration>
    ) : AttendeeUiState
    object AttendeeNotFound : AttendeeUiState
    data class Error(val message: String) : AttendeeUiState
}