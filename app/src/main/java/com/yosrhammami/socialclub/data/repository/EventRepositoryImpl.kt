package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.firestore.EventRemoteDataSource
import com.yosrhammami.socialclub.data.remote.toDomain
import com.yosrhammami.socialclub.domain.model.Event
import com.yosrhammami.socialclub.domain.repository.EventRepository
import javax.inject.Inject

class EventRepositoryImpl  @Inject constructor(
    private val remoteDataSource: EventRemoteDataSource
): EventRepository {

    override suspend fun getEvent(eventId: String): Event? {
        val dto = remoteDataSource.getEvent(eventId) ?: return null
        return dto.toDomain(id = eventId)
    }
}