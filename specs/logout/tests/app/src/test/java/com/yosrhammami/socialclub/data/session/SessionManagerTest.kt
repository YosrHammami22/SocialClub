package com.yosrhammami.socialclub.data.session

import com.yosrhammami.socialclub.domain.model.Attendee
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * T3 · Data · Covers AC5
 * After clear(), currentAttendee is null, so no screen can keep
 * showing the previous user.
 * (Project convention: assert on .value, no Turbine needed for a final state.)
 */
class SessionManagerTest {

    private val sessionManager = SessionManager()

    @Test
    fun `clear sets currentAttendee to null`() {
        val alice: Attendee = mockk()

        // ADAPT: use the real setter name from your SessionManager.
        sessionManager.setAttendee(alice)
        assertEquals(alice, sessionManager.currentAttendee.value)

        sessionManager.clear()

        assertNull(sessionManager.currentAttendee.value)
    }
}
