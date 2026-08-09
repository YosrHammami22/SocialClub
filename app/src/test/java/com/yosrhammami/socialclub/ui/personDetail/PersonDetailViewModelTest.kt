package com.yosrhammami.socialclub.ui.personDetail

import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.FakePersonRepository
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.usecase.GetPersonDetailUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakePersonRepository()
    private val getPersonDetailUseCase = GetPersonDetailUseCase(fakeRepository)
    private val fakeLogger = FakeLogger()   // <- no Android framework involved at all


    @Test
    fun `when person exists, uiState becomes Success`() = runTest {
        // Arrange
        val jane = Person(id = "1", fullName = "Jane Doe", email = "jane@test.com", city = "Paris", country = "France", age = 29, photoUrl = "")
        fakeRepository.peopleToReturn = listOf(jane)

        // Act
        val viewModel = PersonDetailViewModel(getPersonDetailUseCase,fakeLogger)
        viewModel.loadPerson("1")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is PersonDetailUiState.Success)
        assertEquals(jane, (state as PersonDetailUiState.Success).person)
    }

    @Test
    fun `when person does not exist, uiState becomes Error`() = runTest {
        // Arrange
        fakeRepository.peopleToReturn = emptyList()

        // Act
        val viewModel = PersonDetailViewModel(getPersonDetailUseCase,fakeLogger)
        viewModel.loadPerson("nonexistent-id")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is PersonDetailUiState.Error)
        assertEquals("Person not found", (state as PersonDetailUiState.Error).message)
    }
    @Test
    fun `when repository throws, uiState becomes Error`() = runTest {
        // Arrange
        fakeRepository.shouldThrowError = true

        // Act
        val viewModel = PersonDetailViewModel(getPersonDetailUseCase,fakeLogger)
        viewModel.loadPerson("1")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is PersonDetailUiState.Error)
    }
}