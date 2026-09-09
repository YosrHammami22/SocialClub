package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.domain.repository.RegistrationRepository
import com.yosrhammami.socialclub.data.firestore.RegistrationRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.domain.model.Registration
import javax.inject.Inject

class RegistrationRepositoryImpl @Inject constructor(private val remoteDataSource: RegistrationRemoteDataSource):
    RegistrationRepository {

    override suspend fun getRegistrationsForEvent(eventId: String): List<Registration> {
        val dto = remoteDataSource.getRegistrationsForEvent(eventId)  ?:return emptyList()
        return dto.map {it.toDomain()}
    }

    override suspend fun getRegistrationsForPerson(personId: String): List<Registration> {
        val dto = remoteDataSource.getRegistrationsForPerson(personId)  ?:return emptyList()
        return dto.map {it.toDomain()}
    }
}