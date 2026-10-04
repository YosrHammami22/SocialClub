package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.ContactRequestButtonState
import com.yosrhammami.socialclub.domain.model.ContactRequestStatus
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetContactRequestStateUseCase @Inject constructor(
    private val repository: ContactRequestRepository
) {
    operator fun invoke(
        fromAttendeeId: String,
        toAttendeeId: String
    ): Flow<ContactRequestButtonState> {
        return repository.observeContactRequest(fromAttendeeId, toAttendeeId).map { request ->
            when {
                request == null -> ContactRequestButtonState.NoRequest
                request.status == ContactRequestStatus.DECLINED -> ContactRequestButtonState.Declined
                request.status == ContactRequestStatus.ACCEPTED -> ContactRequestButtonState.Accepted
                request.status == ContactRequestStatus.PENDING && request.fromAttendeeId == fromAttendeeId -> ContactRequestButtonState.PendingSender
                else -> ContactRequestButtonState.PendingReceiver
            }
        }
    }
}