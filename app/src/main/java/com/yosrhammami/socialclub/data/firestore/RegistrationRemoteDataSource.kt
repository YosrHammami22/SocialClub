package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.yosrhammami.socialclub.data.remote.RegistrationDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RegistrationRemoteDataSource @Inject constructor(private val firestore: FirebaseFirestore) {

    suspend fun getRegistrationsForPerson(personId: String): List<RegistrationDto> {
        val snapshot = firestore.collection("registrations")
            .whereEqualTo(
                "personId",
                personId
            )
            .get()
            .await()

        return snapshot.toObjects(RegistrationDto::class.java)
    }

    suspend fun getRegistrationsForEvent(eventId: String): List<RegistrationDto> {
        val snapshot = firestore.collection("registrations")
            .whereEqualTo(
                "eventId",
                eventId
            )
            .get()
            .await()

        return snapshot.toObjects(RegistrationDto::class.java)
    }
}