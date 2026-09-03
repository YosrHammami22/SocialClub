package com.yosrhammami.socialclub.domain.model

sealed interface AttendeeWithRegistrationsResult {
    data class Found(
        val attendee: Attendee,
        val registrations: List<Registration>
    ) : AttendeeWithRegistrationsResult
    object AttendeeNotFound : AttendeeWithRegistrationsResult
}