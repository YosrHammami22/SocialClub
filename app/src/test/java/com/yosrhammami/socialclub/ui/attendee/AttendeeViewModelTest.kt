package com.yosrhammami.socialclub.ui.attendee

import androidx.lifecycle.SavedStateHandle
import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.AttendeeWithRegistrationsResult
import com.yosrhammami.socialclub.domain.model.PaymentStatus
import com.yosrhammami.socialclub.domain.model.Registration
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeWithRegistrationsUseCase
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
    private val fakeUseCase = mockk<GetAttendeeWithRegistrationsUseCase>()
    private val fakeLogger = FakeLogger()
    private fun createViewModel(email: String = "jane@test.com"): AttendeeViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("email" to email))
        return AttendeeViewModel(fakeUseCase, fakeLogger, savedStateHandle)
    }

    @Test
    fun `when attendee is found, uiState becomes Success with attendee and registrations`() = runTest {
        // Arrange
        val jane = Attendee(
            id = "1",
            fullName = "Jane Doe",
            age=33,
            email = "jane@test.com",
            prompt = "Android dev",
            tags = listOf("android")
        )
        val registrations = listOf(
            Registration(
                id = "r1",
                personId = "1",
                eventId = "e1",
                paymentStatus = PaymentStatus.PAID,
                qrCode = "A-123",
                registeredAt = 0L
            )
        )
        coEvery { fakeUseCase("jane@test.com") } returns
                AttendeeWithRegistrationsResult.Found(jane, registrations)

        // Act
        val viewModel = createViewModel(email = "jane@test.com")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is AttendeeUiState.Success)
        assertEquals(jane, (state as AttendeeUiState.Success).attendee)
        assertEquals(registrations, state.registrations)
    }
    @Test
    fun `when attendee is not found, uiState becomes AttendeeNotFound`() = runTest {
        // Arrange
        coEvery { fakeUseCase("unknown@test.com") } returns
                AttendeeWithRegistrationsResult.AttendeeNotFound

        // Act
        val viewModel = createViewModel(email = "unknown@test.com")

        // Assert
        assertTrue(viewModel.uiState.value is AttendeeUiState.AttendeeNotFound)
    }
    @Test
    fun `when use case throws, uiState becomes Error`() = runTest {
        // Arrange
        coEvery { fakeUseCase("jane@test.com") } throws Exception("Network error")

        // Act
        val viewModel = createViewModel(email = "jane@test.com")

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is AttendeeUiState.Error)
        assertEquals("Network error", (state as AttendeeUiState.Error).message)
    }
}