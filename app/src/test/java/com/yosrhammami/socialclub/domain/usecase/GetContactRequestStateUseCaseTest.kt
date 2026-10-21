package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetContactRequestStateUseCaseTest {

    private val repository = mockk<ContactRequestRepository>()
    private val useCase = GetContactRequestStateUseCase(repository)

    @Test
    fun `when no request exists, state is NoRequest`() = runTest {
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(null)

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()

        assertEquals(
            ContactRequestButtonState.NoRequest,
            result
        )
    }
    @Test
    fun `when request is pending and I am the sender, state is PendingSender`() = runTest {
        val request = ContactRequest(
            id = "someId", senderAuthUid = "A1", receiverAuthUid = "B2",
            status = ContactRequestStatus.PENDING, createdAt = 0L
        )
        every { repository.observeContactRequest(any(), any()) } returns flowOf(request)
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()
        assertEquals(ContactRequestButtonState.PendingSender, result)
    }

    @Test
    fun `when request is pending and I am the receiver, state is PendingReceiver`() = runTest {
        val request = ContactRequest(
            id = "someId",
            senderAuthUid = "B2",
            receiverAuthUid = "A1",
            status = ContactRequestStatus.PENDING,
            createdAt = 0L
        )
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(request)

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()

        assertEquals(
            ContactRequestButtonState.PendingReceiver,
            result
        )
    }

    @Test
    fun `when my request was declined, state is Declined`() = runTest {
        val request = ContactRequest(
            id = "someId",
            senderAuthUid = "A1",
            receiverAuthUid = "B2",
            status = ContactRequestStatus.DECLINED,
            createdAt = 0L
        )
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(request)
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()
        assertEquals(
            ContactRequestButtonState.Declined,
            result
        )
    }

    @Test
    fun `when request is accepted, state is Accepted`() = runTest {
        val request = ContactRequest(
            id = "someId",
            senderAuthUid = "B2",
            receiverAuthUid = "A1",
            status = ContactRequestStatus.ACCEPTED,
            createdAt = 0L
        )
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(request)
        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()
        assertEquals(
            ContactRequestButtonState.Accepted,
            result
        )
    }

    // Mirrors ContactRequest.send(): whoever declined may send a new request, so they get the button.
    @Test
    fun `when I declined their request, state is NoRequest`() = runTest {
        val request = ContactRequest(id = "someId", senderAuthUid = "B2", receiverAuthUid = "A1", status = ContactRequestStatus.DECLINED, createdAt = 0L)
        every { repository.observeContactRequest(any(), any()) } returns flowOf(request)

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()

        assertEquals(ContactRequestButtonState.NoRequest, result)
    }

    // A corrupt document where neither uid is mine: no action at all, rather than Confirm/Delete.
    @Test
    fun `when I am neither sender nor receiver, state is Unknown`() = runTest {
        val request = ContactRequest(id = "someId", senderAuthUid = "B2", receiverAuthUid = "C3", status = ContactRequestStatus.PENDING, createdAt = 0L)
        every { repository.observeContactRequest(any(), any()) } returns flowOf(request)

        val result = useCase(currentAuthUid = "A1", otherAuthUid = "B2").first()

        assertEquals(ContactRequestButtonState.Unknown, result)
    }
}
