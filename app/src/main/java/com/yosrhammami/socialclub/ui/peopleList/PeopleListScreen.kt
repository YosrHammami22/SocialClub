package com.yosrhammami.socialclub.ui.peopleList


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.domain.model.Gender
import com.yosrhammami.socialclub.domain.model.Person

// Stateful — used by MainActivity, connects to the real ViewModel
@Composable
fun PeopleListScreen(
    viewModel: PeopleListViewModel = hiltViewModel(),
    onPersonClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PeopleListContent(
        uiState = uiState,
        onPersonClick = onPersonClick
    )
}

// Stateless — pure UI, takes state as a parameter, no ViewModel/Hilt involved
@Composable
fun PeopleListContent(
    uiState: PeopleListUiState,
    onPersonClick: (String) -> Unit
) {
    when (uiState) {
        is PeopleListUiState.Loading -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is PeopleListUiState.Success -> {
            LazyColumn {
                items(uiState.people) {person ->
                    PersonListItem(person = person,
                        onClick = {onPersonClick(person.id)})
                    HorizontalDivider(modifier = Modifier.padding(start = 84.dp))

                }
            }
        }

        is PeopleListUiState.Error -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${uiState.message}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PeopleListLoadingPreview() {
    PeopleListContent(
        uiState = PeopleListUiState.Loading,
        onPersonClick = {})
}

@Preview(showBackground = true)
@Composable
fun PeopleListSuccessPreview() {
    val fakePeople = listOf(
        Person(
            id = "1",
            fullName = "Jane Doe",
            email = "jane@test.com",
            city = "Paris",
            country = "France",
            age = 29,
            photoUrl = "",
            gender = Gender.UNKNOWN

        ),
        Person(
            id = "2",
            fullName = "John Smith",
            email = "john@test.com",
            city = "Lyon",
            country = "France",
            age = 34,
            photoUrl = "",
            gender = Gender.UNKNOWN
        )
    )
    PeopleListContent(
        uiState = PeopleListUiState.Success(fakePeople),
        onPersonClick = {})
}

@Preview(showBackground = true)
@Composable
fun PeopleListErrorPreview() {
    PeopleListContent(
        uiState = PeopleListUiState.Error(message = "Network error"),
        onPersonClick = {})
}