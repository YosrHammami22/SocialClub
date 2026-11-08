package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException   // ← NOUVEAU import
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.data.mapper.toMessage
import com.yosrhammami.socialclub.data.remote.MessageDto
import com.yosrhammami.socialclub.domain.model.AuthException         // ← NOUVEAU import
import com.yosrhammami.socialclub.domain.model.ChatException         // ← NOUVEAU import
import com.yosrhammami.socialclub.domain.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {

    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val registration = firestore
            .collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    // ← NOUVEAU : on traduit l'erreur Firebase en erreur du domain.
                    // Le read rule refuse l'accès quand le contact n'est plus ACCEPTED (blocage).
                    val mapped = if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        ChatException.ContactBlocked()
                    } else {
                        error
                    }
                    close(mapped)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents.orEmpty().mapNotNull { it.toMessage() }
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(conversationId: String, text: String): Result<Unit> = runSuspendCatching {
        // ← CORRIGÉ : AuthException.NoSignedInUser, comme dans le use case
        val senderUid = auth.currentUser?.uid ?: throw AuthException.NoSignedInUser()
        val dto = MessageDto(
            senderUid = senderUid,
            text = text,
        )
        firestore
            .collection("conversations").document(conversationId)
            .collection("messages")
            .add(dto)       // Firestore écrit d'abord dans son cache local, immédiatement
            .await()        // puis on attend la confirmation du serveur (décision A)
        Unit
    }
}