package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.yosrhammami.socialclub.data.remote.EventDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRemoteDataSource  @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getEvent(eventId: String): EventDto? {
        val snapshot = firestore.collection("events").document(eventId).get().await()
        return snapshot.toObject(EventDto::class.java)
    }
}