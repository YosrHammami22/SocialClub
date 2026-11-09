package com.yosrhammami.socialclub.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.domain.model.ChatException
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import com.yosrhammami.socialclub.domain.usecase.ObserveMessagesUseCase
import com.yosrhammami.socialclub.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    authRepository: AuthRepository,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // ① ChatRoute arguments (keys = ChatRoute property names). Read by key, like the other
    //    ViewModels of the project: works in plain JUnit tests with SavedStateHandle(mapOf(...)).
    private val receiverUid: String = checkNotNull(savedStateHandle["receiverUid"])
    val receiverName: String = checkNotNull(savedStateHandle["receiverName"])
    val receiverPhotoUrl: String? = savedStateHandle["receiverPhotoUrl"]

    private val currentUid: String? = authRepository.currentUserUid()

    // ② Draft: mutableStateOf so the TextField updates synchronously (no cursor jump).
    var draft by mutableStateOf("")
        private set

    // ③ One-off errors: an event, not a state (not shown again after a rotation).
    private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // ④ Screen state: listener kept during a rotation, closed 5 s after the screen stops observing.
    val uiState: StateFlow<ChatUiState> = observeMessagesUseCase(receiverUid)
        .map { messages ->
            if (messages.isEmpty()) {
                ChatUiState.Empty
            } else {
                ChatUiState.Content(messages.map { it.toUi(currentUid) })
            }
        }
        .catch { e ->
            logger.e("Error observing messages", e)
            emit(
                if (e is ChatException.ContactBlocked) {
                    ChatUiState.Blocked
                } else {
                    ChatUiState.Error("Couldn't load this conversation.")
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChatUiState.Loading,
        )

    fun onTextChange(text: String) {
        draft = text
    }

    // ⑤ Decision A: clear the field now, send in the background, report only failures.
    fun onSend() {
        val text = draft
        draft = ""
        viewModelScope.launch {
            sendMessageUseCase(receiverUid, text).onFailure { e ->
                logger.e("Error sending message", e)
                _errorEvent.tryEmit("Message not sent.")
            }
        }
    }
}