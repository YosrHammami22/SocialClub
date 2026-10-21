package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateAttendeeAccountUseCaseTest {
    private val attendeeRepository = mockk<AttendeeRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = CreateAttendeeAccountUseCase(attendeeRepository, authRepository)

    private val jane = Attendee(id = "a1", fullName = "Jane Doe", email = "jane@test.com", age = 33)

    @Test
    fun `when account is created, the new uid is written to that attendee`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } returns jane
        coEvery { authRepository.createAccount("jane@test.com", "sunny-Harbor42") } returns Result.success("uid-123")
        coEvery { attendeeRepository.linkAuthUid("a1", "uid-123") } returns Result.success(Unit)

        val result = useCase(attendeeId = "a1", password = "sunny-Harbor42")

        assertTrue(result.isSuccess)
        coVerifyOrder {
            authRepository.createAccount("jane@test.com", "sunny-Harbor42")
            attendeeRepository.linkAuthUid("a1", "uid-123")
        }
        coVerify(exactly = 0) { authRepository.deleteCurrentAccount() }
    }

    @Test
    fun `when attendee does not exist, it fails without creating an account`() = runTest {
        coEvery { attendeeRepository.getAttendee("missing") } returns null

        val result = useCase(attendeeId = "missing", password = "sunny-Harbor42")

        assertEquals("We couldn't find this attendee.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.createAccount(any(), any()) }
    }

    @Test
    fun `when attendee already has an authUid, it fails without creating a second account`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } returns jane.copy(authUid = "uid-existing")

        val result = useCase(attendeeId = "a1", password = "sunny-Harbor42")

        assertEquals("This attendee already has an account.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.createAccount(any(), any()) }
        coVerify(exactly = 0) { attendeeRepository.linkAuthUid(any(), any()) }
    }

    @Test
    fun `when loading the attendee throws, the failure is returned`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } throws Exception("Firestore unavailable")

        val result = useCase(attendeeId = "a1", password = "sunny-Harbor42")

        assertEquals("Firestore unavailable", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.createAccount(any(), any()) }
    }

    @Test
    fun `when account creation fails, nothing is written and nothing is rolled back`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } returns jane
        coEvery { authRepository.createAccount(any(), any()) } returns Result.failure(AuthException.EmailAlreadyInUse())

        val result = useCase(attendeeId = "a1", password = "sunny-Harbor42")

        assertEquals("An account already exists for this email.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { attendeeRepository.linkAuthUid(any(), any()) }
        // Nothing of ours to delete: deleting here could remove someone else's signed-in account.
        coVerify(exactly = 0) { authRepository.deleteCurrentAccount() }
    }

    @Test
    fun `when writing the uid fails, the new account is deleted and the write error is returned`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } returns jane
        coEvery { authRepository.createAccount(any(), any()) } returns Result.success("uid-123")
        coEvery { attendeeRepository.linkAuthUid("a1", "uid-123") } returns Result.failure(Exception("Permission denied"))
        coEvery { authRepository.deleteCurrentAccount() } returns Result.success(Unit)

        val result = useCase(attendeeId = "a1", password = "sunny-Harbor42")

        assertEquals("Permission denied", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { authRepository.deleteCurrentAccount() }
    }

    @Test
    fun `when the rollback itself fails, the original write error is still the one returned`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } returns jane
        coEvery { authRepository.createAccount(any(), any()) } returns Result.success("uid-123")
        coEvery { attendeeRepository.linkAuthUid("a1", "uid-123") } returns Result.failure(Exception("Permission denied"))
        coEvery { authRepository.deleteCurrentAccount() } returns Result.failure(Exception("Network error"))

        val result = useCase(attendeeId = "a1", password = "sunny-Harbor42")

        assertEquals("Permission denied", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when the attendee lookup is cancelled, cancellation propagates and no account is created`() = runTest {
        coEvery { attendeeRepository.getAttendee("a1") } throws CancellationException("screen left")

        val error = try {
            useCase(attendeeId = "a1", password = "sunny-Harbor42")
            null
        } catch (e: CancellationException) {
            e
        }

        assertEquals("screen left", error?.message)
        coVerify(exactly = 0) { authRepository.createAccount(any(), any()) }
    }

    // The user backs out while createAccount is in flight: the account still gets created
    // server-side, so the link must still be written once it returns.
    @Test
    fun `when cancelled during account creation, the uid is still linked`() = runTest {
        val accountCreated = CompletableDeferred<Result<String>>()
        coEvery { attendeeRepository.getAttendee("a1") } returns jane
        coEvery { authRepository.createAccount("jane@test.com", "sunny-Harbor42") } coAnswers { accountCreated.await() }
        coEvery { attendeeRepository.linkAuthUid("a1", "uid-123") } returns Result.success(Unit)

        val job = launch { useCase(attendeeId = "a1", password = "sunny-Harbor42") }
        runCurrent()
        job.cancel()
        accountCreated.complete(Result.success("uid-123"))
        advanceUntilIdle()

        coVerify(exactly = 1) { attendeeRepository.linkAuthUid("a1", "uid-123") }
        coVerify(exactly = 0) { authRepository.deleteCurrentAccount() }
    }

    @Test
    fun `when cancelled and the link then fails, the account is still rolled back`() = runTest {
        val accountCreated = CompletableDeferred<Result<String>>()
        coEvery { attendeeRepository.getAttendee("a1") } returns jane
        coEvery { authRepository.createAccount("jane@test.com", "sunny-Harbor42") } coAnswers { accountCreated.await() }
        coEvery { attendeeRepository.linkAuthUid("a1", "uid-123") } returns Result.failure(Exception("Permission denied"))
        coEvery { authRepository.deleteCurrentAccount() } returns Result.success(Unit)

        val job = launch { useCase(attendeeId = "a1", password = "sunny-Harbor42") }
        runCurrent()
        job.cancel()
        accountCreated.complete(Result.success("uid-123"))
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.deleteCurrentAccount() }
    }
}
