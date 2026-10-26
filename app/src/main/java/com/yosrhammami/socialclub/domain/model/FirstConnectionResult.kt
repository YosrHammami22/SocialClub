package com.yosrhammami.socialclub.domain.model

sealed interface FirstConnectionResult {
    object AttendeeNotFound : FirstConnectionResult

    // The attendee exists but has no Firebase Auth account yet (authUid is empty).
    data class NeedsPassword(val attendee: Attendee) : FirstConnectionResult // Create PasseWord 

    // The attendee has an account, but this device has no session for it
    // (reinstall, new device, cleared data, or another account signed in).
    data class NeedsSignIn(val attendee: Attendee) : FirstConnectionResult

    // The attendee has an account and is the one signed in on this device.
    data class AlreadyActivated(val attendee: Attendee) : FirstConnectionResult
}
