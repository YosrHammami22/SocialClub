package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.data.firestore.ContactRequestRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.data.remote.toDto
import com.yosrhammami.socialclub.domain.model.ContactRequest
import com.yosrhammami.socialclub.domain.repository.ContactRequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/*
runSuspendCatching catches that as a safety net (and rethrows cancellation)
 */

class ContactRequestRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContactRequestRemoteDataSource
): ContactRequestRepository {

    override fun observeContactRequest(
        senderAuthUid: String,
        receiverAuthUid: String
    ): Flow<ContactRequest?> {
        return remoteDataSource.getContactRequest(
            senderAuthUid,
            receiverAuthUid
        )
            .map {dto -> dto?.toDomain()}
    }

    override suspend fun modifyAtomically(
        id: String,
        change: (current: ContactRequest?) -> Result<ContactRequest>
    ): Result<Unit> {
        return runSuspendCatching {
            remoteDataSource.modifyAtomically(id) { dto ->
                change(dto?.toDomain()).map { it.toDto() }
            }
        }
    }
}