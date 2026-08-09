package com.yosrhammami.socialclub.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object PeopleListRoute

@Serializable
data class PersonDetailRoute(val personId: String)