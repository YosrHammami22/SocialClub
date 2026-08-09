package com.yosrhammami.socialclub.data

import com.yosrhammami.socialclub.data.remote.RandomUserResponse
import com.yosrhammami.socialclub.data.repository.RandomUserApiService

class FakeRandomUserApiService : RandomUserApiService {
    var responseToReturn: RandomUserResponse = RandomUserResponse(results = emptyList())

    override suspend fun getPeople(count: Int): RandomUserResponse {
        return responseToReturn
    }
}