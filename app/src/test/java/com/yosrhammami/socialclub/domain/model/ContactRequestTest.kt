package com.yosrhammami.socialclub.domain.model

import org.junit.Assert.*
import org.junit.Test

class ContactRequestTest {
    @Test
    fun `generateId returns the same id regardless of sender-receiver order`() {
        val idForward = ContactRequest.generateId("A1", "B2")
        val idReversed = ContactRequest.generateId("B2", "A1")

        assertEquals(idForward, idReversed)
    }

}