package com.yosrhammami.socialclub.domain.usecase

import app.cash.turbine.test
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.model.Message
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import com.yosrhammami.socialclub.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveMessagesUseCaseTest {

    private val chatRepository = mockk<ChatRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = ObserveMessagesUseCase(chatRepository, authRepository)

    private val firstBatch = listOf(Message("m1", "uidA", "Hi Lina!", sentAt = 1_000L, isPending = true))
    private val secondBatch = firstBatch + Message("m2", "uidB", "Hey Sami, see you at the meetup?", sentAt = 2_000L, isPending = true)

    @Test
    fun `given a signed-in user when observing then the pair conversation is observed and lists are forwarded unchanged`() = runTest {
        every { authRepository.currentUserUid() } returns "uidA"
        every { chatRepository.observeMessages("uidA_uidB") } returns flowOf(firstBatch, secondBatch)

        useCase(receiverUid = "uidB").test {
            assertEquals(firstBatch, awaitItem())
            assertEquals(secondBatch, awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { chatRepository.observeMessages("uidA_uidB") }
    }

    @Test
    fun `given no signed-in user when observing then the flow fails with NoSignedInUser and the repository is never called`() = runTest {
        every { authRepository.currentUserUid() } returns null

        useCase(receiverUid = "uidB").test {
            val error = awaitError()
            assertTrue(error is AuthException.NoSignedInUser)
        }
        verify(exactly = 0) { chatRepository.observeMessages(any()) }
    }

    @Test
    fun `given the repository flow fails when observing then the error is propagated`() = runTest {
        val repositoryError = Exception("Firestore listener failed")
        every { authRepository.currentUserUid() } returns "uidA"
        every { chatRepository.observeMessages("uidA_uidB") } returns flow { throw repositoryError }

        useCase(receiverUid = "uidB").test {
            assertSame(repositoryError, awaitError())
        }
    }
}
