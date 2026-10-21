package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val attendeeRepository: AttendeeRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        // Re-read the attendee instead of trusting the previous screen, for the same reason as
        // CreateAttendeeAccountUseCase: authUid is the value we verify against, so it must be fresh.
        val attendee = runSuspendCatching { attendeeRepository.findAttendeeByEmail(email) }
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalStateException("We couldn't find this attendee."))

        val uid = authRepository.signIn(attendee.email, password)
            .getOrElse { return Result.failure(it) }

        /*
        A correct password only proves the user owns *an* Auth account with this email. authUid is
        what actually ties an account to this attendee (and what Home checks for a live session),
        so a mismatch is refused. The session Firebase just opened is dropped too: leaving it would
        keep the device signed in as an account the app has just rejected.
         */
        if (uid != attendee.authUid) {
            authRepository.signOut()
            return Result.failure(AuthException.AccountMismatch())
        }
        return Result.success(Unit)
    }
}
