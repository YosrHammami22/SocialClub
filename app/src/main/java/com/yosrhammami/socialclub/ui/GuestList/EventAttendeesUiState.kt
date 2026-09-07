package com.yosrhammami.socialclub.ui.GuestList

import com.yosrhammami.socialclub.domain.model.Attendee

interface EventAttendeesUiState {
    object Loading : EventAttendeesUiState
    data class Success(val guestAttendees: List<Attendee>) : EventAttendeesUiState
    data class Error(val message: String) : EventAttendeesUiState
}