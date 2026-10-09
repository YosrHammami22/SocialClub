package com.yosrhammami.socialclub.ui.personDetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.ui.personDetail.uiState.IcebreakerUiState
import com.yosrhammami.socialclub.ui.personDetail.uiState.PersonDetailUiState
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.util.toPlaceholderDrawable

@Composable
fun PersonDetailScreen(
    personId: String,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {/*
    What LaunchedEffect(key) actually does: it runs its block once when the Composable first enters the screen, and then only runs again if the key changes
     */
    LaunchedEffect(personId) {
        viewModel.loadPerson(personId)              // <- you trigger loading explicitly
    }
    val uiState by viewModel.uiState.collectAsState()
    val icebreakerState by viewModel.icebreakerState.collectAsState()

    PersonDetailContent(
        uiState = uiState,
        icebreakerState = icebreakerState,
        onGenerateIcebreakerClick = {
            viewModel.onGenerateIcebreakerClick()
        }
    )
}

@Composable
fun PersonDetailContent(
    uiState: PersonDetailUiState,
    icebreakerState: IcebreakerUiState,
    onGenerateIcebreakerClick: () -> Unit
) {

    when (uiState) {
        is PersonDetailUiState.Loading -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PersonDetailUiState.Success -> {
            PersonDetailContentSuccess(
                person = uiState.person,
                icebreakerState = icebreakerState,
                onGenerateIcebreakerClick=onGenerateIcebreakerClick
            )
        }

        is PersonDetailUiState.Error -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${uiState.message}")
            }
        }
    }
}

@Composable
fun PersonDetailContentSuccess(
    person: Person,
    icebreakerState: IcebreakerUiState,
    onGenerateIcebreakerClick: () -> Unit
) {
    val context = LocalContext.current
    val placeholder = person.gender.toPlaceholderDrawable()
    // Top-anchored, not vertically centered: with Arrangement.Center the whole block is
    // re-centered every time the icebreaker result changes the content height, which made
    // the avatar jump. Anchoring from the top keeps it fixed and lets the result grow downward.
    // verticalScroll goes before padding so the padding scrolls with the content.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top

    ) {
        Spacer(Modifier.height(Spacing.xxl))

        AsyncImage(
            model = person.photoUrl,
            contentDescription = null,
            placeholder = painterResource(placeholder),
            error = painterResource(placeholder),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = person.fullName,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "${person.age} · ${person.city}, ${person.country}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        // Column, not Row: IcebreakerContent emits its button and then a spinner/text/error
        // as siblings, so the parent's axis decides where the result lands. In a Row it sat
        // beside the button and its height-based Spacers did nothing.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = {
                Toast.makeText(
                    context,
                    "I want to tag you",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pisst),
                    contentDescription = "I want to tag you",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Add spacing between the buttons
            Spacer(modifier = Modifier.height(Spacing.sm))
            IcebreakerContent(uiState = icebreakerState, onGenerateIcebreakerClick = onGenerateIcebreakerClick)
        }
    }
}

@Composable
fun IcebreakerContent(
    uiState: IcebreakerUiState,
    onGenerateIcebreakerClick: () -> Unit = {}
) {

    Button(onClick = {onGenerateIcebreakerClick()}) {
        Text("Generate icebreaker ✨")
    }
    when (uiState) {
        is IcebreakerUiState.Idle -> { /* nothing shown */
        }

        is IcebreakerUiState.Loading -> {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        is IcebreakerUiState.Success -> {
            Spacer(Modifier.height(12.dp))
            Text(
                text = uiState.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        is IcebreakerUiState.Error -> {
            Spacer(Modifier.height(12.dp))
            Text(
                "Couldn't generate an icebreaker, try again",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ---------- Previews ----------

// One shared sample so every preview shows the same person and only the state under test varies.
private val previewPerson = Person(
    id = "1",
    fullName = "Jane Doe",
    email = "jane@test.com",
    city = "Paris",
    country = "France",
    age = 29,
    photoUrl = "https://randomuser.me/api/portraits/women/44.jpg",
    gender = Gender.FEMALE
)

@ThemePreviews
@Composable
fun PersonDetailSuccessPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PersonDetailContent(
                uiState = PersonDetailUiState.Success(previewPerson),
                icebreakerState = IcebreakerUiState.Idle,
                onGenerateIcebreakerClick = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PersonDetailIcebreakerLoadingPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PersonDetailContent(
                uiState = PersonDetailUiState.Success(previewPerson),
                icebreakerState = IcebreakerUiState.Loading,
                onGenerateIcebreakerClick = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PersonDetailIcebreakerSuccessPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PersonDetailContent(
                uiState = PersonDetailUiState.Success(previewPerson),
                icebreakerState = IcebreakerUiState.Success(
                    "Ask Jane which Paris neighbourhood she'd send a first-time visitor to, and why."
                ),
                onGenerateIcebreakerClick = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PersonDetailIcebreakerErrorPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PersonDetailContent(
                uiState = PersonDetailUiState.Success(previewPerson),
                icebreakerState = IcebreakerUiState.Error("Network error"),
                onGenerateIcebreakerClick = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PersonDetailLoadingPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PersonDetailContent(
                uiState = PersonDetailUiState.Loading,
                icebreakerState = IcebreakerUiState.Idle,
                onGenerateIcebreakerClick = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PersonDetailErrorPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PersonDetailContent(
                uiState = PersonDetailUiState.Error("Network error"),
                icebreakerState = IcebreakerUiState.Idle,
                onGenerateIcebreakerClick = {}
            )
        }
    }
}
