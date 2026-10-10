package com.yosrhammami.socialclub.ui.home

import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.FirstConnectionResult
import com.yosrhammami.socialclub.domain.usecase.CheckFirstConnectionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUseCase = mockk<CheckFirstConnectionUseCase>()
    private val viewModel = HomeViewModel(checkFirstConnectionUseCase = fakeUseCase, logger = FakeLogger())

    private val jane = Attendee(id = "a1", fullName = "Jane Doe", email = "jane@test.com", age = 33)

    @Test
    fun `when email is malformed, emailError is set and no lookup happens`() = runTest {
        viewModel.onEmailChanged("not-an-email")

        viewModel.onSubmitClick()

        assertEquals("Please enter a valid email address", viewModel.emailError.value)
        assertEquals(FindAttendeeUiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { fakeUseCase(any()) }
    }

    @Test
    fun `when attendee has no account yet, uiState becomes NeedsPassword`() = runTest {
        coEvery { fakeUseCase("jane@test.com") } returns FirstConnectionResult.NeedsPassword(jane)
        viewModel.onEmailChanged("jane@test.com")

        viewModel.onSubmitClick()

        assertEquals(FindAttendeeUiState.NeedsPassword(jane), viewModel.uiState.value)
        assertNull(viewModel.emailError.value)
    }

    @Test
    fun `when attendee already has an account, uiState becomes Success`() = runTest {
        val activated = jane.copy(authUid = "uid-123")
        coEvery { fakeUseCase("jane@test.com") } returns FirstConnectionResult.AlreadyActivated(activated)
        viewModel.onEmailChanged("jane@test.com")

        viewModel.onSubmitClick()

        assertEquals(FindAttendeeUiState.Success(activated), viewModel.uiState.value)
    }

    @Test
    fun `when attendee is not found, emailError explains it and uiState returns to Idle`() = runTest {
        coEvery { fakeUseCase("unknown@test.com") } returns FirstConnectionResult.AttendeeNotFound
        viewModel.onEmailChanged("unknown@test.com")

        viewModel.onSubmitClick()

        assertEquals("We couldn't find an attendee with this email.", viewModel.emailError.value)
        assertEquals(FindAttendeeUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `when use case throws, uiState becomes Error with the exception message`() = runTest {
        coEvery { fakeUseCase("jane@test.com") } throws Exception("Network error")
        viewModel.onEmailChanged("jane@test.com")

        viewModel.onSubmitClick()

        assertEquals(FindAttendeeUiState.Error("Network error"), viewModel.uiState.value)
    }

    @Test
    fun `after navigation is handled, uiState is reset to Idle`() = runTest {
        coEvery { fakeUseCase("jane@test.com") } returns FirstConnectionResult.NeedsPassword(jane)
        viewModel.onEmailChanged("jane@test.com")
        viewModel.onSubmitClick()

        viewModel.onNavigationHandled()

        assertEquals(FindAttendeeUiState.Idle, viewModel.uiState.value)
    }
}
