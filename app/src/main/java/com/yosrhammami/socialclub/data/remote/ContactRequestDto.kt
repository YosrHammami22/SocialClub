package com.yosrhammami.socialclub.data.remote

import com.google.firebase.firestore.DocumentId
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus

// Property names are the Firestore field names: toObject()/set() map by name.
data class ContactRequestDto(
    @DocumentId val id: String = "",
    val senderAuthUid: String = "",
    val receiverAuthUid: String = "",
    val status: String = "",
    val createdAt: Long = 0L)

fun ContactRequestDto.toDomain(): ContactRequest {
    return ContactRequest(
        id = id,
        senderAuthUid = senderAuthUid,
        receiverAuthUid = receiverAuthUid,
        // Unknown/missing status = corrupt document. Falling back to PENDING (or returning null)
        // would make it look like a real request (or no request) and hide the bad data.
        status = ContactRequestStatus.entries.find { it.name == status }
            ?: throw IllegalArgumentException("Unknown ContactRequest status: '$status'"),
        createdAt = createdAt
    )
}

fun ContactRequest.toDto(): ContactRequestDto {
    return ContactRequestDto(
        id = id,
        senderAuthUid = senderAuthUid,
        receiverAuthUid = receiverAuthUid,
        status = status.name,
        createdAt = createdAt
    )
}
