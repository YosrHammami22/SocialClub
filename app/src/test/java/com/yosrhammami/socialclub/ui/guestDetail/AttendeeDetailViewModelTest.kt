package com.yosrhammami.socialclub.ui.guestDetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.data.session.SessionManager
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeUseCase
import com.yosrhammami.socialclub.domain.usecase.GetContactRequestStateUseCase
import com.yosrhammami.socialclub.domain.usecase.SendContactRequestUseCase
import com.yosrhammami.socialclub.domain.usecase.UpdateContactRequestStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AttendeeDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAttendeeUseCase = mockk<GetAttendeeUseCase>()
    private val getContactRequestStateUseCase = mockk<GetContactRequestStateUseCase>()
    private val sendContactRequestUseCase = mockk<SendContactRequestUseCase>()
    private val updateContactRequestUseCase = mockk<UpdateContactRequestStatusUseCase>()
    private val sessionManager = mockk<SessionManager>()
    private val logger = FakeLogger()

    private val me = Attendee(id = "me1", fullName = "Me", email = "me@test.com", photoUrl = "", prompt = "", tags = emptyList(), age = 30)
    private val guest = Attendee(id = "guest1", fullName = "Jane", email = "jane@test.com", photoUrl = "", prompt = "", tags = emptyList(), age = 22)

    private fun createViewModel(attendeeId: String = "guest1"): AttendeeDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("attendeeId" to attendeeId))
        return AttendeeDetailViewModel(
            getAttendeeUseCase, getContactRequestStateUseCase, sendContactRequestUseCase,
            updateContactRequestUseCase, sessionManager, logger, savedStateHandle
        )
    }

    // ---------- loadAttendeeDetail ----------

    @Test
    fun `when attendee is found, uiState becomes Success with correct contact state`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(fromAttendeeId = "me1", toAttendeeId = "guest1") } returns flowOf(ContactRequestButtonState.NoRequest)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is AttendeeDetailUiState.Success)
        assertEquals(guest, (state as AttendeeDetailUiState.Success).guestAttendee)
        assertEquals(ContactRequestButtonState.NoRequest, state.contactButtonState)
    }

    @Test
    fun `when attendee is not found, uiState becomes Error`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns null

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is AttendeeDetailUiState.Error)
        assertEquals("Attendee not found", (state as AttendeeDetailUiState.Error).message)
    }

    @Test
    fun `when getAttendeeUseCase throws, uiState becomes Error with exception message`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } throws RuntimeException("Network down")

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is AttendeeDetailUiState.Error)
        assertEquals("Network down", (state as AttendeeDetailUiState.Error).message)
    }

    // ---------- onSendRequestClick ----------

    @Test
    fun `onSendRequestClick success does not emit error event`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(fromAttendeeId = "me1", toAttendeeId = "guest1") } returns flowOf(ContactRequestButtonState.NoRequest)
        coEvery { sendContactRequestUseCase("me1", "guest1",any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.onSendRequestClick()

        coVerify { sendContactRequestUseCase("me1", "guest1", any()) }
    }

    @Test
    fun `onSendRequestClick failure emits error event`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(fromAttendeeId = "me1", toAttendeeId = "guest1") } returns flowOf(ContactRequestButtonState.NoRequest)
        coEvery { sendContactRequestUseCase("me1", "guest1", any()) } returns Result.failure(RuntimeException("No internet"))

        val viewModel = createViewModel()

        viewModel.errorEvent.test {
            viewModel.onSendRequestClick()
            assertEquals("No internet connection", awaitItem())
        }
    }

    // ---------- updateContactRequest ----------

    @Test
    fun `updateContactRequest ACCEPTED success calls use case with correct params`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(fromAttendeeId = "me1", toAttendeeId = "guest1") } returns flowOf(ContactRequestButtonState.PendingReceiver)
        coEvery { updateContactRequestUseCase("me1", "guest1", status = ContactRequestStatus.ACCEPTED) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.updateContactRequest(ContactRequestStatus.ACCEPTED)

        coVerify { updateContactRequestUseCase("me1", "guest1", status = ContactRequestStatus.ACCEPTED) }
    }

    @Test
    fun `updateContactRequest DECLINED failure emits error event`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(fromAttendeeId = "me1", toAttendeeId = "guest1") } returns flowOf(ContactRequestButtonState.PendingReceiver)
        coEvery { updateContactRequestUseCase("me1", "guest1", status = ContactRequestStatus.DECLINED) } returns Result.failure(RuntimeException("No internet"))

        val viewModel = createViewModel()

        viewModel.errorEvent.test {
            viewModel.updateContactRequest(ContactRequestStatus.DECLINED)

            assertEquals("No internet connection", awaitItem())
        }

    }
}