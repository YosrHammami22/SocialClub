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

        /**
         * Builds the Firestore document ID for the contact request between two attendees,
         * formatted as `"<smaller>_<larger>"` (lexicographic String comparison).
         *
         * The result is the same regardless of argument order: `generateId(a, b) == generateId(b, a)`,
         * even though the parameters are named sender and receiver. This is deliberate — a pair of
         * attendees has at most one request document, whoever sent it, so either side can look it up.
         *
         * @throws IllegalArgumentException if a uid is blank, contains `_`, or both uids are equal.
         */
        fun generateId(
            senderUid: String,
            receiverUid: String
        ): String {
            require(senderUid.isNotBlank() && receiverUid.isNotBlank()) { "uid must not be blank" }
            // "_" is the separator: allowing it would let two different pairs map to the same ID.
            require('_' !in senderUid && '_' !in receiverUid) { "uid must not contain '_'" }
            require(senderUid != receiverUid) { "Cannot send a contact request to yourself" }

            return if (senderUid < receiverUid) {
                "${senderUid}_$receiverUid"
            } else {
                "${receiverUid}_$senderUid"
            }
        }
    }
}

enum class ContactRequestStatus { PENDING, ACCEPTED, DECLINED }