package com.yosrhammami.socialclub.ui.settings

import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.usecase.LogoutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * T5 · Presentation · Covers AC1 (double tap)
 *
 * The "gate" trick: the fake use case waits on a CompletableDeferred,
 * so logout stays "in progress" until the test opens the gate.
 * With MainDispatcherRule's UnconfinedTestDispatcher, onLogoutClick()
 * runs immediately up to gate.await(), so uiState.value is already
 * LoggingOut on the next line: no Turbine needed (project convention).
 */
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logoutUseCase: LogoutUseCase = mockk()
    private val gate = CompletableDeferred<Unit>()

    private fun createViewModel(): SettingsViewModel {
        coEvery { logoutUseCase() } coAnswers { gate.await() }
        return SettingsViewModel(logoutUseCase)
    }

    @Test
    fun `initial state is Idle`() {
        val viewModel = createViewModel()

        assertEquals(SettingsUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `tapping logout sets state to LoggingOut`() {
        val viewModel = createViewModel()

        viewModel.onLogoutClick()

        assertEquals(SettingsUiState.LoggingOut, viewModel.uiState.value)
        gate.complete(Unit)
    }

    @Test
    fun `second tap while logging out does not call the use case again`() {
        val viewModel = createViewModel()

        viewModel.onLogoutClick()
        viewModel.onLogoutClick()   // user taps twice quickly

        coVerify(exactly = 1) { logoutUseCase() }
        gate.complete(Unit)
    }
}
