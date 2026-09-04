package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.AttendeeWithRegistrationsResult
import com.yosrhammami.socialclub.domain.model.RegistrationWithEvent
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.EventRepository
import com.yosrhammami.socialclub.domain.repository.RegistrationRepository
import javax.inject.Inject

class GetAttendeeWithRegistrationsUseCase @Inject constructor(
    private val attendeeRepository: AttendeeRepository,
    private val registrationRepository: RegistrationRepository,
    private val eventRepository: EventRepository
) {

    suspend operator fun invoke(email: String): AttendeeWithRegistrationsResult {
        val attendee = attendeeRepository.findAttendeeByEmail(email)
            ?: return AttendeeWithRegistrationsResult.AttendeeNotFound

        val registrations = registrationRepository.getRegistrationsForPerson(attendee.id)

        val registrationsWithEvents = registrations.map { registration ->
            val event = eventRepository.getEvent(registration.eventId)
            RegistrationWithEvent(registration = registration, event = event)
        }
        return AttendeeWithRegistrationsResult.Found(
            attendee = attendee,
            registrations = registrationsWithEvents
        )
    }
}