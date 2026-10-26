package com.yosrhammami.socialclub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun AppAvatar(
    name: String="",
    placeholder: Int = R.drawable.ic_avatar_neutral,
    modifier: Modifier = Modifier,
    photoUrl: String? = null,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    contentDescription: String? = null
) {
    // clickable goes after clip so the ripple is circular, not a square around the avatar.
    // Only applied when onClick is set: a decorative avatar must not be announced as a button.
    val shapeModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (onClick != null) {
                // The image itself stays decorative (contentDescription = null below), so the
                // button needs its own name here, otherwise TalkBack announces just "Button".
                Modifier
                    .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                    .semantics { contentDescription?.let { this.contentDescription = it } }
            } else {
                Modifier
            }
        )

    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            placeholder=painterResource(placeholder),
            contentScale = ContentScale.Crop,
            modifier = shapeModifier
        )
    } else {
        Box(
            modifier = shapeModifier
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            // When a contentDescription names the button, the initial letter is just decoration:
            // hide it so TalkBack reads "Settings, button", not "Settings, J, button".
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                modifier = if (contentDescription != null) Modifier.clearAndSetSemantics {} else Modifier,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
@ThemePreviews
@Composable
fun PreviewAppAvatar(){
    SocialClubTheme {
        AppAvatar(name="Test",placeholder=R.drawable.ic_avatar_neutral)
    }
}

// Interactive mode lets you tap the avatar to see the circular ripple.
// The photo URL won't load in a static preview, so the placeholder shows there.
@ThemePreviews
@Composable
fun PreviewAppAvatarClickable() {
    SocialClubTheme {
        AppAvatar(
            name = "Amira Ben Salah",
            photoUrl = "https://randomuser.me/api/portraits/women/44.jpg",
            size = 64.dp,
            onClick = {},
            onClickLabel = "View profile",
            contentDescription = "Amira Ben Salah"
        )
    }
}