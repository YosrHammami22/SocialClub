package com.yosrhammami.socialclub.ui.personDetail.uiState

interface IcebreakerUiState {
    object Idle : IcebreakerUiState          // nothing has happened yet
    object Loading : IcebreakerUiState
    data class Success(val text: String) : IcebreakerUiState
    data class Error(val message: String) : IcebreakerUiState
}