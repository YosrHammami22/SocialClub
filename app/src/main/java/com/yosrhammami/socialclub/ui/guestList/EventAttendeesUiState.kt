package com.yosrhammami.socialclub.ui.guestList

import com.yosrhammami.socialclub.domain.model.Attendee

interface EventAttendeesUiState {
    object Loading : EventAttendeesUiState
    data class Success(  val eventName: String,val guestAttendees: List<Attendee>) : EventAttendeesUiState
    data class Error(val message: String) : EventAttendeesUiState
}