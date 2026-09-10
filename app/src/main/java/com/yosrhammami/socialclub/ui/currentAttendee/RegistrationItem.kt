package com.yosrhammami.socialclub.ui.currentAttendee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.domain.model.Event
import com.yosrhammami.socialclub.domain.model.PaymentStatus
import com.yosrhammami.socialclub.domain.model.Registration
import com.yosrhammami.socialclub.domain.model.RegistrationWithEvent
import com.yosrhammami.socialclub.ui.components.AppCard
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.components.LabelText
import com.yosrhammami.socialclub.ui.components.SubtitleText
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews
import com.yosrhammami.socialclub.ui.util.formatDate

@Composable
fun RegistrationItem(
    item: RegistrationWithEvent,
    onCardClick: (eventId: String) -> Unit
) {
    AppCard(
        modifier = Modifier.clickable { onCardClick(item.registration.eventId) }
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                SubtitleText(
                    text = item.event?.name ?: stringResource(R.string.unknown_event),
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = item.registration.paymentStatus)
            }

            Spacer(modifier = Modifier.height(Spacing.xs))
            LabelText( text = item.event?.location ?: stringResource(R.string.unknown_location),)

            Spacer(modifier = Modifier.height(Spacing.xs))
            CaptionText( text = item.event?.date?.let { formatDate(it) } ?: "",)
        }
    }
}

@Composable
fun StatusBadge(status: PaymentStatus) {
    val (containerColor, contentColor) = when (status) {
        PaymentStatus.PAID -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        PaymentStatus.PENDING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        PaymentStatus.UNKNOWN -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(50)   // fully rounded pill shape
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@ThemePreviews
@Composable
fun PreviewRegistrationItemPaid() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                RegistrationItem(
                    item = RegistrationWithEvent(
                        registration = Registration(
                            id = "123", eventId = "WORKSHOP-01", personId = "123",
                            qrCode = "qr_code_123", registeredAt = 1624505600000,
                            paymentStatus = PaymentStatus.PAID
                        ),
                        event = Event(id = "WORKSHOP-01", name = "Android Meetup", date = 1, location = "Paris")
                    ),
                    onCardClick = {}
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun PreviewRegistrationItemPending() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                RegistrationItem(
                    item = RegistrationWithEvent(
                        registration = Registration(
                            id = "124", eventId = "WORKSHOP-02", personId = "123",
                            qrCode = "qr_code_124", registeredAt = 1624505600000,
                            paymentStatus = PaymentStatus.PENDING
                        ),
                        event = Event(id = "WORKSHOP-02", name = "Design Systems Talk", date = 1, location = "Lyon")
                    ),
                    onCardClick = {}
                )
            }
        }
    }
}