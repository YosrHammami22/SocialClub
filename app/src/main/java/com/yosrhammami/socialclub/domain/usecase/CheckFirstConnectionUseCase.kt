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
        is plain, JVM-testable logic. Compared against the email stored in Firestore (not the typed
        one) and case-insensitively, since Firebase Auth lowercases emails.
         */
        val hasSession = authRepository.currentUserEmail().equals(attendee.email, ignoreCase = true)
        return if (hasSession) {
            FirstConnectionResult.AlreadyActivated(attendee)
        } else {
            FirstConnectionResult.NeedsSignIn(attendee)
        }
    }
}
