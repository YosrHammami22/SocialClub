package com.yosrhammami.socialclub.domain.model

sealed interface FirstConnectionResult {
    object AttendeeNotFound : FirstConnectionResult

    // The attendee exists but has no Firebase Auth account yet (authUid is empty).
    data class NeedsPassword(val attendee: Attendee) : FirstConnectionResult // Create PasseWord 

    data class AlreadyActivated(val attendee: Attendee) : FirstConnectionResult // SignIn
}
