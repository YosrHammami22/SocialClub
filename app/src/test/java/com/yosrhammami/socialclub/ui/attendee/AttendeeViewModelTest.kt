package com.yosrhammami.socialclub.ui.attendee

import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AttendeeViewModelTest{
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = mockk<AttendeeRepository>()
    private val getAttendeeUseCase = GetAttendeeUseCase(fakeRepository)
    private val fakeLogger = FakeLogger()

    @Test
    fun `when attendee exists, uiState becomes Success`() = runTest {
// Arrange
        val jane = Attendee(
            id = "1",
            fullName = "Jane Doe",
            email = "jane@test.com",
            prompt = "Android dev, love hiking",
            tags = listOf("android", "hiking")
        )
        coEvery { fakeRepository.getAttendee("1") } returns jane
        // Act
        val viewModel = AttendeeViewModel(getAttendeeUseCase, fakeLogger)
        viewModel.loadAttendee("1")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is AttendeeUiState.Success)
        assertEquals(jane, (state as AttendeeUiState.Success).attendee)

    }

    @Test
    fun `when attendee does not exist, uiState becomes Error`() = runTest {
        // Arrange
        coEvery { fakeRepository.getAttendee("unknown-id") } returns null

        // Act
        val viewModel = AttendeeViewModel(getAttendeeUseCase, fakeLogger)
        viewModel.loadAttendee("unknown-id")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is AttendeeUiState.Error)
        assertEquals("Attendee not found", (state as AttendeeUiState.Error).message)
    }

    @Test
    fun `when repository throws, uiState becomes Error`() = runTest {
        // Arrange
        coEvery { fakeRepository.getAttendee("1") } throws Exception("Network error")

        // Act
        val viewModel = AttendeeViewModel(getAttendeeUseCase, fakeLogger)
        viewModel.loadAttendee("1")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is AttendeeUiState.Error)
        assertEquals("Network error", (state as AttendeeUiState.Error).message)
    }
}