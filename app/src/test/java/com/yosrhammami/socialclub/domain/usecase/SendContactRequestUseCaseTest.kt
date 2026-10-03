package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SendContactRequestUseCaseTest {
    private val repository = mockk<ContactRequestRepository>()
    private val useCase = SendContactRequestUseCase(repository)

    @Test
    fun `when senderId equals receiverId, request is rejected`() = runTest {
        val result = useCase(senderId = "A1", receiverId = "A1")

        // 1. assert on what came back — exact shape depends on your return type
        assertTrue(result.isFailure)   // or: assertTrue(result is ContactRequestResult.Rejected), etc.
        assertEquals("Cannot send a contact request to yourself", result.exceptionOrNull()?.message)

        // 2. the more important assertion for THIS test:
        coVerify(exactly = 0) { repository.send(any()) }
    }

}