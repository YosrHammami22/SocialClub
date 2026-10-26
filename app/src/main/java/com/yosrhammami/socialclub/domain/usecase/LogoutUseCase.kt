package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.repository.AuthRepository
import javax.inject.Inject

/*
Returns Unit, not Result: logout only clears the device (spec AC2), so from the user's point of view
it can't fail, and a Result would carry no information. The ordered steps live in the repository.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}
