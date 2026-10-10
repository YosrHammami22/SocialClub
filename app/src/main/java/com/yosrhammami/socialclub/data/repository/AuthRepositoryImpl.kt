package com.yosrhammami.socialclub.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.yosrhammami.socialclub.data.firestore.AuthRemoteDataSource
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun createAccount(email: String, password: String): Result<String> {
        return runCatching {
            remoteDataSource.createAccount(email, password)
        }.recoverCatching { throw it.toAuthException() }
    }

    override suspend fun deleteCurrentAccount(): Result<Unit> {
        return runCatching {
            remoteDataSource.deleteCurrentAccount()
        }
    }

    // WeakPassword is a subclass of InvalidCredentials in the Firebase SDK, so it must be checked first.
    private fun Throwable.toAuthException(): AuthException = when (this) {
        is FirebaseAuthWeakPasswordException -> AuthException.WeakPassword(this)
        is FirebaseAuthUserCollisionException -> AuthException.EmailAlreadyInUse(this)
        is FirebaseAuthInvalidCredentialsException -> AuthException.InvalidEmail(this)
        is FirebaseNetworkException -> AuthException.Network(this)
        else -> AuthException.Unknown(this)
    }
}
