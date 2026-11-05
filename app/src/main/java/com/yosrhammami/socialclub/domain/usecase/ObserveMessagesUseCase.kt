package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.model.Message
import com.yosrhammami.socialclub.domain.model.pairIdOf
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import com.yosrhammami.socialclub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveMessagesUseCase  @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {

    operator fun invoke(receiverUid: String): Flow<List<Message>> = flow {
        val currentUid = authRepository.currentUserUid()
            ?: throw AuthException.NoSignedInUser()
        val conversationId = pairIdOf(currentUid, receiverUid)
        emitAll(chatRepository.observeMessages(conversationId))
    }
}