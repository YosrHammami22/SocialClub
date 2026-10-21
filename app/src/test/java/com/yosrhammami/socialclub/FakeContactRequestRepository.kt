package com.yosrhammami.socialclub

import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// In-memory stand-in for Firestore: modifyAtomically really applies `change` to what's stored,
// which a MockK stub of a lambda-taking method would make awkward to express.
class FakeContactRequestRepository : ContactRequestRepository {

    val requests = mutableMapOf<String, ContactRequest>()
    var failure: Throwable? = null  // simulates Firestore itself failing (network, permission)

    override fun observeContactRequest(senderAuthUid: String, receiverAuthUid: String): Flow<ContactRequest?> {
        return flowOf(requests[ContactRequest.generateId(senderAuthUid, receiverAuthUid)])
    }

    override suspend fun modifyAtomically(
        id: String,
        change: (current: ContactRequest?) -> Result<ContactRequest>
    ): Result<Unit> {
        failure?.let { return Result.failure(it) }
        return change(requests[id]).map { requests[id] = it }
    }
}
