package com.yosrhammami.socialclub.ui.chat


import com.yosrhammami.socialclub.domain.model.Message
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Domain → UI: resolves isMine and formats the send time for one chat bubble. */
internal fun Message.toUi(
    currentUid: String?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): MessageUi = MessageUi(
    id = id,
    text = text,
    time = Instant.ofEpochMilli(sentAt).atZone(zoneId).format(timeFormatter),
    isMine = senderUid == currentUid,
    isPending = isPending,
)