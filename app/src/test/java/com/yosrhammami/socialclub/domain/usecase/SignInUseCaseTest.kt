package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInUseCaseTest {

    private val attendeeRepository = mockk<AttendeeRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = SignInUseCase(attendeeRepository, authRepository)

    private val jane = Attendee(id = "a1", fullName = "Jane Doe", email = "jane@test.com", age = 33, authUid = "uid-123")

    @Test
    fun `when the password is correct and the uid matches authUid, the result is a success`() = runTest {
        coEvery { attendeeRepository.findAttendeeByEmail("jane@test.com") } returns jane
        coEvery { authRepository.signIn("jane@test.com", "sunny-Harbor42") } returns Result.success("uid-123")

        val result = useCase("jane@test.com", "sunny-Harbor42")

        assertTrue(result.isSuccess)
        verify(exactly = 0) { authRepository.signOut() }
    }

    @Test
    fun `when the signed-in uid differs from authUid, the session is dropped and the result is AccountMismatch`() = runTest {
        coEvery { attendeeRepository.findAttendeeByEmail("jane@test.com") } returns jane
        coEvery { authRepository.signIn("jane@test.com", "sunny-Harbor42") } returns Result.success("uid-999")
        justRun { authRepository.signOut() }

        val result = useCase("jane@test.com", "sunny-Harbor42")

        assertTrue(result.exceptionOrNull() is AuthException.AccountMismatch)
        assertEquals(
            "This account isn't linked to your registration. Please contact the organizer.",
            result.exceptionOrNull()?.message
        )
        verify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun `when the password is wrong, the failure is passed through unchanged`() = runTest {
        coEvery { attendeeRepository.findAttendeeByEmail("jane@test.com") } returns jane
        coEvery { authRepository.signIn("jane@test.com", "wrong-password") } returns Result.failure(AuthException.InvalidCredentials())

        val result = useCase("jane@test.com", "wrong-password")

        assertTrue(result.exceptionOrNull() is AuthException.InvalidCredentials)
        assertEquals("Incorrect password. Please try again.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when no attendee has this email, sign-in is not attempted`() = runTest {
        coEvery { attendeeRepository.findAttendeeByEmail("unknown@test.com") } returns null

        val result = useCase("unknown@test.com", "sunny-Harbor42")

        assertEquals("We couldn't find this attendee.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.signIn(any(), any()) }
    }

    @Test
    fun `when the attendee lookup throws, the failure carries that exception and sign-in is not attempted`() = runTest {
        coEvery { attendeeRepository.findAttendeeByEmail("jane@test.com") } throws Exception("Network error")

        val result = useCase("jane@test.com", "sunny-Harbor42")

        assertEquals("Network error", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.signIn(any(), any()) }
    }

    // Backing out of sign-in cancels the coroutine: that must propagate, not come back as a
    // failed Result the ViewModel would then show as a sign-in error.
    @Test
    fun `when the attendee lookup is cancelled, cancellation propagates instead of returning failure`() = runTest {
        coEvery { attendeeRepository.findAttendeeByEmail("jane@test.com") } throws CancellationException("screen left")

        val error = try {
            useCase("jane@test.com", "sunny-Harbor42")
            null
        } catch (e: CancellationException) {
            e
        }

        assertEquals("screen left", error?.message)
        coVerify(exactly = 0) { authRepository.signIn(any(), any()) }
    }
}
