package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.AttendeeWithRegistrationsResult
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.RegistrationRepository
import javax.inject.Inject

class GetAttendeeWithRegistrationsUseCase @Inject constructor(
    private val attendeeRepository: AttendeeRepository,
    private val registrationRepository: RegistrationRepository
) {

    suspend operator fun invoke(email: String): AttendeeWithRegistrationsResult {
        val attendee = attendeeRepository.findAttendeeByEmail(email)
            ?: return AttendeeWithRegistrationsResult.AttendeeNotFound

        val registrations = registrationRepository.getRegistrationsForPerson(attendee.id)

        return AttendeeWithRegistrationsResult.Found(
            attendee = attendee,
            registrations = registrations
        )
    }
}