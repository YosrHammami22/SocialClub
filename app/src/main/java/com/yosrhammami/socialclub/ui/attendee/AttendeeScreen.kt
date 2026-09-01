package com.yosrhammami.socialclub.ui.attendee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun AttendeeScreen(
    email: String,
    viewModel: AttendeeViewModel = hiltViewModel()
) {
    LaunchedEffect(email) {
        viewModel.loadAttendee(email)
    }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is AttendeeUiState.Idle, is AttendeeUiState.Loading -> {
                CircularProgressIndicator()
            }

            is AttendeeUiState.Success -> {
                val attendee = (uiState as AttendeeUiState.Success).attendee
                AttendeeBlock(attendee)
            }

            is AttendeeUiState.Error -> {
                Text("Error: ${(uiState as AttendeeUiState.Error).message}")
            }
        }
    }
}

@Composable
fun AttendeeBlock(attendee: Attendee) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            attendee.fullName,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(attendee.email)
        Text(attendee.prompt)
        Text("Tags: ${attendee.tags.joinToString(", ")}")
    }

}

@ThemePreviews
@Composable
fun AttendeeScreenPreviews() {
    val attendee = Attendee(
        id = "id",
        fullName = "fullname",
        email = "email",
        prompt = "prompt",
        age = 18,
        tags = listOf(
            "tag1",
            "tag2"
        )
    )
    AttendeeBlock(attendee)
}

