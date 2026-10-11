package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.FirstConnectionResult
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckFirstConnectionUseCaseTest {
    private val repository = mockk<AttendeeRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val useCase = CheckFirstConnectionUseCase(repository, authRepository)

    private val jane = Attendee(id = "a1", fullName = "Jane Doe", email = "jane@test.com", age = 33)

    @Test
    fun `when no attendee has this email, result is AttendeeNotFound`() = runTest {
        coEvery { repository.findAttendeeByEmail("unknown@test.com") } returns null

        assertEquals(FirstConnectionResult.AttendeeNotFound, useCase("unknown@test.com"))
    }

    @Test
    fun `when attendee has an empty authUid, result is NeedsPassword with that attendee`() = runTest {
        coEvery { repository.findAttendeeByEmail("jane@test.com") } returns jane

        assertEquals(FirstConnectionResult.NeedsPassword(jane), useCase("jane@test.com"))
    }

    @Test
    fun `when attendee has a whitespace-only authUid, result is still NeedsPassword`() = runTest {
        val blankUid = jane.copy(authUid = "  ")
        coEvery { repository.findAttendeeByEmail("jane@test.com") } returns blankUid

        assertEquals(FirstConnectionResult.NeedsPassword(blankUid), useCase("jane@test.com"))
    }

    @Test
    fun `when the session uid matches the attendee authUid, result is AlreadyActivated`() = runTest {
        val activated = jane.copy(authUid = "uid-123")
        coEvery { repository.findAttendeeByEmail("jane@test.com") } returns activated
        every { authRepository.currentUserUid() } returns "uid-123"

        assertEquals(FirstConnectionResult.AlreadyActivated(activated), useCase("jane@test.com"))
    }

    @Test
    fun `when attendee has an account but the device has no session, result is NeedsSignIn`() = runTest {
        val activated = jane.copy(authUid = "uid-123")
        coEvery { repository.findAttendeeByEmail("jane@test.com") } returns activated
        every { authRepository.currentUserUid() } returns null

        assertEquals(FirstConnectionResult.NeedsSignIn(activated), useCase("jane@test.com"))
    }

    @Test
    fun `when another account is signed in on this device, result is NeedsSignIn`() = runTest {
        val activated = jane.copy(authUid = "uid-123")
        coEvery { repository.findAttendeeByEmail("jane@test.com") } returns activated
        every { authRepository.currentUserUid() } returns "uid-999"

        assertEquals(FirstConnectionResult.NeedsSignIn(activated), useCase("jane@test.com"))
    }
}
