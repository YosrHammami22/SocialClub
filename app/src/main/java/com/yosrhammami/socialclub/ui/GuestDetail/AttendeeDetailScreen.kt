package com.yosrhammami.socialclub.ui.GuestDetail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AttendeeDetailScreen(viewModel: AttendeeDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(targetState = uiState, label = "guest detail") { state ->
        when (state) {
            is AttendeeDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AttendeeDetailUiState.Success -> {
                Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(state.guestAttendee.fullName, style = MaterialTheme.typography.headlineMedium)
                    Text(state.guestAttendee.email, style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider()
                    Text("Prompt:", style = MaterialTheme.typography.labelLarge)
                    Text(state.guestAttendee.prompt)
                    if (state.guestAttendee.tags.isNotEmpty()) {
                        Text(
                            "Tags: ${state.guestAttendee.tags.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            is AttendeeDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message)
                }
            }

            AttendeeDetailUiState.AttendeeNotFound -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Attendee not found")
                }
            }
            AttendeeDetailUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Problem")
                }
            }
        }
    }
}