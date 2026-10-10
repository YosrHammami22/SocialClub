package com.yosrhammami.socialclub.ui.home

import com.yosrhammami.socialclub.domain.model.Attendee

sealed interface FindAttendeeUiState {
    object Idle : FindAttendeeUiState
    object Loading : FindAttendeeUiState
    // First connection: the attendee exists but has no account yet -> go create a password.
    data class NeedsPassword(val attendee: Attendee) : FindAttendeeUiState
    // The attendee already has an account -> continue to their registrations.
    data class Success(val attendee: Attendee) : FindAttendeeUiState
    data class Error(val message: String) : FindAttendeeUiState
}