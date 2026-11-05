package com.yosrhammami.socialclub.domain.model

data class Message(
    val id:String,
    val senderUid: String ,
    val text: String ,
    val sentAt: Long,
)

