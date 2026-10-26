package com.yosrhammami.socialclub.ui.guestList

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.ui.components.AppTopBar
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

// Stateful — used by MainActivity, connects to the real ViewModel
@Composable
fun EventAttendeesScreen(
    viewModel: EventAttendeesViewModel = hiltViewModel(),
    onGuestClick: (guestAttendeeId: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        EventAttendeesContent(
            uiState = uiState,
            onGuestClick = onGuestClick,
            onBackClick = onBackClick,
            modifier = modifier.fillMaxSize()
        )

}

// Stateless — pure UI, takes state as a parameter, no ViewModel/Hilt involved
@Composable
fun EventAttendeesContent(
    uiState: EventAttendeesUiState,
    onGuestClick: (guestAttendeeId: String) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    Column(modifier.fillMaxSize()) {
    // Outside the Crossfade so the bar stays still while only the content below fades.
    // The event name is only known once loaded; until then (or on error) a generic title.
    AppTopBar(
        title = (uiState as? EventAttendeesUiState.Success)?.eventName
            ?: stringResource(R.string.event_attendees_title),
        onBackClick = onBackClick
    )
    Crossfade(
        targetState = uiState,
        modifier = Modifier.weight(1f),
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
                Column(Modifier.fillMaxSize()) {
                    // No separate header here: the event name is the top bar's title.
                    HorizontalDivider()
                    if (state.guestAttendees.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CaptionText("No other guests registered yet.")
                        }
                    }
                    else {

                        LazyColumn(
                            Modifier.fillMaxSize()

                        ) {
                            items(items = state.guestAttendees,
                                key = {it.id}) {guest ->
                                GuestItem(
                                    guest,
                                    onGuestClick = {onGuestClick(guest.id)})
                                HorizontalDivider(modifier = Modifier.padding(start = 84.dp))
                            }
                        }
                    }
                }
            }

            is EventAttendeesUiState.Error -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorText(text = "Error: ${state.message}")
                }
            }
        }
    }
    }

}

// Shared by the Success previews: realistic names + real photo URLs, plus one guest without
// a photo so AppAvatar's initial-letter fallback is covered too.
private val previewGuests = listOf(
    Attendee(
        id = "1",
        fullName = "Amira Ben Salah",
        email = "amira.bensalah@example.com",
        age = 29,
        gender = Gender.FEMALE,
        photoUrl = "https://randomuser.me/api/portraits/women/44.jpg"
    ),
    Attendee(
        id = "2",
        fullName = "Lucas Moreau",
        email = "lucas.moreau@example.com",
        age = 32,
        gender = Gender.MALE,
        photoUrl = "https://randomuser.me/api/portraits/men/32.jpg"
    ),
    Attendee(
        id = "3",
        fullName = "Sofia Rinaldi",
        email = "sofia.rinaldi@example.com",
        age = 27,
        gender = Gender.FEMALE
    )
)

@Composable
private fun EventAttendeesPreviewContainer(uiState: EventAttendeesUiState) {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            EventAttendeesContent(uiState = uiState, onGuestClick = {}, onBackClick = {})
        }
    }
}

@ThemePreviews
@Composable
fun PreviewEventAttendeesLoading() {
    EventAttendeesPreviewContainer(EventAttendeesUiState.Loading)
}

@ThemePreviews
@Composable
fun PreviewEventAttendeesSuccess() {
    EventAttendeesPreviewContainer(EventAttendeesUiState.Success("Sunset Rooftop Mixer", previewGuests))
}

@ThemePreviews
@Composable
fun PreviewEventAttendeesEmpty() {
    EventAttendeesPreviewContainer(EventAttendeesUiState.Success("Sunset Rooftop Mixer", emptyList()))
}

@ThemePreviews
@Composable
fun PreviewEventAttendeesError() {
    EventAttendeesPreviewContainer(EventAttendeesUiState.Error(message = "Network error"))
}
