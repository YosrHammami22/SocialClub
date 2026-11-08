package com.yosrhammami.socialclub.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.socialclub.R
import com.yosrhammami.socialclub.ui.components.AppAvatar
import com.yosrhammami.socialclub.ui.components.BodyText
import com.yosrhammami.socialclub.ui.components.CaptionText
import com.yosrhammami.socialclub.ui.components.ErrorText
import com.yosrhammami.socialclub.ui.components.SubtitleText
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import com.yosrhammami.socialclub.ui.theme.Spacing
import com.yosrhammami.socialclub.ui.theme.preview.ThemePreviews

// No stateful ChatScreen yet: S2 is UI-only. The Hilt-backed `ChatScreen` (collecting a
// ViewModel's StateFlow and delegating here) arrives with the ViewModel story.

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // errorEvent : affiche-le comme dans AttendeeDetailScreen (Snackbar ou Toast)

    ChatContent(
        receiverName = viewModel.receiverName,
        receiverPhotoUrl = viewModel.receiverPhotoUrl,
        uiState = uiState,
        draft = viewModel.draft,
        onTextChange = viewModel::onTextChange,
        onSend = viewModel::onSend,
        onBack = onBack,
    )
}
// Stateless — pure UI, previewable, no ViewModel/Hilt involved
@Composable
fun ChatContent(
    receiverName: String,
    receiverPhotoUrl: String?,
    uiState: ChatUiState,
    draft: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        // Outside the animated body: who you're talking to never fades in and out between states.
        ChatTopBar(
            receiverName = receiverName,
            receiverPhotoUrl = receiverPhotoUrl,
            onBack = onBack
        )
        // AnimatedContent rather than Crossfade (used elsewhere) for one reason: contentKey.
        // Loading → Content fades, but Content → Content (a new message arrived) and
        // Content → Blocked (contact removed mid-conversation) share a key, so the same
        // LazyColumn stays in place — no full-list fade, scroll position kept.
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { state ->
                when (state) {
                    is ChatUiState.Content -> "messages"
                    else -> state::class
                }
            },
            modifier = Modifier.weight(1f),
            label = "chat body"
        ) { state ->
            when (state) {
                ChatUiState.Loading -> CenteredBox { CircularProgressIndicator() }

                ChatUiState.Empty -> EmptyConversation(receiverName = receiverName)

                is ChatUiState.Content -> MessageList(
                    messages = state.messages,
                    receiverName = receiverName
                )

                // History stays readable: losing the contact shouldn't erase what was said.
                is ChatUiState.Blocked -> CenteredBox { }

                is ChatUiState.Error -> CenteredBox { ErrorText(text = state.message) }
            }
        }
        // Outside the animated body so the field (and the keyboard) isn't torn down when the first
        // message moves the state from Empty to Content.
        when (uiState) {
            is ChatUiState.Empty, is ChatUiState.Content -> ChatInputBar(
                draft = draft,
                onTextChange = onTextChange,
                onSend = onSend
            )
            // The notice takes the input bar's place: the reason sits exactly where the user
            // would otherwise try to type.
            is ChatUiState.Blocked -> BlockedNotice(receiverName = receiverName)
            // Loading/Error: a message typed before the conversation is known has nowhere
            // reliable to go. The input bar normally owns the nav-bar inset, so keep that.
            ChatUiState.Loading, is ChatUiState.Error -> Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun BlockedNotice(receiverName: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        BodyText(
            text = stringResource(R.string.chat_blocked, receiverName),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                // Polite live region: if the contact becomes unavailable while the screen is
                // open, TalkBack announces it instead of the input silently vanishing.
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun EmptyConversation(receiverName: String) {
    CenteredBox {
        // One TalkBack stop for title + invitation, read as a single sentence.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.semantics(mergeDescendants = true) {}
        ) {
            SubtitleText(text = stringResource(R.string.chat_empty_title))
            BodyText(
                text = stringResource(R.string.chat_empty_body, receiverName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Not [com.yosrhammami.socialclub.ui.components.AppTopBar]: that one only takes a title String,
 * and an avatar next to the name is chat-specific. Same colors, so it still blends into the flat
 * design; if a second screen ever needs a leading avatar, promote this to a slot on AppTopBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    receiverName: String,
    receiverPhotoUrl: String?,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Decorative: the name right next to it already says who this is.
                AppAvatar(name = receiverName, photoUrl = receiverPhotoUrl, size = 36.dp)
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = receiverName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // Lets TalkBack users jump straight to "who am I talking to".
                    modifier = Modifier.semantics { heading() }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.navigate_back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun MessageList(
    messages: List<MessageUi>,
    receiverName: String,
    modifier: Modifier = Modifier
) {
    // reverseLayout anchors the list to the bottom: the newest message sits right above the
    // input bar, it stays there when the keyboard opens (the list shrinks from the top), and a
    // new message appears without a manual scrollToItem. The cost: items are laid out
    // bottom-up, so the chronological list is read through a reversed view (asReversed() is a
    // view, not a copy).
    LazyColumn(
        // fillMaxSize, not fillMaxWidth: a LazyColumn wraps its height, and a short
        // conversation would then float at the top instead of sitting above the input bar.
        modifier = modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // A stable key keeps a bubble's identity when the pending local copy is replaced by the
        // confirmed one, instead of re-composing every row below it.
        items(messages.asReversed(), key = { it.id }) { message ->
            MessageBubble(message = message, receiverName = receiverName)
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageUi,
    receiverName: String
) {
    val containerColor: Color
    val contentColor: Color
    val shape: RoundedCornerShape
    if (message.isMine) {
        containerColor = MaterialTheme.colorScheme.primaryContainer
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        // Small corner on the sender's side = the bubble's "tail". start/end (not left/right),
        // so it mirrors correctly in RTL.
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        containerColor = MaterialTheme.colorScheme.surfaceVariant
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    // One announcement per bubble ("You, 14:32: See you there, sending") instead of TalkBack
    // stopping on the text, the time and the spinner separately — and without the sender, a
    // screen reader user couldn't tell the two sides apart, since alignment and color are visual only.
    val sending = stringResource(R.string.chat_sending)
    val baseDescription = if (message.isMine) {
        stringResource(R.string.chat_bubble_mine_description, message.time, message.text)
    } else {
        stringResource(R.string.chat_bubble_theirs_description, receiverName, message.time, message.text)
    }
    val description = if (message.isPending) "$baseDescription, $sending" else baseDescription

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // The empty gutter on the opposite side caps bubble width, so a long message
            // never spans edge to edge and the left/right reading stays obvious.
            .padding(
                start = if (message.isMine) Spacing.xxl else 0.dp,
                end = if (message.isMine) 0.dp else Spacing.xxl
            ),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = shape,
            // fill = false: short messages get a short bubble; the weight only kicks in to stop
            // a long one from pushing past the gutter.
            modifier = Modifier
                .weight(1f, fill = false)
                .clearAndSetSemantics { contentDescription = description }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.md - Spacing.xs, vertical = Spacing.sm),
                horizontalAlignment = Alignment.End
            ) {
                BodyText(text = message.text, color = contentColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (message.isPending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(10.dp),
                            strokeWidth = 1.5.dp,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                    CaptionText(text = message.time, color = contentColor.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    draft: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    // Blank = empty or whitespace only: sending "   " would create an invisible bubble.
    val canSend = draft.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Edge-to-edge is on (MainActivity), so the bar must pad itself. navigationBars first,
            // then ime: insets padding consumes what's already applied, so with the keyboard open
            // the bar sits right on top of it, not keyboard height + nav bar height.
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        // Bottom: when the field grows to 4 lines, the send button stays next to the last
        // line (where the thumb and caret are), not floating in the middle.
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Not AppTextField: it has a floating label and singleLine/form styling. A chat composer
        // wants a placeholder (a label would jump above the text on focus) and maxLines, and the
        // pill shape is the conventional composer look rather than the filled-field spec.
        TextField(
            value = draft,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
            // Grows with content up to 4 lines, then TextField scrolls internally.
            minLines = 1,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f)
        )
        // Disabled rather than hidden: the button stays where the user expects it, and TalkBack
        // still finds it and announces "disabled", which explains why nothing happens.
        FilledIconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier
                .padding(bottom = Spacing.xs)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_send),
                contentDescription = stringResource(R.string.chat_send_button)
            )
        }
    }
}

// ---------- Previews ----------

private val previewMessages = listOf(
    MessageUi("1", "Hey Lucas! Great meeting you at the rooftop mixer yesterday.", "18:42", isMine = true),
    MessageUi("2", "Likewise! Still thinking about that sourdough debate 😄", "18:45", isMine = false),
    MessageUi("3", "Ha, I stand by it. Are you going to the Kotlin meetup on Thursday?", "18:46", isMine = true),
    MessageUi(
        "4",
        "Yes, I'll be there around 7. They're doing a talk on Compose performance, then drinks at the bar across the street.",
        "18:51",
        isMine = false
    ),
    MessageUi("5", "Perfect, see you there!", "18:52", isMine = true, isPending = true)
)

@ThemePreviews
@Composable
fun ChatContentWithMessagesPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Lucas Bernard",
                receiverPhotoUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                uiState = ChatUiState.Content(previewMessages),
                draft = "",
                onTextChange = {},
                onSend = {},
                onBack = {}
            )
        }
    }
}

// Draft long enough to hit the 4-line cap, with Send enabled.
@ThemePreviews
@Composable
fun ChatContentLongDraftPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Camille Martin",
                receiverPhotoUrl = null,
                uiState = ChatUiState.Content(previewMessages.take(2)),
                draft = "Sounds great! Quick question though: do you know if they're recording the talk? " +
                    "I might be a bit late because of work, and I'd hate to miss the part about " +
                    "recomposition. Also, should I bring my laptop for the workshop afterwards, " +
                    "or is it just a discussion?",
                onTextChange = {},
                onSend = {},
                onBack = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun ChatContentEmptyPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Amira Ben Salah",
                receiverPhotoUrl = "https://randomuser.me/api/portraits/women/44.jpg",
                uiState = ChatUiState.Empty,
                draft = "Hi Amira!",
                onTextChange = {},
                onSend = {},
                onBack = {}
            )
        }
    }
}

