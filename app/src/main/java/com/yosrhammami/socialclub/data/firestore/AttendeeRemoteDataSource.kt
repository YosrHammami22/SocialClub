package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.yosrhammami.socialclub.data.remote.AttendeeDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AttendeeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAttendee(personId: String): AttendeeDto? {
        val snapshot = firestore.collection("attendees")
            .document(personId)
            .get()
            .await()

        return snapshot.toObject(AttendeeDto::class.java)
    }
}