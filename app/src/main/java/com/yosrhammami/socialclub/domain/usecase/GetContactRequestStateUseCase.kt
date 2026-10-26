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
        currentAuthUid: String,
        otherAuthUid: String
    ): Flow<ContactRequestButtonState> {
        return repository.observeContactRequest(currentAuthUid, otherAuthUid).map { request ->
            val iAmSender = request?.senderAuthUid == currentAuthUid
            val iAmReceiver = request?.receiverAuthUid == currentAuthUid
            when {
                request == null -> ContactRequestButtonState.NoRequest
                // Neither party matches: corrupt document. Show nothing rather than any action —
                // the old `else` fallback offered Confirm/Delete here.
                !iAmSender && !iAmReceiver -> ContactRequestButtonState.Unknown
                request.status == ContactRequestStatus.ACCEPTED -> ContactRequestButtonState.Accepted
                // Mirrors ContactRequest.send(): the person who declined may send a new request.
                request.status == ContactRequestStatus.DECLINED && iAmReceiver -> ContactRequestButtonState.NoRequest
                request.status == ContactRequestStatus.DECLINED -> ContactRequestButtonState.Declined
                iAmSender -> ContactRequestButtonState.PendingSender
                else -> ContactRequestButtonState.PendingReceiver
            }
        }
    }
}
