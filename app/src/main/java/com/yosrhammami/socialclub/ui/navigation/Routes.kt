package com.yosrhammami.socialclub.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object PeopleListRoute

@Serializable
data class PersonDetailRoute(val personId: String)

@Serializable
object HomeRoute

@Serializable
data class AttendeeRoute(val email: String)

@Serializable
data class CreatePasswordRoute(val attendeeId: String, val email: String)

@Serializable
data class SignInRoute(val email: String)

@Serializable
data class EventAttendeesRoute(val eventId: String)

@Serializable
data class AttendeeDetailRoute(val attendeeId: String)

@Serializable
object SettingsRoute

/**
 * Name and photo are passed along (instead of re-reading the attendee) to save a Firestore read.
 * Trade-off: if the receiver changes their photo while the chat is open, the old one stays.
 */
@Serializable
data class ChatRoute(
    val receiverUid: String,              // attendee.authUid, NOT attendeeId (the document id)
    val receiverName: String,
    val receiverPhotoUrl: String? = null,
)