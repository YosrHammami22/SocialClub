package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.FakeContactRequestRepository
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UpdateContactRequestStatusUseCaseTest {

    private val repository = FakeContactRequestRepository()
    private val useCase = UpdateContactRequestStatusUseCase(repository)

    private val pairId = ContactRequest.generateId("A1", "B2")
    // B2 asked A1: A1 is the receiver, the only one allowed to answer.
    private val pendingFromB2 = ContactRequest(pairId, senderAuthUid = "B2", receiverAuthUid = "A1", status = ContactRequestStatus.PENDING, createdAt = 1_000L)

    @Test
    fun `when currentAuthUid equals otherAuthUid, returns failure`() = runTest {
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "A1", status = ContactRequestStatus.ACCEPTED)

        assertTrue(result.isFailure)
        assertEquals(
            "Cannot send a contact request to yourself",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `when otherAuthUid is empty, returns failure`() = runTest {
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "", status = ContactRequestStatus.ACCEPTED)

        assertTrue(result.isFailure)
        assertEquals(
            "Cannot send a contact request issues with empty id",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `when status is PENDING, returns failure`() = runTest {
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2", status = ContactRequestStatus.PENDING)

        assertTrue(result.isFailure)
        assertEquals("A request can only be accepted or declined.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when the receiver accepts a pending request, it is stored as ACCEPTED`() = runTest {
        repository.requests[pairId] = pendingFromB2

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2", status = ContactRequestStatus.ACCEPTED)

        assertTrue(result.isSuccess)
        assertEquals(pendingFromB2.copy(status = ContactRequestStatus.ACCEPTED), repository.requests[pairId])
    }

    @Test
    fun `when the receiver declines a pending request, it is stored as DECLINED`() = runTest {
        repository.requests[pairId] = pendingFromB2

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2", status = ContactRequestStatus.DECLINED)

        assertTrue(result.isSuccess)
        assertEquals(pendingFromB2.copy(status = ContactRequestStatus.DECLINED), repository.requests[pairId])
    }

    // H2: the sender used to be able to accept their own request.
    @Test
    fun `when the sender tries to accept their own request, it is refused and unchanged`() = runTest {
        repository.requests[pairId] = pendingFromB2

        val result = useCase(currentAuthUid = "B2", otherAuthUid = "A1", status = ContactRequestStatus.ACCEPTED)

        assertEquals("Only the person who received this request can answer it.", result.exceptionOrNull()?.message)
        assertEquals(pendingFromB2, repository.requests[pairId])
    }

    @Test
    fun `when the request was already declined, accepting it is refused`() = runTest {
        val declined = pendingFromB2.copy(status = ContactRequestStatus.DECLINED)
        repository.requests[pairId] = declined

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2", status = ContactRequestStatus.ACCEPTED)

        assertEquals("This request has already been answered.", result.exceptionOrNull()?.message)
        assertEquals(declined, repository.requests[pairId])
    }

    @Test
    fun `when no request exists, a NotFound failure is returned`() = runTest {
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2", status = ContactRequestStatus.ACCEPTED)

        assertEquals("This contact request no longer exists.", result.exceptionOrNull()?.message)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `when a uid is invalid, a failure is returned instead of throwing`() = runTest {
        val result = useCase(currentAuthUid = "A_1", otherAuthUid = "B2", status = ContactRequestStatus.ACCEPTED)

        assertEquals("uid must not contain '_'", result.exceptionOrNull()?.message)
    }
}
