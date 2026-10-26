package com.yosrhammami.socialclub.ui.signIn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.components.AppPrimaryButton
import com.yosrhammami.socialclub.ui.components.AppTextField
import com.yosrhammami.socialclub.ui.components.BodyText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.components.TitleText
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun SignInScreen(
    onSignedIn: (email: String) -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val password by viewModel.password.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Success is terminal for this screen (it gets popped), so there is no state to reset.
    val currentOnSignedIn by rememberUpdatedState(onSignedIn)
    LaunchedEffect(uiState) {
        if (uiState is SignInUiState.Success) {
            currentOnSignedIn(viewModel.email)
        }
    }

    SignInContent(
        email = viewModel.email,
        password = password,
        passwordError = passwordError,
        // Keep the spinner through Success so the button can't be tapped during the navigation frame.
        isLoading = uiState is SignInUiState.Loading || uiState is SignInUiState.Success,
        errorMessage = (uiState as? SignInUiState.Error)?.message,
        onPasswordChange = viewModel::onPasswordChanged,
        onSubmit = viewModel::onSubmitClick
    )
}

@Composable
fun SignInContent(
    email: String,
    password: String,
    passwordError: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.Center
    ) {
        TitleText(text = stringResource(R.string.sign_in_title))
        Spacer(Modifier.height(Spacing.sm))
        BodyText(
            text = stringResource(
                R.string.sign_in_subtitle,
                email
            )
        )

        Spacer(Modifier.height(Spacing.lg))

        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.sign_in_password_label),
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            errorMessage = passwordError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(Spacing.lg))

        if (errorMessage != null) {
            // liveRegion: the failure appears asynchronously, away from focus, so TalkBack
            // must announce it on its own instead of waiting for the user to find it.
            ErrorText(
                text = errorMessage,
                modifier = Modifier
                    .padding(bottom = Spacing.md)
                    .semantics {liveRegion = LiveRegionMode.Polite}
            )
        }

        AppPrimaryButton(
            text = stringResource(R.string.sign_in_button),
            onClick = onSubmit,
            isLoading = isLoading
        )
    }
}

@ThemePreviews
@Composable
fun PreviewSignInContent() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SignInContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor42",
                passwordError = null,
                isLoading = false,
                errorMessage = null,
                onPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewSignInContentWrongPassword() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SignInContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor41",
                passwordError = "Incorrect password. Please try again.",
                isLoading = false,
                errorMessage = null,
                onPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewSignInContentLoading() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SignInContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor42",
                passwordError = null,
                isLoading = true,
                errorMessage = null,
                onPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewSignInContentError() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SignInContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor42",
                passwordError = null,
                isLoading = false,
                errorMessage = "Network error. Please check your connection and try again.",
                onPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}
