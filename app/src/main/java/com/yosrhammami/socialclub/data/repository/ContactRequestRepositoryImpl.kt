package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.firestore.ContactRequestRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.data.remote.toDto
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRequestRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContactRequestRemoteDataSource
): ContactRequestRepository {

    override suspend fun send(request: ContactRequest): Result<Unit> {
        return runCatching {
            remoteDataSource.send(request.toDto())
        }
    }

    override fun observeContactRequest(
        fromAttendeeId: String,
        toAttendeeId: String
    ): Flow<ContactRequest?> {
        return remoteDataSource.getContactRequest(
            fromAttendeeId,
            toAttendeeId
        )
            .map {dto -> dto?.toDomain()}
    }
}