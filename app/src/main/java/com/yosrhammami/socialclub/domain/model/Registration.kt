package com.yosrhammami.socialclub.domain.model

data class Registration(
    val id: String,
    val personId: String,
    val eventId: String,
    val paymentStatus: PaymentStatus,
    val qrCode: String,
    val registeredAt: Long
)
enum class PaymentStatus { PENDING, PAID, UNKNOWN }