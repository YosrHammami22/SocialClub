package com.yosrhammami.socialclub.ui.personDetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Person
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews
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
    PersonDetailContent(uiState = uiState)
}

@Composable
fun PersonDetailContent(uiState: PersonDetailUiState) {

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
            PersonDetailContentSuccess(person = uiState.person)
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
fun PersonDetailContentSuccess(person: Person) {
    val context = LocalContext.current
    val placeholder = person.gender.toPlaceholderDrawable()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
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

        IconButton(onClick = {
            Toast.makeText(
                context,
                "I want to tag you",
                Toast.LENGTH_SHORT
            )
                .show()
        },
            modifier = Modifier.semantics {
                contentDescription = "Tag ${person.fullName}"
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pisst),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@ThemePreviews
@Composable
fun PersonDetailSuccessPreview() {
    val person = Person(
        id = "id",
        fullName = "Jane Doe",
        email = "jane@test.com",
        city = "Paris",
        country = "France",
        age = 29,
        photoUrl = "https://randomuser.me/api/portraits/women/44.jpg"
    )
    PersonDetailContentSuccess(person)

}
