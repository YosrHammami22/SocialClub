package com.yosrhammami.socialclub.domain.model

data class Attendee(
    val id: String,           // Firebase Auth UID
    val fullName: String,
    val email: String,
    val prompt: String,
    val tags: List<String>
)
