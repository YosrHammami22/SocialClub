package com.yosrhammami.socialclub.ui.attendee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yosrhammami.socialclub.domain.model.Event
import com.yosrhammami.socialclub.domain.model.PaymentStatus
import com.yosrhammami.socialclub.domain.model.Registration
import com.yosrhammami.socialclub.domain.model.RegistrationWithEvent
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews
import com.yosrhammami.socialclub.ui.util.formatDate

@Composable
fun RegistrationItem(item: RegistrationWithEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = item.event?.name ?: "Unknown event",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location: ${item.event?.location ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Status Chip/Badge
                StatusBadge(status = item.registration.paymentStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.event?.date?.let { formatDate(it) } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusBadge(status: PaymentStatus) {
    val backgroundColor = when (status) {
        PaymentStatus.PAID -> Color(0xFF4CAF50) // Green
        PaymentStatus.PENDING -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }

    Surface(
        color = backgroundColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@ThemePreviews
@Composable
fun PreviewRegistrationItemPaid() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RegistrationItem(
                item = RegistrationWithEvent(
                    registration = Registration(
                        id = "123",
                        eventId = "WORKSHOP-01",
                        personId = "123",
                        qrCode = "qr_code_123",
                        registeredAt = 1624505600000,
                        paymentStatus = PaymentStatus.PENDING
                    ),
                    event = Event(
                        id = "WORKSHOP-01",
                        name = "name Event",
                        date = 1,
                        location = "location Event"
                    )
                )
            )

        }
    }
}

@ThemePreviews
@Composable
fun PreviewRegistrationItemPending() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RegistrationItem(
                item = RegistrationWithEvent(
                    registration = Registration(
                        id = "123",
                        eventId = "WORKSHOP-01",
                        personId = "123",
                        qrCode = "qr_code_123",
                        registeredAt = 1624505600000,
                        paymentStatus = PaymentStatus.PENDING
                    ),
                    event = Event(
                        id = "WORKSHOP-01",
                        name = "name Event",
                        date = 1,
                        location = "location Event"
                    )
                )
            )
        }
    }
}
