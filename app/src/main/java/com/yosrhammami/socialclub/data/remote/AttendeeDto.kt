package com.yosrhammami.socialclub.data.remote

import com.google.firebase.firestore.DocumentId
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.model.Gender

data class AttendeeDto(
    @DocumentId val id: String="",
    val fullName: String = "",
    val email: String = "",
    val gender: String?=null,
    val age: Int = 0,
    val prompt: String = "",
    val tags: List<String> = emptyList()
)

fun AttendeeDto.toDomain(): Attendee {
    return Attendee(
        id = id,
        fullName = fullName,
        email = email,
        prompt = prompt,
        tags = tags,
        age = age,
        gender = when (gender?.lowercase()) {
            "male" -> Gender.MALE
            "female" -> Gender.FEMALE
            else -> Gender.UNKNOWN
        }
    )
}