package com.yosrhammami.socialclub.ui.chat

/**
 * What one chat bubble needs to render — a presentation model, not the future domain `Message`.
 *
 * [sentTime] is already formatted ("HH:mm"): formatting depends on the device's time zone and
 * locale, which belongs in the ViewModel (testable on the JVM), not in a Composable — and it keeps
 * previews deterministic regardless of the machine's time zone.
 *
 * [isMine] is resolved upstream (senderId == current user's authUid) so the bubble never needs to
 * know who is logged in.
 */
data class ChatMessageUi(
    val id: String,
    val text: String,
    val sentTime: String,
    val isMine: Boolean,
    val isPending: Boolean = false
)
