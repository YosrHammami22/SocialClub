package com.yosrhammami.socialclub.ui.personDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.usecase.GenerateIcebreakerUseCase
import com.yosrhammami.socialclub.domain.usecase.GetPersonDetailUseCase
import com.yosrhammami.socialclub.ui.personDetail.uiState.IcebreakerUiState
import com.yosrhammami.socialclub.ui.personDetail.uiState.PersonDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val getPersonDetailUseCase: GetPersonDetailUseCase,
    private val generateIcebreakerUseCase: GenerateIcebreakerUseCase,
    private val logger: Logger
): ViewModel() {

    private val _uiState = MutableStateFlow<PersonDetailUiState>(PersonDetailUiState.Loading)
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    private val _icebreakerState = MutableStateFlow<IcebreakerUiState>(IcebreakerUiState.Idle)
    val icebreakerState: StateFlow<IcebreakerUiState> = _icebreakerState.asStateFlow()

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
                    logger.i("Person not found")
                    _uiState.value = PersonDetailUiState.Error("Person not found")
                }
            }
            catch (e: Exception) {
                logger.e(
                    "Error loading person",
                    e
                )
                _uiState.value = PersonDetailUiState.Error(e.message ?: "Unknown error")
            }

        }
    }

    fun onGenerateIcebreakerClick() {
        val person = (_uiState.value as? PersonDetailUiState.Success)?.person ?: return
        logger.i("onGenerateIcebreakerClick called for person: ${person.fullName}")
        viewModelScope.launch {
            _icebreakerState.value = IcebreakerUiState.Loading
            try {
                val icebreakerText = generateIcebreakerUseCase(person)
                logger.i("Icebreaker generated: $icebreakerText")
                _icebreakerState.value = IcebreakerUiState.Success(icebreakerText)

            }
            catch (e: Exception) {
                logger.e(
                    "Error generating icebreaker",
                    e
                )
                _icebreakerState.value = IcebreakerUiState.Error(e.message ?: "Failed to generate")
            }

        }

    }
}