package com.yosrhammami.socialclub.ui.attendee


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
    onPersonClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EventAttendeesContent(
        uiState = uiState,
        onPersonClick = onPersonClick
    )
}

// Stateless — pure UI, takes state as a parameter, no ViewModel/Hilt involved
@Composable
fun EventAttendeesContent(
    uiState: EventAttendeesUiState,
    onPersonClick: (String) -> Unit
) {
    when (uiState) {
        is EventAttendeesUiState.Loading -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventAttendeesUiState.Success -> {
            LazyColumn {
                items(uiState.attendees) {attendee ->
                    AttendeeListItem(attendee = attendee,
                        onClick = {onPersonClick(attendee.id)})
                    HorizontalDivider(modifier = Modifier.padding(start = 84.dp))

                }
            }
        }

        is EventAttendeesUiState.Error -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${uiState.message}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventAttendeesLoadingPreview() {
    EventAttendeesContent(
        uiState = EventAttendeesUiState.Loading,
        onPersonClick = {})
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
            gender = Gender.UNKNOWN
        ),
        Attendee(
            id = "2",
            fullName = "jack Doe",
            email = "jack@test.com",
            age = 29,
            prompt = "",
            tags = emptyList(),
            gender = Gender.UNKNOWN
        )

    )
    EventAttendeesContent(
        uiState = EventAttendeesUiState.Success(fakePeople),
        onPersonClick = {})
}

@Preview(showBackground = true)
@Composable
fun AttendeeListErrorPreview() {
    EventAttendeesContent(
        uiState = EventAttendeesUiState.Error(message = "Network error"),
        onPersonClick = {})
}