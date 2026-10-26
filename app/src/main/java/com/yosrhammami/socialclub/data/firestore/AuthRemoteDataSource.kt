package com.yosrhammami.socialclub.data.firestore

/*
An interface (Firebase lives only in AuthRemoteDataSourceImpl) so AuthRepositoryImpl can be tested
with a mock, per the project rule "mock interfaces, never concrete classes".
 */
interface AuthRemoteDataSource {

    // Returns the new account's uid. Firebase also signs this new user in.
    suspend fun createAccount(email: String, password: String): String

    suspend fun signIn(email: String, password: String): String

    // Local only, no network: this is what lets logout work offline (spec AC2).
    fun signOut()

    fun currentUserUid(): String?

    suspend fun deleteCurrentAccount()
}
