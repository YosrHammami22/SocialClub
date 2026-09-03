package com.yosrhammami.socialclub.ui.attendee

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.model.AttendeeWithRegistrationsResult
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeWithRegistrationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendeeViewModel @Inject constructor(
    private val getAttendeeWithRegistrationsUseCase: GetAttendeeWithRegistrationsUseCase,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val email: String = checkNotNull(savedStateHandle["email"])
    private val _uiState = MutableStateFlow<AttendeeUiState>(AttendeeUiState.Idle)
    val uiState: StateFlow<AttendeeUiState> = _uiState.asStateFlow()
   init {
       loadAttendee(email)
   }

     private fun loadAttendee(email: String) {

        viewModelScope.launch {
            _uiState.value = AttendeeUiState.Loading
            try {
                when (val result = getAttendeeWithRegistrationsUseCase(email)) {
                    is AttendeeWithRegistrationsResult.Found -> _uiState.value =
                        AttendeeUiState.Success(
                            attendee = result.attendee,
                            registrations = result.registrations
                        )

                    is AttendeeWithRegistrationsResult.AttendeeNotFound -> _uiState.value =
                        AttendeeUiState.AttendeeNotFound
                }

            }
            catch (e: Exception) {
                logger.e(
                    "Error loading attendee",
                    e
                )
                _uiState.value = AttendeeUiState.Error(e.message ?: "Unknown error")

            }
        }

    }
}