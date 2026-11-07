package com.yosrhammami.socialclub.data.repository

import com.yosrhammami.socialclub.data.firestore.ChatRemoteDataSource

import com.yosrhammami.socialclub.domain.model.Message
import com.yosrhammami.socialclub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource
): ChatRepository {

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        remoteDataSource.observeMessages(conversationId)

    override suspend fun sendMessage(
        conversationId: String,
        text: String
    ) =remoteDataSource.sendMessage(conversationId, text)

}