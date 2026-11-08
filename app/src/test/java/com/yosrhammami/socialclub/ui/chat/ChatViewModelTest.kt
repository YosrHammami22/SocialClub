package com.yosrhammami.socialclub.ui.chat

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.yosrhammami.socialclub.FakeLogger
import com.yosrhammami.socialclub.MainDispatcherRule
import com.yosrhammami.socialclub.domain.model.ChatException
import com.yosrhammami.socialclub.domain.model.Message
import com.yosrhammami.socialclub.domain.repository.AuthRepository
import com.yosrhammami.socialclub.domain.usecase.ObserveMessagesUseCase
import com.yosrhammami.socialclub.domain.usecase.SendMessageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeMessagesUseCase = mockk<ObserveMessagesUseCase>()
    private val sendMessageUseCase = mockk<SendMessageUseCase>()
    private val authRepository = mockk<AuthRepository> {
        every { currentUserUid() } returns "uidMe"
    }

    private val mine = Message(
        id = "m1", senderUid = "uidMe", text = "Are you going to the rooftop event?",
        sentAt = 1_762_610_700_000, isPending = false,
    )
    private val theirs = Message(
        id = "m2", senderUid = "uidLea", text = "Yes! See you at 8.",
        sentAt = 1_762_610_760_000, isPending = false,
    )

    private fun createViewModel(
        messages: Flow<List<Message>> = flowOf(emptyList()),
        args: Map<String, Any?> = mapOf(
            "receiverUid" to "uidLea",
            "receiverName" to "Léa Martin",
            "receiverPhotoUrl" to "https://randomuser.me/api/portraits/women/44.jpg",
        ),
    ): ChatViewModel {
        every { observeMessagesUseCase("uidLea") } returns messages
        return ChatViewModel(
            observeMessagesUseCase, sendMessageUseCase, authRepository, FakeLogger(),
            SavedStateHandle(args),
        )
    }

    // uiState is stateIn(WhileSubscribed): without a subscriber the upstream never starts and
    // .value stays Loading. A background collector plays the role of the Composable.
    // Unconfined so it subscribes immediately: backgroundScope alone runs on runTest's lazy
    // StandardTestDispatcher, and the collector would only start after the assertions.
    private fun TestScope.subscribe(viewModel: ChatViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    // ---------- uiState ----------

    @Test
    fun `before the first emission, uiState is Loading`() = runTest {
        val viewModel = createViewModel(messages = MutableSharedFlow()) // never emits
        subscribe(viewModel)

        assertEquals(ChatUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `when messages arrive, uiState becomes Content with isMine resolved against the signed-in uid`() = runTest {
        val viewModel = createViewModel(messages = flowOf(listOf(mine, theirs)))
        subscribe(viewModel)

        val state = viewModel.uiState.value
        assertEquals(ChatUiState.Content(listOf(mine.toUi("uidMe"), theirs.toUi("uidMe"))), state)
        // Explicit, so the test doesn't only trust the mapper call above.
        assertEquals(listOf(true, false), (state as ChatUiState.Content).messages.map { it.isMine })
    }

    @Test
    fun `when the conversation has no messages, uiState becomes Empty`() = runTest {
        val viewModel = createViewModel(messages = flowOf(emptyList()))
        subscribe(viewModel)

        assertEquals(ChatUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `when observing messages fails, uiState becomes Error with a generic message`() = runTest {
        val viewModel = createViewModel(messages = flow { throw RuntimeException("PERMISSION_DENIED") })
        subscribe(viewModel)

        assertEquals(ChatUiState.Error("Couldn't load this conversation."), viewModel.uiState.value)
    }

    @Test
    fun `when the contact is blocked, uiState becomes Blocked instead of Error`() = runTest {
        val viewModel = createViewModel(messages = flow { throw ChatException.ContactBlocked() })
        subscribe(viewModel)

        assertEquals(ChatUiState.Blocked, viewModel.uiState.value)
    }

    @Test
    fun `when a new message arrives in an empty conversation, uiState switches from Empty to Content`() = runTest {
        val live = MutableSharedFlow<List<Message>>()
        val viewModel = createViewModel(messages = live)
        subscribe(viewModel)

        live.emit(emptyList())
        assertEquals(ChatUiState.Empty, viewModel.uiState.value)

        live.emit(listOf(theirs))
        assertEquals(ChatUiState.Content(listOf(theirs.toUi("uidMe"))), viewModel.uiState.value)
    }

    // ---------- draft / onSend ----------

    @Test
    fun `onTextChange updates the draft`() {
        val viewModel = createViewModel()

        viewModel.onTextChange("Hello Léa")

        assertEquals("Hello Léa", viewModel.draft)
    }

    @Test
    fun `onSend sends the draft to the receiver from the route`() = runTest {
        coEvery { sendMessageUseCase(any(), any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.onTextChange("Hello Léa")

        viewModel.onSend()

        coVerify(exactly = 1) { sendMessageUseCase("uidLea", "Hello Léa") }
    }

    @Test
    fun `onSend clears the draft before the send completes`() = runTest {
        // Decision A: the field empties immediately, it doesn't wait for the network.
        val pendingSend = CompletableDeferred<Result<Unit>>()
        coEvery { sendMessageUseCase(any(), any()) } coAnswers { pendingSend.await() }
        val viewModel = createViewModel()
        viewModel.onTextChange("Hello Léa")

        viewModel.onSend()

        assertEquals("", viewModel.draft)
        pendingSend.complete(Result.success(Unit))
    }

    @Test
    fun `when sending fails, errorEvent emits Message not sent`() = runTest {
        coEvery { sendMessageUseCase(any(), any()) } returns Result.failure(RuntimeException("UNAVAILABLE"))
        val viewModel = createViewModel()
        viewModel.onTextChange("Hello Léa")

        viewModel.errorEvent.test {
            viewModel.onSend()
            assertEquals("Message not sent.", awaitItem())
        }
    }

    @Test
    fun `when sending succeeds, no error event is emitted`() = runTest {
        coEvery { sendMessageUseCase(any(), any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.onTextChange("Hello Léa")

        viewModel.errorEvent.test {
            viewModel.onSend()
            expectNoEvents()
        }
    }

    // ---------- route arguments ----------

    @Test(expected = IllegalStateException::class)
    fun `when receiverUid is missing from the route, construction fails fast`() {
        createViewModel(args = mapOf("receiverName" to "Léa Martin"))
    }
}
