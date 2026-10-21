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
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

    private val me = Attendee(id = "me1", fullName = "Me", email = "me@test.com", photoUrl = "", prompt = "", tags = emptyList(), age = 30, authUid = "uidMe")
    private val guest = Attendee(id = "guest1", fullName = "Jane", email = "jane@test.com", photoUrl = "", prompt = "", tags = emptyList(), age = 22, authUid = "uidGuest")

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
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flowOf(ContactRequestButtonState.NoRequest)

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

    // Cancellation (e.g. leaving the screen) must not be reported as a load failure.
    @Test
    fun `when loading is cancelled, uiState stays Loading instead of Error`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } throws CancellationException("screen left")

        val viewModel = createViewModel()

        assertEquals(AttendeeDetailUiState.Loading, viewModel.uiState.value)
    }

    // Without an account the guest has no auth uid to key a request on; observing would
    // make generateId() throw inside viewModelScope, so we show GuestHasNoAccount instead.
    @Test
    fun `when guest has no auth account, button state is GuestHasNoAccount and nothing is observed`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest.copy(authUid = "")
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value as AttendeeDetailUiState.Success
        assertEquals(ContactRequestButtonState.GuestHasNoAccount, state.contactButtonState)
        verify(exactly = 0) { getContactRequestStateUseCase(any(), any()) }
    }

    // The upstream fails only once released inside test {}: errorEvent has no replay, so an
    // error emitted during init (before Turbine subscribes) would be dropped and never seen.
    @Test
    fun `when observing contact request fails, error event is emitted and Success is kept`(): Unit = runTest {
        val failure = CompletableDeferred<ContactRequestButtonState>()
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flow { emit(failure.await()) }

        val viewModel = createViewModel()

        viewModel.errorEvent.test {
            failure.completeExceptionally(IllegalArgumentException("Unknown ContactRequest status: 'BLOCKED'"))

            assertEquals("Error loading contact request Unknown ContactRequest status: 'BLOCKED'", awaitItem())
        }
        val state = viewModel.uiState.value as AttendeeDetailUiState.Success
        assertEquals(ContactRequestButtonState.Unknown, state.contactButtonState)
    }

    // ---------- onSendRequestClick ----------

    @Test
    fun `onSendRequestClick success does not emit error event`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flowOf(ContactRequestButtonState.NoRequest)
        coEvery { sendContactRequestUseCase("uidMe", "uidGuest",any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.onSendRequestClick()

        coVerify { sendContactRequestUseCase("uidMe", "uidGuest", any()) }
    }

    @Test
    fun `onSendRequestClick failure emits error event`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flowOf(ContactRequestButtonState.NoRequest)
        coEvery { sendContactRequestUseCase("uidMe", "uidGuest", any()) } returns Result.failure(RuntimeException("No internet"))

        val viewModel = createViewModel()

        viewModel.errorEvent.test {
            viewModel.onSendRequestClick()
            assertEquals("Error sending contact request No internet", awaitItem())
        }
    }

    @Test
    fun `onSendRequestClick emits error event when use case throws instead of returning failure`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flowOf(ContactRequestButtonState.NoRequest)
        coEvery { sendContactRequestUseCase("uidMe", "uidGuest", any()) } throws IllegalArgumentException("uid must not contain '_'")

        val viewModel = createViewModel()

        viewModel.errorEvent.test {
            viewModel.onSendRequestClick()
            assertEquals("Error sending contact request uid must not contain '_'", awaitItem())
        }
    }

    // ---------- updateContactRequest ----------

    @Test
    fun `updateContactRequest ACCEPTED success calls use case with correct params`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flowOf(ContactRequestButtonState.PendingReceiver)
        coEvery { updateContactRequestUseCase("uidMe", "uidGuest", status = ContactRequestStatus.ACCEPTED) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.updateContactRequest(ContactRequestStatus.ACCEPTED)

        coVerify { updateContactRequestUseCase("uidMe", "uidGuest", status = ContactRequestStatus.ACCEPTED) }
    }

    @Test
    fun `updateContactRequest DECLINED failure emits error event`(): Unit = runTest {
        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(currentAuthUid = "uidMe", otherAuthUid = "uidGuest") } returns flowOf(ContactRequestButtonState.PendingReceiver)
        coEvery { updateContactRequestUseCase("uidMe", "uidGuest", status = ContactRequestStatus.DECLINED) } returns Result.failure(RuntimeException("No internet"))

        val viewModel = createViewModel()

        viewModel.errorEvent.test {
            viewModel.updateContactRequest(ContactRequestStatus.DECLINED)

            assertEquals("Error update contact request No internet", awaitItem())
        }

    }
}