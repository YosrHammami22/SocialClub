package com.yosrhammami.socialclub.data.remote

import com.yosrhammami.socialclub.domain.model.Attendee

data class AttendeeDto(
    val fullName: String = "",
    val email: String = "",
    val prompt: String = "",
    val tags: List<String> = emptyList()
)

fun AttendeeDto.toDomain(id: String): Attendee {
    return Attendee(
        id = id,
        fullName = fullName,
        email = email,
        prompt = prompt,
        tags = tags
    )
}