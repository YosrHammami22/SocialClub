package com.yosrhammami.socialclub.domain.model

enum class ContactRequestButtonState {
    NoRequest,
    PendingSender,
    PendingReceiver,
    Declined,
    Accepted,
    GuestHasNoAccount,  // guestAttendee has no authUid yet: nobody to send a request to
    Unknown
}