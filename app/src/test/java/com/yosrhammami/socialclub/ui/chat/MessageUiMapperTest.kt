package com.yosrhammami.socialclub.ui.chat

import com.yosrhammami.socialclub.domain.model.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

/**
 * Pure mapper → no mocks, no dispatcher rule. Every test passes an explicit [ZoneId]:
 * relying on ZoneId.systemDefault() would make the "HH:mm" assertions pass on one machine
 * and fail on a CI runner in another time zone.
 */
class MessageUiMapperTest {

    private val utc = ZoneId.of("UTC")
    private val paris = ZoneId.of("Europe/Paris") // UTC+1 in November (no DST)

    private fun message(
        senderUid: String = "uidAlice",
        sentAtIso: String = "2026-11-08T14:05:00Z",
        isPending: Boolean = false,
    ) = Message(
        id = "msg-42",
        senderUid = senderUid,
        text = "See you at the rooftop event tonight?",
        sentAt = Instant.parse(sentAtIso).toEpochMilli(),
        isPending = isPending,
    )

    @Test
    fun `when the current user sent the message, it maps to a bubble marked as mine with every field`() {
        val ui = message(senderUid = "uidAlice").toUi(currentUid = "uidAlice", zoneId = utc)

        assertEquals(
            MessageUi(
                id = "msg-42",
                text = "See you at the rooftop event tonight?",
                time = "14:05",
                isMine = true,
                isPending = false,
            ),
            ui,
        )
    }

    @Test
    fun `when another attendee sent the message, isMine is false`() {
        val ui = message(senderUid = "uidBob").toUi(currentUid = "uidAlice", zoneId = utc)

        assertFalse(ui.isMine)
    }

    @Test
    fun `when there is no current user, the message is never mine`() {
        // Session can be cleared (logout / expiry) while the chat is still on screen.
        val ui = message(senderUid = "uidAlice").toUi(currentUid = null, zoneId = utc)

        assertFalse(ui.isMine)
    }

    @Test
    fun `a pending message stays pending after mapping`() {
        val ui = message(isPending = true).toUi(currentUid = "uidAlice", zoneId = utc)

        assertTrue(ui.isPending)
    }

    @Test
    fun `the same instant is formatted in the given time zone`() {
        val msg = message(sentAtIso = "2026-11-08T14:05:00Z")

        assertEquals("14:05", msg.toUi(currentUid = "uidAlice", zoneId = utc).time)
        assertEquals("15:05", msg.toUi(currentUid = "uidAlice", zoneId = paris).time)
    }

    @Test
    fun `time uses a zero-padded 24-hour clock, even across midnight`() {
        // 23:30 UTC → 00:30 the next day in Paris: checks "HH" (not "hh"/"H") and zone-driven day rollover.
        val ui = message(sentAtIso = "2026-11-08T23:30:00Z").toUi(currentUid = "uidAlice", zoneId = paris)

        assertEquals("00:30", ui.time)
    }
}
