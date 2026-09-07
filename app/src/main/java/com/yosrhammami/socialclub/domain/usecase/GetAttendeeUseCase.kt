package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import javax.inject.Inject

class GetAttendeeUseCase @Inject constructor(private val attendeeRepository: AttendeeRepository) {

    suspend operator fun invoke(personId: String): Attendee? {
        return attendeeRepository.getAttendee(personId)
    }
}

