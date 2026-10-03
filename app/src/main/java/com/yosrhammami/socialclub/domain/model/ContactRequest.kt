package com.yosrhammami.socialclub.domain.model

data class ContactRequest(
    val id: String,
    val fromAttendeeId: String,     // the currentAttendee sending it
    val toAttendeeId: String,       // the guestAttendee receiving it
   // val message: String,
    val status: ContactRequestStatus,
    val createdAt: Long
) {

    companion object {

        fun generateId(
            senderId: String,
            receiverId: String
        ): String {
            return if (senderId < receiverId) {
                "${senderId}_$receiverId"
            } else {
                "${receiverId}_$senderId"
            }
        }
    }
}

enum class ContactRequestStatus { PENDING, ACCEPTED, DECLINED }