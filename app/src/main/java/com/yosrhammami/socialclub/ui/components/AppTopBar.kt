package com.yosrhammami.socialclub.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

/**
 * Standard top bar: optional back button + a single-line title.
 *
 * [onBackClick] is nullable rather than a separate `showBack` flag: a root screen (e.g. Home)
 * simply passes nothing, and it's impossible to show a back button that does nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    enabled: Boolean=true
) {
    TopAppBar(
        title = {
            // One line + ellipsis: at 200% font scale a long event name would otherwise
            // wrap and push the bar's content out of its fixed height.
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                // IconButton gives the 48dp touch target; the contentDescription is
                // announced by TalkBack as "Back, button".
                IconButton(enabled = enabled, onClick = onBackClick ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            }
        },
        // Same color as the screen's Surface, so the bar blends into the flat design
        // instead of reading as a separate tinted strip.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
    )
}

@ThemePreviews
@Composable
fun PreviewAppTopBarWithBack() {
    SocialClubTheme(dynamicColor = false) {
        AppTopBar(title = "Sunset Rooftop Mixer", onBackClick = {})
    }
}

@ThemePreviews
@Composable
fun PreviewAppTopBarWithoutBack() {
    SocialClubTheme(dynamicColor = false) {
        AppTopBar(title = "Settings")
    }
}
