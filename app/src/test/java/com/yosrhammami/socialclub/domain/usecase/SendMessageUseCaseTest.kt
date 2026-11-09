package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.model.ChatException
import com.yosrhammami.socialclub.domain.model.MAX_MESSAGE_LENGTH
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import com.yosrhammami.socialclub.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SendMessageUseCaseTest {

    private val chatRepository = mockk<ChatRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = SendMessageUseCase(chatRepository, authRepository)

    // Default: "uidA" is signed in and the repository accepts the message. Tests override as needed.
    init {
        every { authRepository.currentUserUid() } returns "uidA"
        coEvery { chatRepository.sendMessage(any(), any()) } returns Result.success(Unit)
    }

    @Test
    fun `given blank text when sending then EmptyMessage is returned and the repository is never called`() = runTest {
        val result = useCase(receiverUid = "uidB", text = "   ")

        val error = result.exceptionOrNull()
        assertTrue(error is ChatException.EmptyMessage)
        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    @Test
    fun `given text of 1001 chars when sending then MessageTooLong is returned and the repository is never called`() = runTest {
        val result = useCase(receiverUid = "uidB", text = "a".repeat(1001))

        val error = result.exceptionOrNull()
        assertTrue(error is ChatException.MessageTooLong)
        assertEquals(MAX_MESSAGE_LENGTH, (error as ChatException.MessageTooLong).max)
        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    // Guards against validating the trimmed text: trimmed this is 999 chars, but the raw 1001 is what gets sent.
    @Test
    fun `given 999 chars plus 2 spaces when sending then MessageTooLong is returned and the repository is never called`() = runTest {
        val result = useCase(receiverUid = "uidB", text = " " + "a".repeat(999) + " ")

        val error = result.exceptionOrNull()
        assertTrue(error is ChatException.MessageTooLong)
        assertEquals(MAX_MESSAGE_LENGTH, (error as ChatException.MessageTooLong).max)
        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    @Test
    fun `given text of exactly 1000 chars when sending then the repository is called`() = runTest {
        val text = "a".repeat(1000)

        val result = useCase(receiverUid = "uidB", text = text)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { chatRepository.sendMessage("uidA_uidB", text) }
    }

    @Test
    fun `given no signed-in user when sending then NoSignedInUser is returned and the repository is never called`() = runTest {
        every { authRepository.currentUserUid() } returns null

        val result = useCase(receiverUid = "uidB", text = "Hello")

        val error = result.exceptionOrNull()
        assertTrue(error is AuthException.NoSignedInUser)
        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    @Test
    fun `given the receiver is the current user when sending then IllegalArgumentException is returned and the repository is never called`() = runTest {
        val result = useCase(receiverUid = "uidA", text = "Hello")

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    @Test
    fun `given valid input when sending then the raw text is sent once to the pair conversation`() = runTest {
        // Leading/trailing spaces on purpose: the use case must send the text untrimmed.
        val result = useCase(receiverUid = "uidB", text = "  Hello  ")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { chatRepository.sendMessage("uidA_uidB", "  Hello  ") }
    }

    @Test
    fun `given the repository fails when sending then that same failure is returned`() = runTest {
        val repositoryError = Exception("Network error")
        coEvery { chatRepository.sendMessage(any(), any()) } returns Result.failure(repositoryError)

        val result = useCase(receiverUid = "uidB", text = "Hello")

        assertSame(repositoryError, result.exceptionOrNull())
    }
}
