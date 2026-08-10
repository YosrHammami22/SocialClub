package com.yosrhammami.socialclub.ui.theme.preview

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
    fontScale = 1f
)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
    fontScale = 1f
)
@Preview(
    name = "Large Font (200%)",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
    fontScale = 2f
)
@Preview(
    name = "Dark + Large Font",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
    fontScale = 2f
)
annotation class ThemePreviews