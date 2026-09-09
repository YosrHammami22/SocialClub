package com.yosrhammami.socialclub.domain.model

data class Event(
    val id: String,
    val name: String,
    val date: Long,        // epoch millis, converted from Firestore Timestamp
    val location: String
)
