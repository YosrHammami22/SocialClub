package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.firestore.ContactRequestRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDto
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import javax.inject.Inject

class ContactRequestRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContactRequestRemoteDataSource
): ContactRequestRepository {

    override suspend fun send(request: ContactRequest): Result<Unit> {
        return runCatching {
            remoteDataSource.send(request.toDto())
        }
    }
}