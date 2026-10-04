package com.yosrhammami.socialclub.ui.guestDetail

import androidx.lifecycle.SavedStateHandle
import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.data.session.SessionManager
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState
import com.yosrhammami.socialclub.domain.usecase.GetAttendeeUseCase
import com.yosrhammami.socialclub.domain.usecase.GetContactRequestStateUseCase
import com.yosrhammami.socialclub.domain.usecase.SendContactRequestUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val sessionManager = mockk<SessionManager>()
    private val logger = FakeLogger()

    private fun createViewModel(attendeeId: String = "guest1"): AttendeeDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("attendeeId" to attendeeId))
        return AttendeeDetailViewModel(
            getAttendeeUseCase, getContactRequestStateUseCase, sendContactRequestUseCase,sessionManager, logger, savedStateHandle
        )
    }

    @Test
    fun `when attendee is found, uiState becomes Success with correct contact state`(): Unit = runTest {
        val guest = Attendee(
            id = "guest1",
            fullName = "Jane",
            email = "jane@test.com",
            photoUrl = "",
            prompt = "",
            tags = emptyList(),
            age = 22
        )
        val me = Attendee(id = "me1", fullName = "Me", email = "me@test.com", photoUrl = "", prompt = "", tags = emptyList(), age = 30)

        coEvery { getAttendeeUseCase.invoke("guest1") } returns guest
        every { sessionManager.currentAttendee } returns MutableStateFlow(me)
        every { getContactRequestStateUseCase(fromAttendeeId = "me1", toAttendeeId = "guest1") } returns flowOf(ContactRequestButtonState.NoRequest)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is AttendeeDetailUiState.Success)
        assertEquals(guest, (state as AttendeeDetailUiState.Success).guestAttendee)
        assertEquals(ContactRequestButtonState.NoRequest, state.contactButtonState)
    }
}