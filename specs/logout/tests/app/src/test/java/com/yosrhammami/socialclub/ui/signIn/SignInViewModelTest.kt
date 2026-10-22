package com.yosrhammami.socialclub.ui.signIn

import com.yosrhammami.socialclub.MainDispatcherRule
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * T10 · Presentation · Covers AC4
 * After logout, the sign-in screen starts with empty fields:
 * the previous user's email is never pre-filled.
 *
 * ADAPT: replace the constructor arguments with your real SignInViewModel's
 * dependencies (relaxed mocks are fine: this test only reads the initial state).
 */
class SignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state has empty email and password`() {
        val viewModel = SignInViewModel(
            signInUseCase = mockk(relaxed = true)
        )

        assertEquals("", viewModel.uiState.value.email)
        assertEquals("", viewModel.uiState.value.password)
    }
}
