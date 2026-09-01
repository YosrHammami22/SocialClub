package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import javax.inject.Inject

class GetAttendeeUseCase @Inject constructor(
    private val repository: AttendeeRepository
) {
    suspend operator fun invoke(email: String): Attendee? {
        return repository.findAttendeeByEmail(email)
    }
}