package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.Attendee

interface AttendeeRepository {
    suspend fun findAttendeeByEmail(email: String): Attendee?
}