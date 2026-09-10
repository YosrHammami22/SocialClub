package com.yosrhammami.socialclub.ui.guestList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.data.session.SessionManager
import com.yosrhammami.socialclub.domain.repository.EventRepository
import com.yosrhammami.socialclub.domain.usecase.GetGuestsForEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventAttendeesViewModel @Inject constructor(
    private val getGuestesForEventUseCase: GetGuestsForEventUseCase,
    private val eventRepository: EventRepository,
    private val sessionManager: SessionManager,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    private val _uiState = MutableStateFlow<EventAttendeesUiState>(EventAttendeesUiState.Loading)
    val uiState: StateFlow<EventAttendeesUiState> = _uiState.asStateFlow()


    init {
        loadGuestAttendees()
    }
    private fun loadGuestAttendees() {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEvent(eventId)
                val currentAttendeeId = sessionManager.currentAttendee.value?.id
                val guests = getGuestesForEventUseCase(eventId,excludingAttendeeId=currentAttendeeId)
                _uiState.value = EventAttendeesUiState.Success(event?.name ?: "Event",guests)
            } catch (e: Exception) {
                logger.e("Error loading event attendees", e)
                _uiState.value = EventAttendeesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

}