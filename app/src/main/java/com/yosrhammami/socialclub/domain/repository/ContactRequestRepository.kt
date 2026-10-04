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

    suspend fun send(request: ContactRequest): Result<Unit>
    fun observeContactRequest(fromAttendeeId: String, toAttendeeId: String): Flow<ContactRequest?> //
}