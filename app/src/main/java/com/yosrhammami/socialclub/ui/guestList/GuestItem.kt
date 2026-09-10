package com.yosrhammami.socialclub.ui.guestList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.semantics.semantics
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.ui.components.AppAvatar
import com.yosrhammami.socialclub.ui.components.BodyText
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews
import com.yosrhammami.socialclub.ui.util.toPlaceholderDrawable

@Composable
fun GuestItem(
    guest: Attendee,
    onGuestClick: (guestAttendeeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onGuestClick(guest.id) }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppAvatar(
            name = guest.fullName,
            photoUrl = guest.photoUrl,
            size = 56.dp
        )

        Column(
            modifier = Modifier
                .padding(start = Spacing.md)
                .weight(1f)
        ) {
            BodyText(text = guest.fullName)
            if (guest.prompt.isNotBlank()) {
                CaptionText(text = guest.prompt)
            }
        }
    }
}

@ThemePreviews
@Composable
fun GuestItemPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            GuestItem(
                guest = Attendee(
                    id = "1",
                    fullName = "Jane Doe",
                    email = "jane@test.com",
                    age=33,
                    photoUrl = "https://randomuser.me/api/portraits/women/44.jpg",
                    prompt = "Android dev, loves hiking",
                    tags = emptyList()
                ),
                onGuestClick = {}
            )
        }
    }
}