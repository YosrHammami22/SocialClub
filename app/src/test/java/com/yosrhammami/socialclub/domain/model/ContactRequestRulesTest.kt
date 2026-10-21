package com.yosrhammami.socialclub.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactRequestRulesTest {

    private fun request(
        sender: String = "alice",
        receiver: String = "bob",
        status: ContactRequestStatus = ContactRequestStatus.PENDING
    ) = ContactRequest(
        id = ContactRequest.generateId(sender, receiver),
        senderAuthUid = sender,
        receiverAuthUid = receiver,
        status = status,
        createdAt = 1_000L
    )

    // ---------- send ----------

    @Test
    fun `send with no existing request creates a PENDING request from the sender`() {
        val result = ContactRequest.send(existing = null, senderAuthUid = "alice", receiverAuthUid = "bob", createdAt = 2_000L)

        assertEquals(request(status = ContactRequestStatus.PENDING).copy(createdAt = 2_000L), result.getOrNull())
    }

    @Test
    fun `send when a request is already pending is refused`() {
        val result = ContactRequest.send(request(sender = "bob", receiver = "alice"), "alice", "bob", 2_000L)

        assertTrue(result.exceptionOrNull() is ContactRequestException.AlreadyPending)
        assertEquals("A contact request is already pending between you.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `send when already connected is refused`() {
        val result = ContactRequest.send(request(status = ContactRequestStatus.ACCEPTED), "alice", "bob", 2_000L)

        assertTrue(result.exceptionOrNull() is ContactRequestException.AlreadyConnected)
        assertEquals("You're already connected.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `the person who declined can send a new request, becoming its sender`() {
        val declinedByBob = request(sender = "alice", receiver = "bob", status = ContactRequestStatus.DECLINED)

        val result = ContactRequest.send(declinedByBob, senderAuthUid = "bob", receiverAuthUid = "alice", createdAt = 2_000L)

        val sent = result.getOrNull()
        assertEquals("bob", sent?.senderAuthUid)
        assertEquals("alice", sent?.receiverAuthUid)
        assertEquals(ContactRequestStatus.PENDING, sent?.status)
        assertEquals(2_000L, sent?.createdAt)
    }

    @Test
    fun `the declined sender cannot send again`() {
        val declinedByBob = request(sender = "alice", receiver = "bob", status = ContactRequestStatus.DECLINED)

        val result = ContactRequest.send(declinedByBob, senderAuthUid = "alice", receiverAuthUid = "bob", createdAt = 2_000L)

        assertTrue(result.exceptionOrNull() is ContactRequestException.CannotResend)
        assertEquals("You can't send another request to this person.", result.exceptionOrNull()?.message)
    }

    // ---------- respond ----------

    @Test
    fun `the receiver can accept a pending request`() {
        val result = request().respond(actorAuthUid = "bob", newStatus = ContactRequestStatus.ACCEPTED)

        assertEquals(request(status = ContactRequestStatus.ACCEPTED), result.getOrNull())
    }

    @Test
    fun `the receiver can decline a pending request`() {
        val result = request().respond(actorAuthUid = "bob", newStatus = ContactRequestStatus.DECLINED)

        assertEquals(request(status = ContactRequestStatus.DECLINED), result.getOrNull())
    }

    // The bug H2 was about: nothing stopped the sender from accepting their own request.
    @Test
    fun `the sender cannot answer their own request`() {
        val result = request().respond(actorAuthUid = "alice", newStatus = ContactRequestStatus.ACCEPTED)

        assertTrue(result.exceptionOrNull() is ContactRequestException.NotReceiver)
        assertEquals("Only the person who received this request can answer it.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `an already answered request cannot be answered again`() {
        val result = request(status = ContactRequestStatus.DECLINED).respond(actorAuthUid = "bob", newStatus = ContactRequestStatus.ACCEPTED)

        assertTrue(result.exceptionOrNull() is ContactRequestException.AlreadyAnswered)
        assertEquals("This request has already been answered.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `PENDING is not a valid answer`() {
        val result = request().respond(actorAuthUid = "bob", newStatus = ContactRequestStatus.PENDING)

        assertTrue(result.exceptionOrNull() is ContactRequestException.InvalidResponse)
        assertEquals("A request can only be accepted or declined.", result.exceptionOrNull()?.message)
    }
}
