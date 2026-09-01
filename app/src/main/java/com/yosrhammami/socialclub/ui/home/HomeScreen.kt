import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.ui.home.FindAttendeeUiState
import com.yosrhammami.socialclub.ui.home.HomeViewModel
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun HomeScreen(
    onAttendeeFound: (Attendee) -> Unit,
    onGetFromApiClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 1. Lifecycle-aware state collection
    val email by viewModel.email.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Handle Side Effects
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is FindAttendeeUiState.Success) {
            onAttendeeFound(state.attendee)
            // Optional: viewModel.resetState()
        }
    }

    // 3. Call the Stateless version
    HomeContent(
        email = email,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChanged,
        onSubmit = viewModel::onSubmitClick,
        onGetFromApiClick = onGetFromApiClick
    )
}

@Composable
fun HomeContent(
    email: String,
    uiState: FindAttendeeUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGetFromApiClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = {Text(stringResource(R.string.enter_email))},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = uiState is FindAttendeeUiState.Error,
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Use Smart Casting for Error Message
        if (uiState is FindAttendeeUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 4.dp,
                        start = 16.dp
                    )
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            enabled = uiState !is FindAttendeeUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Fixed height prevents jump
        ) {
            if (uiState is FindAttendeeUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            else {
                Text(stringResource(R.string.find_registration))
            }
        }

        Spacer(Modifier.height(32.dp))

        TextButton( // Using TextButton for secondary actions is more idiomatic
            onClick = onGetFromApiClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.get_from_api))
        }
    }
}

@ThemePreviews()
@Composable
fun PreviewHomeContent() {
    SocialClubTheme {
        HomeContent(email = "user@example.com",
            uiState = FindAttendeeUiState.Idle,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}

@ThemePreviews()
@Composable
fun PreviewHomeLoading() {
    SocialClubTheme {
        HomeContent(email = "user@example.com",
            uiState = FindAttendeeUiState.Loading,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}

@ThemePreviews()
@Composable
fun PreviewHomeError() {
    SocialClubTheme {
        HomeContent(email = "wrong-email",
            uiState = FindAttendeeUiState.Error("Invalid email address found"),
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}