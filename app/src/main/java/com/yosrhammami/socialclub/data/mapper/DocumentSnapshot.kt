package com.yosrhammami.socialclub.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.yosrhammami.socialclub.data.remote.MessageDto
import com.yosrhammami.socialclub.domain.model.Message

fun DocumentSnapshot.toMessage(): Message? {
    val dto = runCatching {
        toObject(MessageDto::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
    }.getOrNull() ?: return null

    val sentAt = dto.sentAt ?: return null
    if (dto.senderUid.isBlank()) return null

    return Message(
        id = dto.id,
        senderUid = dto.senderUid,
        text = dto.text,
        sentAt = sentAt.toDate().time,
        isPending = metadata.hasPendingWrites(),
    )
}