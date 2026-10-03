package com.yosrhammami.socialclub.data.remote

import com.google.firebase.firestore.DocumentId
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus

data class ContactRequestDto(
    @DocumentId  val id: String,
    val senderId: String,
    val receiverId: String,
    val status: String,
    val createdAt: Long)

    fun ContactRequestDto.toDomain(): ContactRequest {
        return ContactRequest(
            id = id,
            fromAttendeeId = senderId,
            toAttendeeId = receiverId,
            status = ContactRequestStatus.entries.find { it.name == status } ?: ContactRequestStatus.PENDING,
            createdAt = createdAt
        )
    }
fun ContactRequest.toDto(): ContactRequestDto {
    return ContactRequestDto(
        id = id,
        senderId = fromAttendeeId,
        receiverId = toAttendeeId,
        status = status.name,
        createdAt = createdAt
    )
}

