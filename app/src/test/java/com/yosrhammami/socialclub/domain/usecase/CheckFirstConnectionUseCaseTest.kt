package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.FirstConnectionResult
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckFirstConnectionUseCaseTest {
    private val repository = mockk<AttendeeRepository>()
    private val useCase = CheckFirstConnectionUseCase(repository)

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
    fun `when attendee already has an authUid, result is AlreadyActivated with that attendee`() = runTest {
        val activated = jane.copy(authUid = "uid-123")
        coEvery { repository.findAttendeeByEmail("jane@test.com") } returns activated

        assertEquals(FirstConnectionResult.AlreadyActivated(activated), useCase("jane@test.com"))
    }
}
