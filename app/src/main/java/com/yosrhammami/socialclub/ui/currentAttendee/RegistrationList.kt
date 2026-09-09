package com.yosrhammami.socialclub.ui.currentAttendee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yosrhammami.socialclub.domain.model.RegistrationWithEvent

@Composable
fun RegistrationList(
    registrations: List<RegistrationWithEvent>,
    onCardClick: (eventId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (registrations.isEmpty()) {
        Text("No registrations found.")
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp), // Padding around the whole list
        verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
    ) {
        items(
            items = registrations,
            key = {it.registration.id}) {registration ->
            RegistrationItem(item = registration, onCardClick = onCardClick)
        }
    }
}