package com.yosrhammami.socialclub.domain.usecase

import com.yosrhammami.socialclub.core.util.runSuspendCatching
import com.yosrhammami.socialclub.domain.model.AuthException
import com.yosrhammami.socialclub.domain.model.ChatException
import com.yosrhammami.socialclub.domain.model.MAX_MESSAGE_LENGTH
import com.yosrhammami.socialclub.domain.model.pairIdOf
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import com.yosrhammami.socialclub.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
)  {

    suspend operator fun invoke(
        receiverUid: String,
        text:String
    ): Result<Unit> {
        if(text.isBlank())return Result.failure(ChatException.EmptyMessage())
        // Raw length, not trimmed: the raw text is what gets sent, so it's what must fit the limit.
        if (text.length > MAX_MESSAGE_LENGTH) return Result.failure(ChatException.MessageTooLong(MAX_MESSAGE_LENGTH))
        val currentUid = authRepository.currentUserUid()
            ?: return Result.failure(AuthException.NoSignedInUser())
        val id = runSuspendCatching { pairIdOf(currentUid, receiverUid) }
            .getOrElse { return Result.failure(it) }

        return chatRepository.sendMessage(id, text)
    }

}