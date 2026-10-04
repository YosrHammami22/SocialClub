package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import javax.inject.Inject

class UpdateContactRequestStatusUseCase @Inject constructor(
    private val repository: ContactRequestRepository
) {

    suspend operator fun invoke(
        fromAttendeeId: String,
        toAttendeeId: String,
        status: ContactRequestStatus
    ): Result<Unit> {
        if (fromAttendeeId == toAttendeeId) {
            return Result.failure(IllegalArgumentException("Cannot send a contact request to yourself"))
        }
        if (toAttendeeId.isEmpty() || fromAttendeeId.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot send a contact request issues with empty id"))
        }
        if (status == ContactRequestStatus.PENDING) {
            return Result.failure(IllegalArgumentException("Cannot update to PENDING"))
        }
        if (status == ContactRequestStatus.ACCEPTED || status == ContactRequestStatus.DECLINED ) {
            return repository.updateContactRequest(
                fromAttendeeId,
                toAttendeeId,
                status
            )
        }
        return   Result.failure(IllegalArgumentException("Cannot send a contact request Call the administrator "))
        }
    }






