import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.components.AppPrimaryButton
import com.yosrhammami.socialclub.ui.components.AppSecondaryButton
import com.yosrhammami.socialclub.ui.components.AppTextField
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.home.FindAttendeeUiState
import com.yosrhammami.socialclub.ui.home.HomeViewModel
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun HomeScreen(
    onValidEmail: (String) -> Unit,
    onFirstConnection: (attendeeId: String, email: String) -> Unit,
    onGetFromApiClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val email by viewModel.email.collectAsStateWithLifecycle()
    val emailError by viewModel.emailError.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /*
    Navigation is driven by state, not by a callback handed to the ViewModel: the lookup is now
    asynchronous, and a lambda captured before a rotation would point at a dead NavController.
    rememberUpdatedState keeps the effect calling the latest lambdas without restarting it.
     */
    val currentOnValidEmail by rememberUpdatedState(onValidEmail)
    val currentOnFirstConnection by rememberUpdatedState(onFirstConnection)
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is FindAttendeeUiState.NeedsPassword -> {
                currentOnFirstConnection(state.attendee.id, state.attendee.email)
                viewModel.onNavigationHandled()
            }
            is FindAttendeeUiState.Success -> {
                currentOnValidEmail(state.attendee.email)
                viewModel.onNavigationHandled()
            }
            else -> Unit
        }
    }

    // Pass state down to the stateless content
    HomeContent(
        email = email,
        emailError = emailError,
        onEmailChange = viewModel::onEmailChanged,
        onSubmit = viewModel::onSubmitClick,
        isLoading = uiState is FindAttendeeUiState.Loading,
        errorMessage = (uiState as? FindAttendeeUiState.Error)?.message,
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
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(modifier = Modifier.height(Spacing.md))

        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.home_email_label),
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError,
            modifier = Modifier.fillMaxWidth()
        )


        if (errorMessage != null) {
            ErrorText(
                text = errorMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
                    .semantics {liveRegion = LiveRegionMode.Polite}
            )
        }

        Spacer(Modifier.height(Spacing.md))

        AppPrimaryButton(
            text = stringResource(R.string.find_registration),
            onClick = onSubmit,
            isLoading = isLoading
        )
        Spacer(Modifier.height(Spacing.md))

        AppSecondaryButton(
            text = stringResource(R.string.get_from_api),
            onClick = onGetFromApiClick
        )
        Spacer(Modifier.height(Spacing.xl))
        AboutScreen()
    }
}

@Composable
fun AboutScreen(context: Context = LocalContext.current) {
    val packageInfo = context.packageManager.getPackageInfo(
        context.packageName,
        0
    )
    val version = packageInfo.versionName ?: ""
    val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    }
    else {
        packageInfo.versionCode.toLong()
    }
    CaptionText(  text = "Version $version ($code)",)
}

@ThemePreviews
@Composable
fun PreviewHomeContent() {
    SocialClubTheme(dynamicColor = false) {
        HomeContent(email = "",
            emailError = null,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {})
    }
}

@ThemePreviews
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

@ThemePreviews
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

@ThemePreviews
@Composable
fun PreviewHomeContentLoading() {
    SocialClubTheme {
        HomeContent(email = "jane.doe@example.com",
            emailError = null,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {},
            isLoading = true)
    }
}

@ThemePreviews
@Composable
fun PreviewHomeContentLookupError() {
    SocialClubTheme {
        HomeContent(email = "jane.doe@example.com",
            emailError = null,
            onEmailChange = {},
            onSubmit = {},
            onGetFromApiClick = {},
            errorMessage = "Network error. Please check your connection and try again.")
    }
}
