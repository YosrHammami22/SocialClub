package com.yosrhammami.socialclub.ui.currentAttendee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.RegistrationWithEvent
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.theme.Spacing

@Composable
fun RegistrationList(
    registrations: List<RegistrationWithEvent>,
    onCardClick: (eventId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (registrations.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CaptionText(stringResource(R.string.no_registrations_found))
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md), // Padding around the whole list
        verticalArrangement = Arrangement.spacedBy(Spacing.md) // Space between items
    ) {
        items(items = registrations,
            key = {it.registration.id}) {registration ->
            RegistrationItem(
                item = registration,
                onCardClick = onCardClick
            )
        }
    }
}