package com.yosrhammami.socialclub.domain.model

enum class ContactRequestButtonState {
    NoRequest,
    PendingSender,
    PendingReceiver,
    Declined,
    Accepted,
    Unknown
}