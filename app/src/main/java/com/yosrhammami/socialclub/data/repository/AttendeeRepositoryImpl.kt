package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.firestore.AttendeeRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.domain.model.Attendee
import com.yosrhammami.socialclub.domain.repository.AttendeeRepository
import javax.inject.Inject

class AttendeeRepositoryImpl @Inject constructor(
    private val remoteDataSource: AttendeeRemoteDataSource
) : AttendeeRepository {

    override suspend fun findAttendeeByEmail(email: String): Attendee? {
        val dto = remoteDataSource.findAttendeeByEmail(email) ?: return null
        return dto.toDomain()
    }
}