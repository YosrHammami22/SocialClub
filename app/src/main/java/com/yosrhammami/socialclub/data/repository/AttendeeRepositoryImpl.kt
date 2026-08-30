package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.firestore.AttendeeRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import javax.inject.Inject

class AttendeeRepositoryImpl @Inject constructor(
    private val remoteDataSource: AttendeeRemoteDataSource
) : AttendeeRepository {

    override suspend fun getAttendee(personId: String): Attendee? {
        val dto = remoteDataSource.getAttendee(personId) ?: return null
        return dto.toDomain(id = personId)
    }
}