package com.yosrhammami.socialclub.domain.model

data class Attendee(
    val id: String,           // Firestore document ID (the attendee's own ID, not the Auth UID)
    val fullName: String,
    val email: String,
    val age:Int,
    val gender: Gender= Gender.UNKNOWN,
    val prompt: String="",
    val photoUrl: String="",
    val tags: List<String> =emptyList(),
    val authUid: String = ""  // Firebase Auth UID; empty = no account created yet (first connection pending)
)
