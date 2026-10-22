package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.repository.AuthRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * T1 · Domain · Covers AC1
 * The use case delegates logout to the repository, exactly once.
 * No Firebase here: the domain only knows the AuthRepository interface.
 */
class LogoutUseCaseTest {

    // relaxUnitFun: logout() returns Unit, so "do nothing" is a safe default.
    private val authRepository: AuthRepository = mockk(relaxUnitFun = true)
    private val logoutUseCase = LogoutUseCase(authRepository)

    @Test
    fun `invoke calls repository logout exactly once`() = runTest {
        logoutUseCase()

        coVerify(exactly = 1) { authRepository.logout() }
    }
}
