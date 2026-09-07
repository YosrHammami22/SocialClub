package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.RegistrationRepository
import javax.inject.Inject

class GetGuestessForEventUseCase @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val attendeeRepository: AttendeeRepository
)  {
    suspend operator fun invoke(eventId: String,excludingAttendeeId: String? = null): List<Attendee> {
        val registrations = registrationRepository.getRegistrationsForEvent(eventId)
        val personIds = registrations.map { it.personId }.distinct()
        val attendees = personIds.mapNotNull { attendeeRepository.getAttendee(it) }
        return attendees.filter { it?.id != excludingAttendeeId }
    }
}