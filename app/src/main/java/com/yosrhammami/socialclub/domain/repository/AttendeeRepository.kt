package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.Attendee

interface AttendeeRepository {
    suspend fun getAttendee(personId: String): Attendee?
}