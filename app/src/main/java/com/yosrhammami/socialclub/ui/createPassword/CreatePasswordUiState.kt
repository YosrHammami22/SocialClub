package com.yosrhammami.socialclub.ui.createPassword

sealed interface CreatePasswordUiState {
    object Idle : CreatePasswordUiState
    object Loading : CreatePasswordUiState
    object Success : CreatePasswordUiState
    data class Error(val message: String) : CreatePasswordUiState
}
/*
sealed interface has become the more common style in modern Kotlin for this pattern: less class machinery for no benefit,
 */