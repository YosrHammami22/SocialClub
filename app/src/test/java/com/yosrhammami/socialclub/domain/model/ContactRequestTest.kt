package com.yosrhammami.socialclub.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ContactRequestTest {

    // Rule 1 — named arguments pin the public signature, so renaming a
    // parameter breaks this test at compile time instead of silently.
    @Test
    fun `generateId takes senderUid and receiverUid and returns a String`() {
        val id: String = ContactRequest.generateId(senderUid = "alice", receiverUid = "bob")

        assertEquals("alice_bob", id)
    }

    // Rule 2
    @Test
    fun `generateId is deterministic for the same inputs`() {
        val first = ContactRequest.generateId("alice", "bob")
        val second = ContactRequest.generateId("alice", "bob")

        assertEquals(first, second)
    }

    // Rule 3
    @Test
    fun `generateId is symmetric regardless of argument order`() {
        val forward = ContactRequest.generateId("alice", "bob")
        val reversed = ContactRequest.generateId("bob", "alice")

        assertEquals(forward, reversed)
    }

    // Rule 4 — "Zed" < "alice" because uppercase sorts before lowercase in
    // String.compareTo; this pins lexicographic (not case-insensitive) ordering.
    @Test
    fun `generateId formats as smaller underscore larger using lexicographic order`() {
        assertEquals("Zed_alice", ContactRequest.generateId("alice", "Zed"))
    }

    // Rule 5 — covers empty and whitespace-only, on either side.
    @Test
    fun `generateId throws IllegalArgumentException when a uid is blank`() {
        val emptySender = assertThrows(IllegalArgumentException::class.java) {
            ContactRequest.generateId("", "bob")
        }
        val blankReceiver = assertThrows(IllegalArgumentException::class.java) {
            ContactRequest.generateId("alice", "   ")
        }

        assertEquals("uid must not be blank", emptySender.message)
        assertEquals("uid must not be blank", blankReceiver.message)
    }

    // Rule 6
    @Test
    fun `generateId throws IllegalArgumentException when both uids are equal`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            ContactRequest.generateId("alice", "alice")
        }

        assertEquals("Cannot send a contact request to yourself", error.message)
    }

    // Rule 7 — "_" is the separator, so allowing it would make ids ambiguous
    // (e.g. "a_b" + "c" and "a" + "b_c" would both produce "a_b_c").
    @Test
    fun `generateId throws IllegalArgumentException when a uid contains an underscore`() {
        val inSender = assertThrows(IllegalArgumentException::class.java) {
            ContactRequest.generateId("a_b", "c")
        }
        val inReceiver = assertThrows(IllegalArgumentException::class.java) {
            ContactRequest.generateId("a", "b_c")
        }

        assertEquals("uid must not contain '_'", inSender.message)
        assertEquals("uid must not contain '_'", inReceiver.message)
    }
}
