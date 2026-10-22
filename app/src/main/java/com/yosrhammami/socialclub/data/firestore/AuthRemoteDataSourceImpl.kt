package com.yosrhammami.socialclub.data.firestore

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRemoteDataSource {

    // Note: on success Firebase also signs this new user in (firebaseAuth.currentUser is set).
    override suspend fun createAccount(email: String, password: String): String {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return checkNotNull(result.user) { "Firebase returned no user after account creation" }.uid
    }

    override suspend fun signIn(email: String, password: String): String {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return checkNotNull(result.user) { "Firebase returned no user after sign-in" }.uid
    }

    override fun signOut() = firebaseAuth.signOut()

    override fun currentUserUid(): String? = firebaseAuth.currentUser?.uid

    override suspend fun deleteCurrentAccount() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}
