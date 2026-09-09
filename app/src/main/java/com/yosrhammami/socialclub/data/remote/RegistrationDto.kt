package com.yosrhammami.socialclub.data.remote

import com.google.firebase.firestore.DocumentId
import com.yosrhammami.socialclub.domain.model.PaymentStatus
import com.yosrhammami.socialclub.domain.model.Registration

data class RegistrationDto(
    @DocumentId val id: String = "",
    val personId: String = "",
    val eventId: String = "",
    val paymentStatus: String = "",
    val qrCode: String = "",
    val registeredAt: com.google.firebase.Timestamp? = null
)
fun RegistrationDto.toDomain(): Registration {
    return Registration(
        id = id,
        personId = personId,
        eventId = eventId,
        paymentStatus = when (paymentStatus.lowercase()) {
            "paid" -> PaymentStatus.PAID
            "pending" -> PaymentStatus.PENDING
            else -> PaymentStatus.UNKNOWN
        },
        qrCode = qrCode,
        registeredAt = registeredAt?.toDate()?.time ?: 0L
    )
}