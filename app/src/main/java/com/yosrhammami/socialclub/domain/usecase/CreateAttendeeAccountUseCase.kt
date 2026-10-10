package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import javax.inject.Inject

class CreateAttendeeAccountUseCase @Inject constructor(
    private val attendeeRepository: AttendeeRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(attendeeId: String, password: String): Result<Unit> {
        /*
        Re-read the attendee instead of trusting what the previous screen passed along:
        - the account is created with the email stored in Firestore, not one typed by the user,
        - and if the attendee was activated in the meantime (another device, stale back stack),
          we stop before creating a second Auth account.
         */
        val attendee = runCatching { attendeeRepository.getAttendee(attendeeId) }
            .getOrElse { return Result.failure(it) }
            ?: return Result.failure(IllegalStateException("We couldn't find this attendee."))

        if (attendee.authUid.isNotBlank()) {
            return Result.failure(IllegalStateException("This attendee already has an account."))
        }

        val authUid = authRepository.createAccount(attendee.email, password)
            .getOrElse { return Result.failure(it) }

        /*
        Creating the Auth account and writing authUid are two separate calls, so they can't be atomic.
        If the write fails we delete the account we just created; otherwise authUid would stay empty
        and every retry would fail with "email already in use". The rollback is best-effort: its own
        failure is ignored, because the error worth showing is the original one.
         */
        return attendeeRepository.linkAuthUid(attendee.id, authUid)
            .onFailure { authRepository.deleteCurrentAccount() }
    }
}
