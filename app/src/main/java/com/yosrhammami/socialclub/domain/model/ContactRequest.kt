package com.yosrhammami.socialclub.domain.model

data class ContactRequest(
    val id: String,
    val fromAttendeeId: String,     // the currentAttendee sending it
    val toAttendeeId: String,       // the guestAttendee receiving it
    val message: String,
    val status: ContactRequestStatus,
    val createdAt: Long
)

enum class ContactRequestStatus { PENDING, ACCEPTED, DECLINED }