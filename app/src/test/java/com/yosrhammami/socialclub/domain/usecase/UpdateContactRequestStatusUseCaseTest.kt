package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UpdateContactRequestStatusUseCaseTest {

    private val repository = mockk<ContactRequestRepository>()
    private val useCase = UpdateContactRequestStatusUseCase(repository)

    @Test
    fun `when fromAttendeeId equals toAttendeeId, returns failure`() = runTest {
        val result = useCase(fromAttendeeId = "A1", toAttendeeId = "A1", status = ContactRequestStatus.ACCEPTED)

        assertTrue(result.isFailure)
        assertEquals(
            "Cannot send a contact request to yourself",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `when toAttendeeId is empty, returns failure`() = runTest {
        val result = useCase(fromAttendeeId = "A1", toAttendeeId = "", status = ContactRequestStatus.ACCEPTED)

        assertTrue(result.isFailure)
        assertEquals(
            "Cannot send a contact request issues with empty id",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `when status is PENDING, returns failure`() = runTest {
        val result = useCase(fromAttendeeId = "A1", toAttendeeId = "B2", status = ContactRequestStatus.PENDING)

        assertTrue(result.isFailure)
        assertEquals("Cannot update to PENDING", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when status is ACCEPTED, calls repository and returns its result`() = runTest {
        coEvery {
            repository.updateContactRequest("A1", "B2", ContactRequestStatus.ACCEPTED)
        } returns Result.success(Unit)

        val result = useCase(fromAttendeeId = "A1", toAttendeeId = "B2", status = ContactRequestStatus.ACCEPTED)

        assertTrue(result.isSuccess)
        coVerify { repository.updateContactRequest("A1", "B2", ContactRequestStatus.ACCEPTED) }
    }

    @Test
    fun `when status is DECLINED, calls repository and returns its result`() = runTest {
        coEvery {
            repository.updateContactRequest("A1", "B2", ContactRequestStatus.DECLINED)
        } returns Result.success(Unit)

        val result = useCase(fromAttendeeId = "A1", toAttendeeId = "B2", status = ContactRequestStatus.DECLINED)

        assertTrue(result.isSuccess)
        coVerify { repository.updateContactRequest("A1", "B2", ContactRequestStatus.DECLINED) }
    }
}