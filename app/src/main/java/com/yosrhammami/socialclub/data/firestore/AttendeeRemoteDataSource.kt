package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.yosrhammami.socialclub.data.remote.AttendeeDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AttendeeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun findAttendeeByEmail(email: String): AttendeeDto? {
        val snapshot = firestore.collection("attendees")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()

        val document = snapshot.documents.firstOrNull() ?: return null
        val dto = document.toObject(AttendeeDto::class.java) ?: return null
        return dto
    }
}