package com.yosrhammami.socialclub.ui.peopleList

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
import com.yosrhammami.socialclub.domain.model.Person
import androidx.compose.ui.semantics.semantics
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun PersonListItem(
    person: Person,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholder = when (person.gender) {
        Gender.MALE -> R.drawable.ic_avatar_male
        Gender.FEMALE -> R.drawable.ic_avatar_female
        Gender.UNKNOWN -> R.drawable.ic_avatar_neutral
    }

    Row(modifier = modifier
        .fillMaxWidth()
        .clickable(onClickLabel = "View ${person.fullName}'s profile") {onClick()}
        .padding(
            horizontal = 16.dp,
            vertical = 10.dp
        )
        .semantics(mergeDescendants = true) {},   // <- groups children into one accessible unit,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = person.photoUrl,
            contentDescription =null,
            placeholder = painterResource(placeholder),
            error = painterResource(placeholder),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = person.fullName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                text = "${person.city}, ${person.country}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@ThemePreviews
@Composable
fun PersonListItemPreview() {
    SocialClubTheme {
        Surface {
            PersonListItem(
                person = Person(
                    id = "1",
                    fullName = "Jane Doe",
                    email = "jane@test.com",
                    city = "Paris",
                    country = "France",
                    age = 29,
                    photoUrl = "https://randomuser.me/api/portraits/women/44.jpg",
                    gender = Gender.UNKNOWN
                ),
                onClick = {}
            )
        }
    }
}