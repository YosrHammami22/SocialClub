package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import javax.inject.Inject

class SendContactRequestUseCase @Inject constructor(
    private val repository: ContactRequestRepository
) {

    suspend operator fun invoke(
        senderId: String,
        receiverId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Result<Unit> {
        if (senderId == receiverId) {
            return Result.failure(IllegalArgumentException("Cannot send a contact request to yourself"))
        }

        val contactRequest = ContactRequest(
            id = ContactRequest.generateId(senderId, receiverId),
            fromAttendeeId = senderId,
            toAttendeeId = receiverId,
            status = ContactRequestStatus.PENDING,
            createdAt = currentTime
        )

        return repository.send(contactRequest)
    }
}