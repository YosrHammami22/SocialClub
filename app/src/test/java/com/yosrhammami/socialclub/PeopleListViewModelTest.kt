package com.yosrhammami.socialclub

import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.usecase.GetPeopleUseCase
import com.yosrhammami.socialclub.ui.peopleList.PeopleListUiState
import com.yosrhammami.socialclub.ui.peopleList.PeopleListViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class PeopleListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakePersonRepository()
    private val getPeopleUseCase = GetPeopleUseCase(fakeRepository)

    @Test
    fun `when repository returns people, uiState becomes Success`() = runTest {
        // Arrange
        val fakePeople = listOf(
            Person(
                id = "1",
                fullName = "Jane Doe",
                email = "jane@test.com",
                city = "Paris",
                country = "France",
                age = 29,
                photoUrl = ""
            )
        )
        fakeRepository.peopleToReturn = fakePeople

        // Act
        val viewModel = PeopleListViewModel(getPeopleUseCase)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is PeopleListUiState.Success)
        assertEquals(
            fakePeople,
            (state as PeopleListUiState.Success).people
        )
    }

    @Test
    fun `when repository throws, uiState becomes Error`() = runTest {
        // Arrange
        fakeRepository.shouldThrowError = true

        // Act
        val viewModel = PeopleListViewModel(getPeopleUseCase)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is PeopleListUiState.Error)
    }
}