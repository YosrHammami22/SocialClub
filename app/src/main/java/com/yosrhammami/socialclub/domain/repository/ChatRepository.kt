package com.yosrhammami.socialclub.domain.repository

import com.yosrhammami.socialclub.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {


    fun observeMessages(conversationId:String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, text: String): Result<Unit>

}