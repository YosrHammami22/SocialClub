package com.yosrhammami.socialclub.ui.currentAttendee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.ui.components.AppAvatar
import com.yosrhammami.socialclub.ui.components.BodyText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.components.TitleText
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews
import com.yosrhammami.socialclub.ui.util.toPlaceholderDrawable

@Composable
fun AttendeeScreen(
    viewModel: AttendeeViewModel = hiltViewModel(),
    onEventClick: (eventId: String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .padding(top = Spacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is AttendeeUiState.Idle, is AttendeeUiState.Loading -> {
                CircularProgressIndicator()
            }

            is AttendeeUiState.Success -> {
                Column(
                    Modifier.fillMaxSize()
                ) {
                    AttendeeHeader(
                        state.attendee,
                        onSettingsClick = onSettingsClick
                    )
                    RegistrationList(
                        state.registrations,
                        onCardClick = onEventClick
                    )
                }
            }

            is AttendeeUiState.AttendeeNotFound -> {
                BodyText(stringResource(R.string.attendee_not_found))
            }

            is AttendeeUiState.Error -> {
                ErrorText("Error: ${state.message}")
            }
        }
    }
}

@Composable
fun AttendeeHeader(
    attendee: Attendee,
    onSettingsClick: () -> Unit
) {
    val placeholder = attendee.gender.toPlaceholderDrawable()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleText(text = "Hello, ${attendee.fullName.substringBefore(" ")}")
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Entry point to Settings (and so to Logout). The avatar is the button, so it carries
            // the "Settings" name: TalkBack announces "Settings, button" with a photo or initials.
            AppAvatar(
                name = attendee.fullName,
                placeholder = placeholder,
                onClick = onSettingsClick,
                contentDescription = stringResource(R.string.settings_open)
            )
        }
    }
}

@ThemePreviews
@Composable
fun AttendeeScreenPreviews() {
    val attendee = Attendee(
        id = "id",
        fullName = "Jane ",
        email = "email",
        age = 29,
        prompt = "prompt",
        tags = listOf(
            "tag1",
            "tag2"
        )
    )
    AttendeeHeader(
        attendee,
        onSettingsClick = {})
}