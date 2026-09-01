package com.yosrhammami.socialclub.domain.model

data class Attendee(
    val id: String,           // Firebase Auth UID
    val fullName: String,
    val email: String,
    val age:Int,
    val gender: Gender= Gender.UNKNOWN,
    val prompt: String,
    val tags: List<String>
)
