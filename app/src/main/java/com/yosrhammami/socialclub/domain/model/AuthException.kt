package com.yosrhammami.socialclub.domain.model

/*
Domain-level auth failures. The data layer translates Firebase's own exception types into these,
so use cases and ViewModels never import Firebase (keeps the domain platform-agnostic for a KMP phase).
Each one carries a user-readable message, since the ViewModels surface e.message directly.
 */
sealed class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class WeakPassword(cause: Throwable? = null) :
        AuthException("This password is too weak. Please choose a stronger one.", cause)

    class EmailAlreadyInUse(cause: Throwable? = null) :
        AuthException("An account already exists for this email.", cause)

    class InvalidEmail(cause: Throwable? = null) :
        AuthException("This email address is not valid.", cause)

    class Network(cause: Throwable? = null) :
        AuthException("Network error. Please check your connection and try again.", cause)

    // Sign-in only. Deliberately one message for "wrong password" and "no such account":
    // Firebase reports both the same way, so an attacker can't probe which emails exist.
    class InvalidCredentials(cause: Throwable? = null) :
        AuthException("Incorrect password. Please try again.", cause)

    class TooManyAttempts(cause: Throwable? = null) :
        AuthException("Too many attempts. Please wait a moment and try again.", cause)

    class SignInFailed(cause: Throwable? = null) :
        AuthException("We couldn't sign you in. Please try again.", cause)

    class Unknown(cause: Throwable? = null) :
        AuthException("We couldn't create your account. Please try again.", cause)
}