// Interactive mode: type to see the field grow and the Send button enable/disable.
@ThemePreviews
@Composable
fun ChatContentInteractivePreview() {
    var draft by remember { mutableStateOf("") }
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Lucas Bernard",
                receiverPhotoUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                uiState = ChatUiState.Content(previewMessages),
                draft = draft,
                onTextChange = { draft = it },
                onSend = { draft = "" },
                onBack = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun ChatContentLoadingPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Lucas Bernard",
                receiverPhotoUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                uiState = ChatUiState.Loading,
                draft = "",
                onTextChange = {},
                onSend = {},
                onBack = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun ChatContentBlockedPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Camille Martin",
                receiverPhotoUrl = "https://randomuser.me/api/portraits/women/68.jpg",
                uiState = ChatUiState.Blocked,
                draft = "",
                onTextChange = {},
                onSend = {},
                onBack = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun ChatContentErrorPreview() {
    SocialClubTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChatContent(
                receiverName = "Lucas Bernard",
                receiverPhotoUrl = "https://randomuser.me/api/portraits/men/32.jpg",
                uiState = ChatUiState.Error("Couldn't load this conversation. Check your connection and try again."),
                draft = "",
                onTextChange = {},
                onSend = {},
                onBack = {}
            )
        }
    }
}
