package com.yosrhammami.socialclub.ui.peopleList

import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.domain.usecase.GetPeopleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class PeopleListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /*
    The use case is mocked rather than built on FakePersonRepository: the real one switches to
    Dispatchers.IO, a real background thread that MainDispatcherRule doesn't replace, so uiState
    was still Loading when the test read it.
     */
    private val getPeopleUseCase = mockk<GetPeopleUseCase>()

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
        coEvery { getPeopleUseCase(20) } returns fakePeople

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
        coEvery { getPeopleUseCase(20) } throws Exception("Network error")

        // Act
        val viewModel = PeopleListViewModel(getPeopleUseCase)

        // Assert
        val state = viewModel.uiState.value
        Assert.assertTrue(state is PeopleListUiState.Error)
        Assert.assertEquals("Network error", (state as PeopleListUiState.Error).message)
    }
}