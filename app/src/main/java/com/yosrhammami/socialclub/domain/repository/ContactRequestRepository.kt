package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.ContactRequest

interface ContactRequestRepository {

    suspend fun send(request: ContactRequest): Result<Unit>
}