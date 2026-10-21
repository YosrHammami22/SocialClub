package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.domain.model.ContactRequest
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
        // generateId() validates the uids with require(): turn that into a failed Result, so this
        // use case never throws for bad input.
        val id = runSuspendCatching { ContactRequest.generateId(senderId, receiverId) }
            .getOrElse { return Result.failure(it) }

        // The existing request is checked inside the transaction, not read beforehand: a plain
        // write here used to overwrite whatever was there (a DECLINED request, or the other
        // person's own pending request sent at the same moment).
        return repository.modifyAtomically(id) { existing ->
            ContactRequest.send(existing, senderId, receiverId, currentTime)
        }
    }
}
