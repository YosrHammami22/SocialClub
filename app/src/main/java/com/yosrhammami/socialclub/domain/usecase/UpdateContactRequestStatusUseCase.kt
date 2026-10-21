package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.model.ContactRequestException
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import javax.inject.Inject

class UpdateContactRequestStatusUseCase @Inject constructor(
    private val repository: ContactRequestRepository
) {

    suspend operator fun invoke(
        currentAuthUid: String,
        otherAuthUid: String,
        status: ContactRequestStatus
    ): Result<Unit> {
        if (currentAuthUid == otherAuthUid) {
            return Result.failure(IllegalArgumentException("Cannot send a contact request to yourself"))
        }
        if (otherAuthUid.isEmpty() || currentAuthUid.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot send a contact request issues with empty id"))
        }
        // respond() rejects PENDING too; checking here as well just saves a Firestore round trip.
        if (status == ContactRequestStatus.PENDING) {
            return Result.failure(ContactRequestException.InvalidResponse())
        }
        val id = runSuspendCatching { ContactRequest.generateId(currentAuthUid, otherAuthUid) }
            .getOrElse { return Result.failure(it) }

        // Who may answer, and whether it's still PENDING, is checked against the current document
        // inside the transaction — before, the UI's button visibility was the only guard.
        return repository.modifyAtomically(id) { current ->
            current?.respond(currentAuthUid, status)
                ?: Result.failure(ContactRequestException.NotFound())
        }
    }
}
