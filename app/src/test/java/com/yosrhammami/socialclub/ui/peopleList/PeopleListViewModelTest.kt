package com.yosrhammami.socialclub.ui.peopleList

import com.yosrhammami.socialclub.FakePersonRepository
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.usecase.GetPeopleUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

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
                photoUrl = "",
                gender = Gender.UNKNOWN
            )
        )
        fakeRepository.peopleToReturn = fakePeople

        // Act
        val viewModel = PeopleListViewModel(getPeopleUseCase)

        // Assert
        val state = viewModel.uiState.value
        Assert.assertTrue(state is PeopleListUiState.Success)
        Assert.assertEquals(
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
        Assert.assertTrue(state is PeopleListUiState.Error)
    }
}