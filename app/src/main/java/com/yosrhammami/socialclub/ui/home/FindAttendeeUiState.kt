package com.yosrhammami.socialclub.ui.home

import com.yosrhammami.socialclub.domain.model.Attendee

sealed interface FindAttendeeUiState {
    object Idle : FindAttendeeUiState
    object Loading : FindAttendeeUiState
    data class Success(val attendee: Attendee) : FindAttendeeUiState
    data class Error(val message: String) : FindAttendeeUiState
}