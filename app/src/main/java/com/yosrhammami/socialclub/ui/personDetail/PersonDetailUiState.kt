package com.yosrhammami.socialclub.ui.personDetail

import com.yosrhammami.socialclub.domain.model.Person

sealed interface PersonDetailUiState {
    object Loading : PersonDetailUiState
    data class Success(val person: Person) : PersonDetailUiState
    data class Error(val message: String) : PersonDetailUiState
}