package com.yosrhammami.socialclub.ui.guestDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.data.session.SessionManager
import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeUseCase
import com.yosrhammami.socialclub.domain.usecase.GetContactRequestStateUseCase
import com.yosrhammami.socialclub.domain.usecase.SendContactRequestUseCase
import com.yosrhammami.socialclub.domain.usecase.UpdateContactRequestStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendeeDetailViewModel @Inject constructor(
    private val getAttendeeUseCase: GetAttendeeUseCase,
    private val getContactRequestStateUseCase: GetContactRequestStateUseCase,
    private val sendContactRequestUseCase: SendContactRequestUseCase,
    private val updateContactRequestUseCase: UpdateContactRequestStatusUseCase,
    private val sessionManager: SessionManager,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val guestAttendeeId: String = checkNotNull(savedStateHandle["attendeeId"])

    private val _uiState = MutableStateFlow<AttendeeDetailUiState>(AttendeeDetailUiState.Loading)
    val uiState: StateFlow<AttendeeDetailUiState> = _uiState.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvent = _errorEvent.asSharedFlow()

    init {
        loadAttendeeDetail()
    }

    private fun loadAttendeeDetail() {
        viewModelScope.launch {
            try {
                val attendee = getAttendeeUseCase.invoke(guestAttendeeId)
                if (attendee != null) {
                    _uiState.value = AttendeeDetailUiState.Success(
                        attendee,
                        ContactRequestButtonState.Unknown
                    )
                    observeContactRequestState()
                }
                else {
                    _uiState.value = AttendeeDetailUiState.Error("Attendee not found")
                }
            }
            catch (e: Exception) {
                logger.e(
                    "Error loading attendee detail",
                    e
                )
                _uiState.value = AttendeeDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeContactRequestState() {
        val currentAttendeeId = sessionManager.currentAttendee.value?.id ?: return
        viewModelScope.launch {
            getContactRequestStateUseCase(
                fromAttendeeId = currentAttendeeId,
                toAttendeeId = guestAttendeeId
            ).collect {buttonState ->
                    val current = _uiState.value
                    if (current is AttendeeDetailUiState.Success) {
                        _uiState.value = current.copy(contactButtonState = buttonState)
                    }
                }
        }
    }

    fun onSendRequestClick() {
        viewModelScope.launch {
            val currentAttendeeId = sessionManager.currentAttendee.value?.id ?: return@launch
            sendContactRequestUseCase(
                currentAttendeeId,
                guestAttendeeId
            ).onFailure {
                logger.e("Error sending contact request", it)
                _errorEvent.tryEmit("No internet connection") /* tryEmit() is the non-suspend version of emit(). It tries to send the value immediately and returns true/false whether it worked — it doesn't pause the coroutine.*/
                }

        }

    }

    fun updateContactRequest(status: ContactRequestStatus){
        viewModelScope.launch {
            val currentAttendeeId = sessionManager.currentAttendee.value?.id ?: return@launch
            updateContactRequestUseCase(
                currentAttendeeId,
                guestAttendeeId,
                status=status
            ).onFailure {
                logger.e("Error update contact request", it)
                _errorEvent.tryEmit("No internet connection") /* tryEmit() is the non-suspend version of emit(). It tries to send the value immediately and returns true/false whether it worked — it doesn't pause the coroutine.*/
            }

        }
    }

}