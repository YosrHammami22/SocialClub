package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.Event

interface EventRepository {
    suspend fun getEvent(eventId: String): Event?
}