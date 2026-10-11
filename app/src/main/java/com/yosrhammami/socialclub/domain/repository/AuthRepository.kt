package com.yosrhammami.socialclub.domain.repository

interface AuthRepository {

    // Returns the new account's uid. Failures are AuthException subtypes.
    suspend fun createAccount(email: String, password: String): Result<String>

    // Signs an existing account in on this device and returns its uid. Failures are AuthException subtypes.
    suspend fun signIn(email: String, password: String): Result<String>

    // Drops this device's session. Local only, so it can't fail on the network.
    fun signOut()

    // Uid of the account signed in on this device, or null when there is no session.
    // Not suspend: Firebase keeps the session in memory, so this is a local read.
    fun currentUserUid(): String?

    // Deletes the currently signed-in account. Used to roll back a half-finished first connection.
    suspend fun deleteCurrentAccount(): Result<Unit>
}
