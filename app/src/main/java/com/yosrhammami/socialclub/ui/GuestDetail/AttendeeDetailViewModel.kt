package com.yosrhammami.socialclub.ui.GuestDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendeeDetailViewModel @Inject constructor(
    private val getAttendeeUseCase: GetAttendeeUseCase,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel(){
    private val guestAttendeeId: String = checkNotNull(savedStateHandle["attendeeId"])

    private val _uiState = MutableStateFlow<AttendeeDetailUiState>(AttendeeDetailUiState.Loading)
    val uiState: StateFlow<AttendeeDetailUiState> = _uiState.asStateFlow()

    init {
        getAttendeeUseCase(guestAttendeeId)
    }
    private fun getAttendeeUseCase(attendeeId:String){
        viewModelScope.launch {

            try {
                val attendee = getAttendeeUseCase.invoke(attendeeId)
                _uiState.value = if (attendee != null) {
                    AttendeeDetailUiState.Success(attendee)
                } else {
                    AttendeeDetailUiState.Error("Attendee not found")
                }
            } catch (e: Exception) {
                logger.e("Error loading attendee detail", e)
                _uiState.value = AttendeeDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

}