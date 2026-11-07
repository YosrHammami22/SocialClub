package com.yosrhammami.socialclub.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp


data class MessageDto(
    @DocumentId  val id: String="",
    val senderUid: String="",
    val text: String="",
    @ServerTimestamp val sentAt: Timestamp? = null)

