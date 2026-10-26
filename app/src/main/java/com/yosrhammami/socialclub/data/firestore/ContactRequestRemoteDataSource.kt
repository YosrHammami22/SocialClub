package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.yosrhammami.socialclub.data.remote.ContactRequestDto
import com.yosrhammami.socialclub.domain.model.ContactRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ContactRequestRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getContactRequest(
        senderAuthUid: String,
        receiverAuthUid: String
    ): Flow<ContactRequestDto?> = callbackFlow {
        val id = ContactRequest.generateId(
            senderAuthUid,
            receiverAuthUid
        )

        val listener = firestore.collection("contactRequests")
            .document(id)
            .addSnapshotListener {snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // This callback runs on Firestore's thread, outside the flow's coroutine: an exception
                // thrown here (toObject() on a mistyped field, e.g. createdAt stored as a Timestamp)
                // would crash the app, unreachable by any try/catch around collect. close(e) routes it
                // into the flow instead, exactly like the listener error above.
                val dto = try {
                    snapshot?.toObject(ContactRequestDto::class.java)
                } catch (e: Exception) {
                    close(e)
                    return@addSnapshotListener
                }
                trySend(dto)
            }

        awaitClose {listener.remove()}
    }

    /*
    Read, decide and write in one transaction: if the document changes between our read and our
    write, Firestore retries the whole block with the fresh document, so [change] always decides on
    what's actually stored. A refusal from [change] is returned from the block (not thrown) so
    nothing is written, then thrown after await() for the repository to wrap as a Result.
     */
    suspend fun modifyAtomically(
        id: String,
        change: (current: ContactRequestDto?) -> Result<ContactRequestDto>
    ) {
        val ref = firestore.collection("contactRequests").document(id)
        val refusal: Throwable? = firestore.runTransaction { transaction ->
            val current = transaction.get(ref).toObject(ContactRequestDto::class.java)
            change(current).fold(
                onSuccess = { updated ->
                    transaction.set(ref, updated)
                    null
                },
                onFailure = { it }
            )
        }.await()
        if (refusal != null) throw refusal
    }
}/*
callbackFlow { } = builds a Flow from a listener (like addSnapshotListener).
trySend(...) = sends a new value every time Firestore updates.
awaitClose { listener.remove() } = this is the "tap turns off" part — removes the Firestore listener when nobody's watching anymore. New piece, but same tap idea from before.
snapshot?.toObject(...) = null if no document exists (no request yet) — matches our ContactRequestDto? nullable design.
 */