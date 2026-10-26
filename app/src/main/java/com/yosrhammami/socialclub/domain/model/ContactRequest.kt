package com.yosrhammami.socialclub.domain.model

data class ContactRequest(
    val id: String,
    val senderAuthUid: String,      // Firebase Auth UID of the attendee who sent it
    val receiverAuthUid: String,    // Firebase Auth UID of the attendee receiving it
   // val message: String,
    val status: ContactRequestStatus,
    val createdAt: Long
) {

    /**
     * The receiver answers this request. Returns the updated request, or a [ContactRequestException]
     * if [actorAuthUid] isn't the receiver, the request was already answered, or [newStatus] isn't
     * an answer. Pure: it may run more than once inside a retried Firestore transaction.
     */
    fun respond(actorAuthUid: String, newStatus: ContactRequestStatus): Result<ContactRequest> = when {
        newStatus == ContactRequestStatus.PENDING -> Result.failure(ContactRequestException.InvalidResponse())
        actorAuthUid != receiverAuthUid -> Result.failure(ContactRequestException.NotReceiver())
        status != ContactRequestStatus.PENDING -> Result.failure(ContactRequestException.AlreadyAnswered())
        else -> Result.success(copy(status = newStatus))
    }

    companion object {

        /**
         * Builds a new PENDING request from [senderAuthUid] to [receiverAuthUid], given the request
         * that currently exists between them ([existing], null if none).
         *
         * Sending is allowed when no request exists, or when the existing one was DECLINED *by the
         * person now sending* (the decliner may change their mind; the declined sender can't ask
         * again). The sender/receiver roles of the new request follow who sends now.
         * Pure, like [respond].
         */
        fun send(
            existing: ContactRequest?,
            senderAuthUid: String,
            receiverAuthUid: String,
            createdAt: Long
        ): Result<ContactRequest> {
            if (existing != null) {
                val refusal = when (existing.status) {
                    ContactRequestStatus.PENDING -> ContactRequestException.AlreadyPending()
                    ContactRequestStatus.ACCEPTED -> ContactRequestException.AlreadyConnected()
                    ContactRequestStatus.DECLINED ->
                        if (existing.receiverAuthUid == senderAuthUid) null
                        else ContactRequestException.CannotResend()
                }
                if (refusal != null) return Result.failure(refusal)
            }
            return Result.success(
                ContactRequest(
                    id = generateId(senderAuthUid, receiverAuthUid),
                    senderAuthUid = senderAuthUid,
                    receiverAuthUid = receiverAuthUid,
                    status = ContactRequestStatus.PENDING,
                    createdAt = createdAt
                )
            )
        }

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