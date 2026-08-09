package com.yosrhammami.socialclub.ui.personDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.AppLog
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.usecase.GetPersonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val getPersonDetailUseCase: GetPersonDetailUseCase,
    private val logger: Logger
): ViewModel() {

    private val _uiState = MutableStateFlow<PersonDetailUiState>(PersonDetailUiState.Loading)
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

     fun loadPerson(personId: String) {
         logger.i("loadPerson called with id = $personId")
        viewModelScope.launch {
            _uiState.value = PersonDetailUiState.Loading
            try {
                val person = getPersonDetailUseCase(personId)
                if (person != null) {
                    logger.i("Person loaded: ${person?.fullName}")
                    _uiState.value = PersonDetailUiState.Success(person)
                }
                else {
                    logger.i( "Person not found")
                    _uiState.value= PersonDetailUiState.Error("Person not found")
                }
            }
            catch (e: Exception) {
                logger.e("Error loading person", e)
                _uiState.value = PersonDetailUiState.Error(e.message ?: "Unknown error")
            }

        }
    }
}