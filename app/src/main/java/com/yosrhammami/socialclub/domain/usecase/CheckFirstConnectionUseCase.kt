package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.FirstConnectionResult
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import javax.inject.Inject

class CheckFirstConnectionUseCase @Inject constructor(
    private val attendeeRepository: AttendeeRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(email: String): FirstConnectionResult {
        val attendee = attendeeRepository.findAttendeeByEmail(email)
            ?: return FirstConnectionResult.AttendeeNotFound

        // isBlank (not isEmpty): a whitespace-only authUid in Firestore is still "no account".
        if (attendee.authUid.isBlank()) {
            return FirstConnectionResult.NeedsPassword(attendee)
        }

        /*
        The session check lives here rather than in the NavHost so the "which screen next" decision
        is plain, JVM-testable logic. Compared by uid, not email: authUid is the actual link between
        the attendee and its Auth account, it is immutable, and it has no casing to normalize.
         */
        val hasSession = authRepository.currentUserUid() == attendee.authUid
        return if (hasSession) {
            FirstConnectionResult.AlreadyActivated(attendee)
        } else {
            FirstConnectionResult.NeedsSignIn(attendee)
        }
    }
}
