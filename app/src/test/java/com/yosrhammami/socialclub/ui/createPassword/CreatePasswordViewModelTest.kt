package com.yosrhammami.socialclub.ui.createPassword

import androidx.lifecycle.SavedStateHandle
import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.usecase.CreateAttendeeAccountUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CreatePasswordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUseCase = mockk<CreateAttendeeAccountUseCase>()

    private fun createViewModel(): CreatePasswordViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("attendeeId" to "a1", "email" to "jane@test.com"))
        return CreatePasswordViewModel(
            createAttendeeAccountUseCase = fakeUseCase,
            logger = FakeLogger(),
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun `when password is shorter than 8 characters, passwordError is set and no account is created`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("short")
        viewModel.onConfirmPasswordChanged("short")

        viewModel.onSubmitClick()

        assertEquals("Password must be at least 8 characters", viewModel.passwordError.value)
        assertEquals(CreatePasswordUiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { fakeUseCase(any(), any()) }
    }

    @Test
    fun `when confirmation differs, confirmPasswordError is set and no account is created`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("sunny-Harbor42")
        viewModel.onConfirmPasswordChanged("sunny-Harbor43")

        viewModel.onSubmitClick()

        assertEquals("Passwords don't match", viewModel.confirmPasswordError.value)
        assertNull(viewModel.passwordError.value)
        coVerify(exactly = 0) { fakeUseCase(any(), any()) }
    }

    @Test
    fun `when editing a field, its error is cleared`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("short")
        viewModel.onSubmitClick()

        viewModel.onPasswordChanged("sunny-Harbor42")

        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun `when account is created, uiState becomes Success`() = runTest {
        coEvery { fakeUseCase("a1", "sunny-Harbor42") } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("sunny-Harbor42")
        viewModel.onConfirmPasswordChanged("sunny-Harbor42")

        viewModel.onSubmitClick()

        assertEquals(CreatePasswordUiState.Success, viewModel.uiState.value)
        coVerify(exactly = 1) { fakeUseCase("a1", "sunny-Harbor42") }
    }

    @Test
    fun `when account creation fails, uiState becomes Error with the failure message`() = runTest {
        coEvery { fakeUseCase("a1", "sunny-Harbor42") } returns Result.failure(AuthException.Network())
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("sunny-Harbor42")
        viewModel.onConfirmPasswordChanged("sunny-Harbor42")

        viewModel.onSubmitClick()

        assertEquals(
            CreatePasswordUiState.Error("Network error. Please check your connection and try again."),
            viewModel.uiState.value
        )
    }

    @Test
    fun `after a failure, submitting again retries the account creation`() = runTest {
        coEvery { fakeUseCase("a1", "sunny-Harbor42") } returns Result.failure(AuthException.Network()) andThen Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("sunny-Harbor42")
        viewModel.onConfirmPasswordChanged("sunny-Harbor42")
        viewModel.onSubmitClick()

        viewModel.onSubmitClick()

        assertEquals(CreatePasswordUiState.Success, viewModel.uiState.value)
    }
}
