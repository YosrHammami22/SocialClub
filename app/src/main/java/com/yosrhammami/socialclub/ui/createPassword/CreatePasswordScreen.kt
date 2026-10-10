package com.yosrhammami.socialclub.ui.createPassword

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.components.AppPrimaryButton
import com.yosrhammami.socialclub.ui.components.AppTextField
import com.yosrhammami.socialclub.ui.components.BodyText
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.components.TitleText
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun CreatePasswordScreen(
    onAccountCreated: (email: String) -> Unit,
    viewModel: CreatePasswordViewModel = hiltViewModel()
) {
    val password by viewModel.password.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Success is terminal for this screen (it gets popped), so unlike Home there is no state to reset.
    val currentOnAccountCreated by rememberUpdatedState(onAccountCreated)
    LaunchedEffect(uiState) {
        if (uiState is CreatePasswordUiState.Success) {
            currentOnAccountCreated(viewModel.email)
        }
    }

    CreatePasswordContent(
        email = viewModel.email,
        password = password,
        confirmPassword = confirmPassword,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        // Keep the spinner through Success so the button can't be tapped during the navigation frame.
        isLoading = uiState is CreatePasswordUiState.Loading || uiState is CreatePasswordUiState.Success,
        errorMessage = (uiState as? CreatePasswordUiState.Error)?.message,
        onPasswordChange = viewModel::onPasswordChanged,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
        onSubmit = viewModel::onSubmitClick
    )
}

@Composable
fun CreatePasswordContent(
    email: String,
    password: String,
    confirmPassword: String,
    passwordError: String?,
    confirmPasswordError: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.Center
    ) {
        TitleText(text = stringResource(R.string.create_password_title))
        Spacer(Modifier.height(Spacing.sm))
        BodyText(
            text = stringResource(
                R.string.create_password_subtitle,
                email
            )
        )

        Spacer(Modifier.height(Spacing.lg))

        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.create_password_label),
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            errorMessage = passwordError,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError == null) {
            CaptionText(
                text = stringResource(R.string.create_password_hint),
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }

        Spacer(Modifier.height(Spacing.md))

        AppTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(R.string.create_password_confirm_label),
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError,
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
            text = stringResource(R.string.create_password_button),
            onClick = onSubmit,
            isLoading = isLoading
        )
    }
}

@ThemePreviews
@Composable
fun PreviewCreatePasswordContent() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CreatePasswordContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor42",
                confirmPassword = "sunny-Harbor42",
                passwordError = null,
                confirmPasswordError = null,
                isLoading = false,
                errorMessage = null,
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewCreatePasswordContentFieldErrors() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CreatePasswordContent(
                email = "jane.doe@example.com",
                password = "sunny",
                confirmPassword = "sunny-Harbor",
                passwordError = "Password must be at least 8 characters",
                confirmPasswordError = "Passwords don't match",
                isLoading = false,
                errorMessage = null,
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewCreatePasswordContentLoading() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CreatePasswordContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor42",
                confirmPassword = "sunny-Harbor42",
                passwordError = null,
                confirmPasswordError = null,
                isLoading = true,
                errorMessage = null,
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewCreatePasswordContentError() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CreatePasswordContent(
                email = "jane.doe@example.com",
                password = "sunny-Harbor42",
                confirmPassword = "sunny-Harbor42",
                passwordError = null,
                confirmPasswordError = null,
                isLoading = false,
                errorMessage = "Network error. Please check your connection and try again.",
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onSubmit = {}
            )
        }
    }
}
