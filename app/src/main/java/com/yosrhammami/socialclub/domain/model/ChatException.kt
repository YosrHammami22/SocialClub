package com.yosrhammami.socialclub.domain.model

// Lives in the domain (not the ViewModel/UI) so the use case and any future input counter
// share one source of truth for the limit.
const val MAX_MESSAGE_LENGTH = 1000

// Optional cause: a domain error can wrap the lower-level error that produced it, so the
// original stays visible in logs instead of being replaced.
sealed class ChatException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class EmptyMessage : ChatException("Message is empty")
    class MessageTooLong(val max: Int) : ChatException("Message exceeds $max characters")
    class ContactBlocked(cause: Throwable? = null) : ChatException("Contact is blocked", cause)
}
