import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.ui.home.HomeViewModel
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme

@Composable
fun HomeScreen(
    onValidEmail: (String) -> Unit,
    onGetFromApiClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val email by viewModel.email.collectAsStateWithLifecycle()
    val emailError by viewModel.emailError.collectAsStateWithLifecycle()

    // Pass state down to the stateless content
    HomeContent(
        email = email,
        emailError = emailError,
        onEmailChange = viewModel::onEmailChanged,
        onSubmit = {viewModel.onSubmitClick(onValidEmail = onValidEmail)},
        onGetFromApiClick = onGetFromApiClick
    )
}

@Composable
fun HomeContent(
    email: String,
    emailError: String?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGetFromApiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = {Text("Enter your email")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (emailError != null) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Find my registration")
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onGetFromApiClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get from RandomUser API")
        }
    }
}

@Preview(
    showBackground = true,
    name = "Default State"
)
@Composable
fun PreviewHomeContent() {
    SocialClubTheme {
        HomeContent(email = "",
            emailError = null,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}

@Preview(
    showBackground = true,
    name = "Error State"
)
@Composable
fun PreviewHomeContentError() {
    SocialClubTheme {
        HomeContent(email = "invalid-email",
            emailError = "Please enter a valid email address",
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}

@Preview(
    showBackground = true,
    name = "Filled State"
)
@Composable
fun PreviewHomeContentFilled() {
    SocialClubTheme {
        HomeContent(email = "user@example.com",
            emailError = null,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}