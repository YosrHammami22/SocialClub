package com.yosrhammami.socialclub.ui.guestList


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yosrhammami.socialclub.ui.components.TitleText
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

@Composable
fun EventHeader(eventName: String="Event Name") {
    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
        TitleText(text = eventName)
    }
}
@ThemePreviews
@Composable
fun PreviewEventHeader(){
    SocialClubTheme {
        EventHeader(eventName = "eventName")
    }
}