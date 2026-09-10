package com.yosrhammami.socialclub.ui.guestDetail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.ui.components.AppAvatar
import com.yosrhammami.socialclub.ui.components.BodyText
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.components.LabelText
import com.yosrhammami.socialclub.ui.components.TitleText
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

// Stateful — used by navigation, connects to the real ViewModel
@Composable
fun AttendeeDetailScreen(viewModel: AttendeeDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AttendeeDetailContent(uiState = uiState)
}

// Stateless — pure UI, previewable, no ViewModel/Hilt involved
@Composable
fun AttendeeDetailContent(uiState: AttendeeDetailUiState) {
    Crossfade(targetState = uiState, label = "guest detail") { state ->
        when (state) {
            is AttendeeDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AttendeeDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(Spacing.lg))

                    AppAvatar(
                        name = state.guestAttendee.fullName,
                        photoUrl = state.guestAttendee.photoUrl,
                        size = 120.dp
                    )

                    Spacer(Modifier.height(Spacing.md))

                    TitleText(
                        text = state.guestAttendee.fullName,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(Spacing.xs))

                    CaptionText(text = state.guestAttendee.email)

                    Spacer(Modifier.height(Spacing.lg))

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(Spacing.lg))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        LabelText(text = "About")
                        Spacer(Modifier.height(Spacing.xs))
                        BodyText(text = state.guestAttendee.prompt)

                        if (state.guestAttendee.tags.isNotEmpty()) {
                            Spacer(Modifier.height(Spacing.md))
                            LabelText(text = "Tags")
                            Spacer(Modifier.height(Spacing.xs))
                            TagRow(tags = state.guestAttendee.tags)
                        }
                    }
                }
            }

            is AttendeeDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorText(state.message)
                }
            }

            AttendeeDetailUiState.AttendeeNotFound -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BodyText("Attendee not found")
                }
            }

            AttendeeDetailUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagRow(tags: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                CaptionText(
                    text = tag,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                )
            }
        }
    }
}

// ---------- Previews ----------

@ThemePreviews
@Composable
fun AttendeeDetailSuccessPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AttendeeDetailContent(
                uiState = AttendeeDetailUiState.Success(
                    guestAttendee = Attendee(
                        id = "1",
                        fullName = "Jane Doe",
                        email = "jane@test.com",
                        photoUrl = "https://randomuser.me/api/portraits/women/44.jpg",
                        prompt = "Android developer who loves hiking, board games, and building side projects on weekends.",
                        tags = listOf("android", "hiking", "boardgames", "kotlin"),
                        age=22
                    )
                )
            )
        }
    }
}

@ThemePreviews
@Composable
fun AttendeeDetailNoTagsPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AttendeeDetailContent(
                uiState = AttendeeDetailUiState.Success(
                    guestAttendee = Attendee(
                        id = "2",
                        fullName = "John Smith",
                        email = "john@test.com",
                        photoUrl = "",
                        age=22,
                        prompt = "New here, excited to meet people!",
                        tags = emptyList()
                    )
                )
            )
        }
    }
}

@ThemePreviews
@Composable
fun AttendeeDetailLoadingPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AttendeeDetailContent(uiState = AttendeeDetailUiState.Loading)
        }
    }
}

@ThemePreviews
@Composable
fun AttendeeDetailErrorPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AttendeeDetailContent(uiState = AttendeeDetailUiState.Error("Network error"))
        }
    }
}

@ThemePreviews
@Composable
fun AttendeeDetailNotFoundPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AttendeeDetailContent(uiState = AttendeeDetailUiState.AttendeeNotFound)
        }
    }
}