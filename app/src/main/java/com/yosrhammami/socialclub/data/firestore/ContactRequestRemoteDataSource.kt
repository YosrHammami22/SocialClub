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
)  {
    suspend fun send(dto: ContactRequestDto) {
        firestore.collection("contactRequests")
            .document(dto.id)
            .set(dto)
            .await()
    }

    fun getContactRequest(fromAttendeeId: String, toAttendeeId: String): Flow<ContactRequestDto?> = callbackFlow {
        val id = ContactRequest.generateId(fromAttendeeId, toAttendeeId)

        val listener = firestore.collection("contactRequests")
            .document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(ContactRequestDto::class.java))
            }

        awaitClose { listener.remove() }
    }
}
/*
callbackFlow { } = builds a Flow from a listener (like addSnapshotListener).
trySend(...) = sends a new value every time Firestore updates.
awaitClose { listener.remove() } = this is the "tap turns off" part — removes the Firestore listener when nobody's watching anymore. New piece, but same tap idea from before.
snapshot?.toObject(...) = null if no document exists (no request yet) — matches our ContactRequestDto? nullable design.
 */