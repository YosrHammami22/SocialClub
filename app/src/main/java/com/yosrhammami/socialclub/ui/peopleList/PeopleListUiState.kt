package com.yosrhammami.socialclub.ui.peopleList

import com.yosrhammami.socialclub.domain.model.Person
/*
Why sealed interface: it means these are the only possible states — Loading, Success, or Error, nothing else. When you later write when (uiState) { } in your Compose screen, Kotlin forces you to handle every case, or it won't compile. This is a very deliberate design choice that prevents bugs like "forgot to handle the error case."
 */
sealed interface PeopleListUiState {
    object Loading : PeopleListUiState
    data class Success(val people: List<Person>) : PeopleListUiState
    data class Error(val message: String) : PeopleListUiState
}