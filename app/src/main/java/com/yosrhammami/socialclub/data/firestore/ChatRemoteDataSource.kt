package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.data.mapper.toMessage
import com.yosrhammami.socialclub.data.remote.MessageDto
import com.yosrhammami.socialclub.domain.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
)  {

    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val registration = firestore
            .collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents.orEmpty().mapNotNull { it.toMessage() }
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(conversationId: String, text: String): Result<Unit> = runSuspendCatching { // Le throw est attrapé par runSuspendCatching et devient Result.failure(AuthException.NoSignedInUser())
        val senderUid = auth.currentUser?.uid ?: throw IllegalStateException("Not connected")
        val dto = MessageDto(
            senderUid = senderUid,
            text = text
        )
        firestore
            .collection("conversations").document(conversationId)
            .collection("messages")
            .add(dto)
            .await()                                   // transforme la Task Firebase en appel suspend
        Unit
    }
    // add(dto)  → Firestore écrit dans son cache local IMMÉDIATEMENT

}
