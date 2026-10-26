package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.ContactRequest
import kotlinx.coroutines.flow.Flow
/*
suspend means: "this function does work and waits, then gives one result." It pauses until finished.

But calling a function that returns a Flow doesn't wait for anything — it just hands you back the "tap" immediately (remember our tap/water analogy?). No waiting happens yet.

The actual waiting happens later, when you call .collect() on that Flow — collect() itself is suspend, not the function that creates the Flow.

So: function returning Flow<X> → no suspend needed.
Function returning X directly (one real value) → needs suspend.
 */
interface ContactRequestRepository {

    fun observeContactRequest(senderAuthUid: String, receiverAuthUid: String): Flow<ContactRequest?>

    /**
     * Atomically reads request [id], passes it to [change] (null if it doesn't exist) and writes
     * back what [change] returns. If [change] returns a failure, nothing is written and that
     * failure is returned.
     *
     * Why a callback instead of plain send/update methods: the business rules (who may answer,
     * when a resend is allowed) live in the domain, but they must be checked against the current
     * document *in the same transaction* as the write — otherwise the other person can change it
     * between our read and our write. [change] may run several times (Firestore retries a
     * transaction on contention), so it must be pure.
     */
    suspend fun modifyAtomically(
        id: String,
        change: (current: ContactRequest?) -> Result<ContactRequest>
    ): Result<Unit>
}
