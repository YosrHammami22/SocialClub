package com.yosrhammami.socialclub.ui.chat

/**
 * Everything the chat body can be. One sealed interface (not `isLoading`/`error`/`messages`
 * flags side by side) so impossible combinations — loading *and* blocked, an error *with* a
 * message list — can't be represented, and the `when` in the UI is exhaustive.
 *
 * The receiver's name/photo are deliberately not in here: the top bar shows them in every
 * state, so they're passed alongside instead of being repeated in each case.
 */
sealed interface ChatUiState {
    data object Loading : ChatUiState

    /** Conversation exists but has no messages yet. */
    data object Empty : ChatUiState

    /** Chronological order, oldest first — the list handles bottom-anchoring itself. */
    data class Content(val messages: List<MessageUi>) : ChatUiState

    /**
     * Contact is no longer available (removed/declined): no new messages can be sent, but the
     * existing history stays readable. Same shape as [Content] on purpose — only the ability
     * to send differs. Empty [messages] is valid (blocked before anyone wrote anything).
     */
    data class Blocked(val messages: List<MessageUi>) : ChatUiState

    data class Error(val message: String) : ChatUiState
}
