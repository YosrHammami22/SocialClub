package com.yosrhammami.socialclub.domain.model

// Lives in the domain (not the ViewModel/UI) so the use case and any future input counter
// share one source of truth for the limit.
const val MAX_MESSAGE_LENGTH = 1000

sealed class ChatException(message: String) : Exception(message) {
    class EmptyMessage : ChatException("Message is empty")
    class MessageTooLong(val max: Int) : ChatException("Message exceeds $max characters")
}
