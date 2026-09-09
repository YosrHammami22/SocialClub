package com.yosrhammami.socialclub.data.remote

import com.google.firebase.firestore.DocumentId
import com.yosrhammami.socialclub.domain.model.Event

data class EventDto(
    @DocumentId val id: String="",
    val name: String = "",
    val date: com.google.firebase.Timestamp? = null,
    val location: String = ""
)
fun EventDto.toDomain(id: String): Event {
    return Event(
        id = id,
        name = name,
        date = date?.toDate()?.time ?: 0L,
        location = location
    )
}
