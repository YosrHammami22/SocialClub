package com.yosrhammami.socialclub.data.remote

import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ContactRequestDtoTest {

    private val domain = ContactRequest(
        id = "uidAlice_uidBob",
        senderAuthUid = "uidAlice",
        receiverAuthUid = "uidBob",
        status = ContactRequestStatus.PENDING,
        createdAt = 1_729_600_000_000L
    )

    private val dto = ContactRequestDto(
        id = "uidAlice_uidBob",
        senderAuthUid = "uidAlice",
        receiverAuthUid = "uidBob",
        status = "PENDING",
        createdAt = 1_729_600_000_000L
    )

    // Rule 1 — named arguments pin the domain field names at compile time.
    @Test
    fun `toDomain maps sender and receiver auth uids onto the domain model`() {
        val result = dto.toDomain()

        assertEquals("uidAlice", result.senderAuthUid)
        assertEquals("uidBob", result.receiverAuthUid)
        assertEquals("uidAlice_uidBob", result.id)
        assertEquals(1_729_600_000_000L, result.createdAt)
    }

    // Rule 2 — the DTO property names ARE the Firestore field names (toObject/set
    // map by name), so asserting them here guards the stored document shape.
    @Test
    fun `toDto writes senderAuthUid, receiverAuthUid, status and createdAt`() {
        val result = domain.toDto()

        assertEquals("uidAlice", result.senderAuthUid)
        assertEquals("uidBob", result.receiverAuthUid)
        assertEquals("PENDING", result.status)
        assertEquals(1_729_600_000_000L, result.createdAt)
    }

    // Rule 3 — every enum value is stored as its uppercase name, both directions.
    @Test
    fun `status is stored as the uppercase enum name`() {
        assertEquals("PENDING", domain.copy(status = ContactRequestStatus.PENDING).toDto().status)
        assertEquals("ACCEPTED", domain.copy(status = ContactRequestStatus.ACCEPTED).toDto().status)
        assertEquals("DECLINED", domain.copy(status = ContactRequestStatus.DECLINED).toDto().status)

        assertEquals(ContactRequestStatus.PENDING, dto.copy(status = "PENDING").toDomain().status)
        assertEquals(ContactRequestStatus.ACCEPTED, dto.copy(status = "ACCEPTED").toDomain().status)
        assertEquals(ContactRequestStatus.DECLINED, dto.copy(status = "DECLINED").toDomain().status)
    }

    // Rule 4
    @Test
    fun `domain to DTO to domain round trip returns an equal object`() {
        ContactRequestStatus.entries.forEach { status ->
            val original = domain.copy(status = status)

            assertEquals(original, original.toDto().toDomain())
        }
    }

    // Rule 4 — the other direction, so a field dropped only by toDomain is caught too.
    @Test
    fun `DTO to domain to DTO round trip returns an equal object`() {
        assertEquals(dto, dto.toDomain().toDto())
    }

    // Rule 5 — an unrecognised status is corrupt data, not "no request":
    // fail loudly with the offending value instead of guessing a status.
    @Test
    fun `unknown status string throws IllegalArgumentException naming the value`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            dto.copy(status = "BLOCKED").toDomain()
        }

        assertEquals("Unknown ContactRequest status: 'BLOCKED'", error.message)
    }

    // Rule 5 — matching is case-sensitive; "pending" is not a stored value.
    @Test
    fun `lowercase status string throws instead of being normalised`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            dto.copy(status = "pending").toDomain()
        }

        assertEquals("Unknown ContactRequest status: 'pending'", error.message)
    }

    // Rule 5 — "" is what toObject() leaves when the field is missing from the document.
    @Test
    fun `missing status field throws instead of defaulting to PENDING`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            ContactRequestDto(id = "uidAlice_uidBob", senderAuthUid = "uidAlice", receiverAuthUid = "uidBob").toDomain()
        }

        assertEquals("Unknown ContactRequest status: ''", error.message)
    }
}
