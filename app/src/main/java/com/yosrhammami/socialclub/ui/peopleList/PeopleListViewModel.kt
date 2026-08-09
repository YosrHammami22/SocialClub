package com.yosrhammami.socialclub.ui.peopleList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.domain.usecase.GetPeopleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel //tells Hilt this ViewModel can be injected, and Hilt will automatically provide whatever's in the constructor (here, PersonRepository, using the binding you already set up)
class PeopleListViewModel @Inject constructor(
    private val getPeopleUseCase: GetPeopleUseCase   // interface, not impl
): ViewModel() {

    private val _uiState =
        MutableStateFlow<PeopleListUiState>(PeopleListUiState.Loading) //(private, mutable) — only the ViewModel itself can change it
    val uiState: StateFlow<PeopleListUiState> =
        _uiState.asStateFlow() //the Compose screen can read and observe it, but can't modify it directly

    init {
        loadPeople()
    }

    /*
    viewModelScope.launch { } — this is the coroutine scope you learned about — tied to the ViewModel's lifecycle, automatically cancelled if the ViewModel is cleared.
     */
    fun loadPeople() {
        viewModelScope.launch {
            _uiState.value = PeopleListUiState.Loading
            try {
                val people = getPeopleUseCase(20)
                _uiState.value = PeopleListUiState.Success(people)
            }
            catch (e: Exception) {
                _uiState.value = PeopleListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}