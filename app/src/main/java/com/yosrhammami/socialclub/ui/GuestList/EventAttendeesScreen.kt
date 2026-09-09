package com.yosrhammami.socialclub.ui.GuestList

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.Gender

// Stateful — used by MainActivity, connects to the real ViewModel
@Composable
fun EventAttendeesScreen(
    viewModel: EventAttendeesViewModel = hiltViewModel(),
    onGuestClick: (guestAttendeeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        EventAttendeesContent(
            uiState = uiState,
            onGuestClick = onGuestClick,
            modifier = modifier.fillMaxSize()
        )
    }
}

// Stateless — pure UI, takes state as a parameter, no ViewModel/Hilt involved
@Composable
fun EventAttendeesContent(
    uiState: EventAttendeesUiState,
    onGuestClick: (guestAttendeeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = uiState,
        label = "state_transition"
    ) {state ->
        when (state) {
            is EventAttendeesUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EventAttendeesUiState.Success -> {
                if (state.guestAttendees.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No other guests registered yet.")
                    }
                }
            else{

                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(
                            items = state.guestAttendees,
                            key = {it.id}) {guest ->
                            GuestItem(guest, onGuestClick = {onGuestClick(guest.id)})
                            HorizontalDivider()
                        }
                    }
                }
            }

            is EventAttendeesUiState.Error -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: ${state.message}")
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun EventAttendeesLoadingPreview() {
    EventAttendeesContent(uiState = EventAttendeesUiState.Loading,
        onGuestClick = {})
}

@Preview(showBackground = true)
@Composable
fun AttendeesSuccessPreview() {
    val fakePeople = listOf(
        Attendee(
            id = "1",
            fullName = "Jane Doe",
            email = "jane@test.com",
            age = 29,
            prompt = "",
            tags = emptyList(),
            gender = Gender.MALE
        ),
        Attendee(
            id = "2",
            fullName = "jane Smith",
            email = "jack@test.com",
            age = 29,
            prompt = "",
            tags = emptyList(),
            gender = Gender.FEMALE
        )

    )
    EventAttendeesContent(uiState = EventAttendeesUiState.Success(fakePeople),
        onGuestClick = {})
}

@Preview(showBackground = true)
@Composable
fun AttendeeListErrorPreview() {
    EventAttendeesContent(uiState = EventAttendeesUiState.Error(message = "Network error"),
        onGuestClick = {})
}