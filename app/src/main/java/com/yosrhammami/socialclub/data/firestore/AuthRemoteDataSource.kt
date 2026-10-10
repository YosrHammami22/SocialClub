package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    // Note: on success Firebase also signs this new user in (firebaseAuth.currentUser is set).
    suspend fun createAccount(email: String, password: String): String {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return checkNotNull(result.user) { "Firebase returned no user after account creation" }.uid
    }

    suspend fun deleteCurrentAccount() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}
