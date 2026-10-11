package com.yosrhammami.socialclub.ui.signIn

sealed interface SignInUiState {
    object Idle : SignInUiState
    object Loading : SignInUiState
    object Success : SignInUiState
    data class Error(val message: String) : SignInUiState
}
