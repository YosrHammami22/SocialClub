package com.yosrhammami.socialclub.ui.personDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun PersonDetailScreen(
    personId: String,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    /*
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
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.person.fullName,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(text = "${uiState.person.city}, ${uiState.person.country}")
                Text(text = "Age: ${uiState.person.age}")
                Text(text = uiState.person.email)
            }
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