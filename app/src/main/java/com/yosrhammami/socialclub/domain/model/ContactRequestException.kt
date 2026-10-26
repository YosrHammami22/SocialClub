package com.yosrhammami.socialclub.domain.model

/*
Domain-level reasons a contact request change is refused, same pattern as AuthException:
each carries a user-readable message, since the ViewModel surfaces e.message directly.
 */
sealed class ContactRequestException(message: String) : Exception(message) {
    class NotFound : ContactRequestException("This contact request no longer exists.")

    class NotReceiver : ContactRequestException("Only the person who received this request can answer it.")

    class AlreadyAnswered : ContactRequestException("This request has already been answered.")

    class InvalidResponse : ContactRequestException("A request can only be accepted or declined.")

    class AlreadyPending : ContactRequestException("A contact request is already pending between you.")

    class AlreadyConnected : ContactRequestException("You're already connected.")

    // Deliberately neutral wording: it doesn't tell the sender they were declined.
    class CannotResend : ContactRequestException("You can't send another request to this person.")
}
