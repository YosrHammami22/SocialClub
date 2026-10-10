package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.FirstConnectionResult
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import javax.inject.Inject

class CheckFirstConnectionUseCase @Inject constructor(
    private val attendeeRepository: AttendeeRepository
) {

    suspend operator fun invoke(email: String): FirstConnectionResult {
        val attendee = attendeeRepository.findAttendeeByEmail(email)
            ?: return FirstConnectionResult.AttendeeNotFound

        // isBlank (not isEmpty): a whitespace-only authUid in Firestore is still "no account".
        return if (attendee.authUid.isBlank()) {
            FirstConnectionResult.NeedsPassword(attendee)
        } else {
            FirstConnectionResult.AlreadyActivated(attendee)
        }
    }
}
