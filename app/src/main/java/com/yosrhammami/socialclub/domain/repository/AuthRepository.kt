package com.yosrhammami.socialclub.domain.repository

interface AuthRepository {

    // Returns the new account's uid. Failures are AuthException subtypes.
    suspend fun createAccount(email: String, password: String): Result<String>

    // Signs an existing account in on this device. Failures are AuthException subtypes.
    suspend fun signIn(email: String, password: String): Result<Unit>

    // Deletes the currently signed-in account. Used to roll back a half-finished first connection.
    suspend fun deleteCurrentAccount(): Result<Unit>
}
