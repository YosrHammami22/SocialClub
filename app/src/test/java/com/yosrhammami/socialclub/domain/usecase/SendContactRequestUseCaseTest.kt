package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.FakeContactRequestRepository
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestException
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SendContactRequestUseCaseTest {
    private val repository = FakeContactRequestRepository()
    private val useCase = SendContactRequestUseCase(repository)

    private val pairId = ContactRequest.generateId("A1", "B2")

    @Test
    fun `when senderId equals receiverId, request is rejected`() = runTest {
        val result = useCase(senderId = "A1", receiverId = "A1")

        assertEquals("Cannot send a contact request to yourself", result.exceptionOrNull()?.message)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `when a uid is invalid, a failure is returned instead of throwing`() = runTest {
        val result = useCase(senderId = "A_1", receiverId = "B2")

        assertEquals("uid must not contain '_'", result.exceptionOrNull()?.message)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun `when no request exists, a PENDING request from the sender is stored`() = runTest {
        val result = useCase(senderId = "A1", receiverId = "B2", currentTime = 5_000L)

        assertTrue(result.isSuccess)
        assertEquals(
            ContactRequest(pairId, senderAuthUid = "A1", receiverAuthUid = "B2", status = ContactRequestStatus.PENDING, createdAt = 5_000L),
            repository.requests[pairId]
        )
    }

    // Used to be a silent overwrite: the other person's pending request was replaced by ours.
    @Test
    fun `when the other person already sent a request, it is refused and theirs is kept`() = runTest {
        val theirs = ContactRequest(pairId, "B2", "A1", ContactRequestStatus.PENDING, createdAt = 1_000L)
        repository.requests[pairId] = theirs

        val result = useCase(senderId = "A1", receiverId = "B2")

        assertEquals("A contact request is already pending between you.", result.exceptionOrNull()?.message)
        assertEquals(theirs, repository.requests[pairId])
    }

    @Test
    fun `when my request was declined, I cannot send again and the decline is kept`() = runTest {
        val declined = ContactRequest(pairId, "A1", "B2", ContactRequestStatus.DECLINED, createdAt = 1_000L)
        repository.requests[pairId] = declined

        val result = useCase(senderId = "A1", receiverId = "B2")

        assertEquals("You can't send another request to this person.", result.exceptionOrNull()?.message)
        assertEquals(declined, repository.requests[pairId])
    }

    @Test
    fun `when I declined their request, I can send a new one`() = runTest {
        repository.requests[pairId] = ContactRequest(pairId, "B2", "A1", ContactRequestStatus.DECLINED, createdAt = 1_000L)

        val result = useCase(senderId = "A1", receiverId = "B2", currentTime = 5_000L)

        assertTrue(result.isSuccess)
        assertEquals(
            ContactRequest(pairId, senderAuthUid = "A1", receiverAuthUid = "B2", status = ContactRequestStatus.PENDING, createdAt = 5_000L),
            repository.requests[pairId]
        )
    }

    @Test
    fun `when the repository fails, its error is returned`() = runTest {
        repository.failure = Exception("Network error")

        val result = useCase(senderId = "A1", receiverId = "B2")

        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
