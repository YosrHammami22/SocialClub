package com.yosrhammami.socialclub.ui.attendee

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.usecase.GetAttendeesForEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventAttendeesViewModel @Inject constructor(
    private val getAttendeesForEventUseCase: GetAttendeesForEventUseCase,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    private val currentAttendeeId: String = checkNotNull(savedStateHandle["currentAttendeeId"])

    private val _uiState = MutableStateFlow<EventAttendeesUiState>(EventAttendeesUiState.Loading)
    val uiState: StateFlow<EventAttendeesUiState> = _uiState.asStateFlow()

    init {
        loadAttendees()
    }
    private fun loadAttendees() {
        viewModelScope.launch {
            try {
                val attendees = getAttendeesForEventUseCase(eventId,currentAttendeeId)
                _uiState.value = EventAttendeesUiState.Success(attendees)
            } catch (e: Exception) {
                logger.e("Error loading event attendees", e)
                _uiState.value = EventAttendeesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}