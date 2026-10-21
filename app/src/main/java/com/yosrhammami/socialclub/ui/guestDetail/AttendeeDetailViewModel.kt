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
import kotlinx.coroutines.CancellationException
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
                    // Known up front from the attendee itself, no Firestore round trip needed —
                    // and observing would be impossible anyway (no uid to build the request ID from).
                    val initialButtonState =
                        if (attendee.authUid.isBlank()) ContactRequestButtonState.GuestHasNoAccount
                        else ContactRequestButtonState.Unknown
                    _uiState.value = AttendeeDetailUiState.Success(
                        attendee,
                        initialButtonState
                    )
                    observeContactRequestState()
                }
                else {
                    _uiState.value = AttendeeDetailUiState.Error("Attendee not found")
                }
            }
            catch (e: CancellationException) {
                throw e // leaving the screen cancels the scope: not an error, and must not become Error state
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

    /*
    Contact requests are keyed by Firebase Auth UIDs, not attendee document IDs: the UID is the
    identity Firebase Auth actually proves. The guest's UID only exists on the loaded Attendee (the
    route only carries the doc ID), so we read it from the Success state.
    A blank UID means that attendee has no account yet — generateId() would throw, so we bail out.
     */
    private fun authUidPairOrNull(): Pair<String, String>? {
        val currentAuthUid = sessionManager.currentAttendee.value?.authUid
        val otherAuthUid = (_uiState.value as? AttendeeDetailUiState.Success)?.guestAttendee?.authUid
        if (currentAuthUid.isNullOrBlank() || otherAuthUid.isNullOrBlank()) return null
        return currentAuthUid to otherAuthUid
    }

    private fun observeContactRequestState() {
        val (currentAuthUid, otherAuthUid) = authUidPairOrNull() ?: return
        viewModelScope.launch {
            // An uncaught exception here would crash the app via viewModelScope: the Firestore
            // listener can fail, and toDomain() throws on a corrupt status.
            try {
                getContactRequestStateUseCase(
                    currentAuthUid = currentAuthUid,
                    otherAuthUid = otherAuthUid
                ).collect {buttonState ->
                        val current = _uiState.value
                        if (current is AttendeeDetailUiState.Success) {
                            _uiState.value = current.copy(contactButtonState = buttonState)
                        }
                    }
            } catch (e: CancellationException) {
                throw e // leaving the screen cancels the scope: that's not an error to report
            } catch (e: Exception) {
                logger.e("Error observing contact request", e)
                _errorEvent.tryEmit("Error loading contact request ${e.message}")
            }
        }
    }

    fun onSendRequestClick() {
        viewModelScope.launch {
            val (currentAuthUid, otherAuthUid) = authUidPairOrNull() ?: return@launch
            // generateId() validates uids with require(), which throws before the use case
            // can return a Result — fold that into the same failure path as a network error.
            val result = try {
                sendContactRequestUseCase(currentAuthUid, otherAuthUid)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
            result.onFailure {
                logger.e("Error sending contact request", it)
                _errorEvent.tryEmit("Error sending contact request ${it.message}") /* tryEmit() is the non-suspend version of emit(). It tries to send the value immediately and returns true/false whether it worked — it doesn't pause the coroutine.*/
                }

        }

    }

    fun updateContactRequest(status: ContactRequestStatus){
        viewModelScope.launch {
            val (currentAuthUid, otherAuthUid) = authUidPairOrNull() ?: return@launch
            updateContactRequestUseCase(
                currentAuthUid,
                otherAuthUid,
                status=status
            ).onFailure {
                logger.e("Error update contact request", it)
                _errorEvent.tryEmit("Error update contact request ${it.message}") /* tryEmit() is the non-suspend version of emit(). It tries to send the value immediately and returns true/false whether it worked — it doesn't pause the coroutine.*/
            }

        }
    }

}