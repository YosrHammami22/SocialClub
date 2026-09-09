package com.yosrhammami.socialclub.domain.repository


import com.yosrhammami.socialclub.domain.model.Registration

interface RegistrationRepository {
    suspend fun getRegistrationsForEvent(eventId: String): List<Registration>
    suspend fun getRegistrationsForPerson(personId: String): List<Registration>
}