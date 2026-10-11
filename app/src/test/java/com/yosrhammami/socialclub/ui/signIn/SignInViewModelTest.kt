package com.yosrhammami.socialclub.ui.signIn

import androidx.lifecycle.SavedStateHandle
import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.usecase.SignInUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUseCase = mockk<SignInUseCase>()

    private fun createViewModel(): SignInViewModel {
        return SignInViewModel(
            signInUseCase = fakeUseCase,
            logger = FakeLogger(),
            savedStateHandle = SavedStateHandle(mapOf("email" to "jane@test.com"))
        )
    }

    @Test
    fun `when password is empty, passwordError is set and no sign-in is attempted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSubmitClick()

        assertEquals("Please enter your password", viewModel.passwordError.value)
        assertEquals(SignInUiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { fakeUseCase(any(), any()) }
    }

    @Test
    fun `when password is correct, uiState becomes Success`() = runTest {
        coEvery { fakeUseCase("jane@test.com", "sunny-Harbor42") } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("sunny-Harbor42")

        viewModel.onSubmitClick()

        assertEquals(SignInUiState.Success, viewModel.uiState.value)
        assertNull(viewModel.passwordError.value)
        coVerify(exactly = 1) { fakeUseCase("jane@test.com", "sunny-Harbor42") }
    }

    @Test
    fun `when password is wrong, passwordError is set and uiState goes back to Idle`() = runTest {
        coEvery { fakeUseCase("jane@test.com", "wrong-password") } returns Result.failure(AuthException.InvalidCredentials())
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong-password")

        viewModel.onSubmitClick()

        assertEquals("Incorrect password. Please try again.", viewModel.passwordError.value)
        assertEquals(SignInUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `when editing the password after a wrong one, the error is cleared`() = runTest {
        coEvery { fakeUseCase("jane@test.com", "wrong-password") } returns Result.failure(AuthException.InvalidCredentials())
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong-password")
        viewModel.onSubmitClick()

        viewModel.onPasswordChanged("sunny-Harbor42")

        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun `when sign-in fails for another reason, uiState becomes Error with the failure message`() = runTest {
        coEvery { fakeUseCase("jane@test.com", "sunny-Harbor42") } returns Result.failure(AuthException.Network())
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("sunny-Harbor42")

        viewModel.onSubmitClick()

        assertEquals(
            SignInUiState.Error("Network error. Please check your connection and try again."),
            viewModel.uiState.value
        )
        assertNull(viewModel.passwordError.value)
    }

    @Test
    fun `after a wrong password, submitting the right one signs in`() = runTest {
        coEvery { fakeUseCase("jane@test.com", "wrong-password") } returns Result.failure(AuthException.InvalidCredentials())
        coEvery { fakeUseCase("jane@test.com", "sunny-Harbor42") } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong-password")
        viewModel.onSubmitClick()

        viewModel.onPasswordChanged("sunny-Harbor42")
        viewModel.onSubmitClick()

        assertEquals(SignInUiState.Success, viewModel.uiState.value)
    }
}
