package com.yosrhammami.socialclub.domain.model

sealed interface AttendeeWithRegistrationsResult {
    data class Found(
        val attendee: Attendee,
        val registrations: List<RegistrationWithEvent>
    ) : AttendeeWithRegistrationsResult
    object AttendeeNotFound : AttendeeWithRegistrationsResult
}

data class RegistrationWithEvent(
    val registration: Registration,
    val event: Event?     // nullable in case the linked event was somehow deleted
)