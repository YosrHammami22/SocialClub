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

        val result = useCase(
            fromAttendeeId = "A1",
            toAttendeeId = "B2"
        ).first()

        assertEquals(
            ContactRequestButtonState.NoRequest,
            result
        )
    }
    @Test
    fun `when request is pending and I am the sender, state is PendingSender`() = runTest {
        val request = ContactRequest(
            id = "someId", fromAttendeeId = "A1", toAttendeeId = "B2",
            status = ContactRequestStatus.PENDING, createdAt = 0L
        )
        every { repository.observeContactRequest(any(), any()) } returns flowOf(request)
        val result = useCase(fromAttendeeId = "A1", toAttendeeId = "B2").first()
        assertEquals(ContactRequestButtonState.PendingSender, result)
    }

    @Test
    fun `when request is pending and I am the receiver, state is PendingReceiver`() = runTest {
        val request = ContactRequest(
            id = "someId",
            fromAttendeeId = "B2",
            toAttendeeId = "A1",
            status = ContactRequestStatus.PENDING,
            createdAt = 0L
        )
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(request)

        val result = useCase(
            fromAttendeeId = "A1",
            toAttendeeId = "B2"
        ).first()

        assertEquals(
            ContactRequestButtonState.PendingReceiver,
            result
        )
    }

    @Test
    fun `when request is declined, state is Declined`() = runTest {
        val request = ContactRequest(
            id = "someId",
            fromAttendeeId = "B2",
            toAttendeeId = "A1",
            status = ContactRequestStatus.DECLINED,
            createdAt = 0L
        )
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(request)
        val result = useCase(
            fromAttendeeId = "A1",
            toAttendeeId = "B2"
        ).first()
        assertEquals(
            ContactRequestButtonState.Declined,
            result
        )
    }

    @Test
    fun `when request is accepted, state is Accepted`() = runTest {
        val request = ContactRequest(
            id = "someId",
            fromAttendeeId = "B2",
            toAttendeeId = "A1",
            status = ContactRequestStatus.ACCEPTED,
            createdAt = 0L
        )
        every {
            repository.observeContactRequest(
                any(),
                any()
            )
        } returns flowOf(request)
        val result = useCase(
            fromAttendeeId = "A1",
            toAttendeeId = "B2"
        ).first()
        assertEquals(
            ContactRequestButtonState.Accepted,
            result
        )
    }
}