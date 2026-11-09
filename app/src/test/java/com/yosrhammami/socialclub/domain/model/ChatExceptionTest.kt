package com.yosrhammami.socialclub.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ChatExceptionTest {

    @Test
    fun `ContactBlocked keeps the original error as its cause, with its message unchanged`() {
        val original = IllegalStateException("PERMISSION_DENIED: Missing or insufficient permissions.")

        val exception = ChatException.ContactBlocked(cause = original)

        assertSame(original, exception.cause)
        assertEquals("Contact is blocked", exception.message)
    }

    @Test
    fun `ContactBlocked without a cause has a null cause`() {
        assertNull(ChatException.ContactBlocked().cause)
    }

    @Test
    fun `EmptyMessage and MessageTooLong keep their messages and have no cause`() {
        val empty = ChatException.EmptyMessage()
        val tooLong = ChatException.MessageTooLong(1000)

        assertEquals("Message is empty", empty.message)
        assertNull(empty.cause)
        assertEquals("Message exceeds 1000 characters", tooLong.message)
        assertEquals(1000, tooLong.max)
        assertNull(tooLong.cause)
    }
}
