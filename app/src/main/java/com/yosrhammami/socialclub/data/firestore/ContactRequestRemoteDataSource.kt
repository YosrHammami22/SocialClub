package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.yosrhammami.socialclub.data.remote.ContactRequestDto
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
}