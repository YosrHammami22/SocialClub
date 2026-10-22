package com.yosrhammami.socialclub.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.data.firestore.AuthRemoteDataSource
import com.yosrhammami.socialclub.data.session.DeliberateLogoutFlag
import com.yosrhammami.socialclub.data.session.SessionManager
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionManager: SessionManager,
    private val logoutFlag: DeliberateLogoutFlag
) : AuthRepository {

    override suspend fun createAccount(email: String, password: String): Result<String> {
        return runSuspendCatching {
            remoteDataSource.createAccount(email, password)
        }.recoverCatching { throw it.toAuthException() }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return runSuspendCatching {
            remoteDataSource.signIn(email, password)
        }.recoverCatching { throw it.toSignInException() }
    }

    override fun signOut() = remoteDataSource.signOut()

    /*
    The order is the point (plan.md, locked by AuthRepositoryImplTest):
    1. mark deliberate first, so whoever reacts to the session ending knows not to show "session expired" (AC6);
    2. clear the session, so the previous user's data is gone from memory (AC5);
    3. sign out last, so there is never a moment where we're signed out but the user's data is still held.
    Lives here, not in a ViewModel: documented exception to "session state is set by ViewModels" (CLAUDE.md).
     */
    override suspend fun logout() {
        logoutFlag.markDeliberate()
        sessionManager.clear()
        remoteDataSource.signOut()
    }

    override fun currentUserUid(): String? = remoteDataSource.currentUserUid()

    override suspend fun deleteCurrentAccount(): Result<Unit> {
        return runSuspendCatching {
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

    /*
    A separate mapping from account creation: the same Firebase type means something different here.
    On sign-in, FirebaseAuthInvalidCredentialsException is a wrong password, not a malformed email.
     */
    private fun Throwable.toSignInException(): AuthException = when (this) {
        is FirebaseAuthInvalidCredentialsException -> AuthException.InvalidCredentials(this)
        is FirebaseTooManyRequestsException -> AuthException.TooManyAttempts(this)
        is FirebaseNetworkException -> AuthException.Network(this)
        else -> AuthException.SignInFailed(this)
    }
}
