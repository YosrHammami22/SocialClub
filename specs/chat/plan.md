# Plan: Chat between attendees (1-to-1)

**Status:** Plan ✅ (written after the first implementation of the domain, data and ViewModel layers)
**Spec:** [`spec.md`](./spec.md) · **Tasks:** [`tasks.md`](./tasks.md)

> This file describes **how** the spec is built in SocialClub's architecture.
> Every decision points back to an acceptance criterion (AC) or constraint (C) in `spec.md`.

---

## Overview

```
AttendeeDetailScreen ──"Message" (contact ACCEPTED)──▶ ChatRoute(receiverUid, receiverName, receiverPhotoUrl)   (AC1)
                                                                │
                                                                ▼
ChatScreen ──collectAsStateWithLifecycle──▶ ChatViewModel ──┬─▶ ObserveMessagesUseCase ─┐
  (Loading / Empty / Content /                              └─▶ SendMessageUseCase ─────┤
   Blocked / Error)                                                                     ▼
                                                                         ChatRepository (interface)
                                                                                        │
                                                                                        ▼
                                                                    ChatRepositoryImpl ──▶ ChatRemoteDataSource
                                                                                        │
                                                                                        ▼
                                                        Firestore: conversations/{pairId}/messages/{autoId}
```

---

## Data model (Firestore)

```
conversations/{pairId}/messages/{autoId}
  senderUid : String     // Firebase Auth UID of the sender
  text      : String     // 1..1000 chars, raw (untrimmed)
  sentAt    : Timestamp  // server timestamp
```

| Element | Decision | Why |
|---|---|---|
| `pairId` | `pairIdOf(authUidA, authUidB)`: the two auth UIDs **sorted**, joined by `_`. | Both participants compute the same ID with no lookup. It is the **same ID** as `contactRequests/{pairId}`, so security rules can read the contact status with one `get()` (AC9). |
| Parent document `conversations/{pairId}` | **Does not exist in V1**; only the `messages` subcollection is written. | Nothing needs it yet. It becomes the home of `lastMessage` / `updatedAt` for the conversation list (follow-up). |
| Auth UID, not attendee document ID | `ChatRoute.receiverUid` is `attendee.authUid`. | Security rules only know `request.auth.uid`; the attendee document ID means nothing to them (AC1, AC9). |

### Alternatives considered

| Option | Rejected because |
|---|---|
| Messages as an array in one document | A document is limited to 1 MiB and about 1 sustained write per second. A conversation would hit both. |
| `messages` subcollection under `contactRequests/{pairId}` | Mixes two lifetimes and two sets of rules in one place. A separate collection keeps contact rules unchanged, and blocking (follow-up) can delete a conversation without touching the contact request. |

---

## Domain

| Element | Decision | Why |
|---|---|---|
| `Message` | `id`, `senderUid`, `text`, `sentAt: Long`, `isPending: Boolean`. No Firebase types. | Domain stays platform-agnostic (KMP-friendly). `isPending` drives the "sending" indicator (AC4, AC5). |
| `ChatRepository` | Interface: `observeMessages(conversationId): Flow<List<Message>>`, `suspend sendMessage(conversationId, text): Result<Unit>`. | Mockable in tests; hides Firestore from the domain. |
| `ObserveMessagesUseCase` | `invoke(receiverUid)`: reads the current UID from `AuthRepository` (none → fails with `AuthException.NoSignedInUser`), builds `pairIdOf(currentUid, receiverUid)`, then forwards the repository's cold `Flow` items and errors unchanged. | AC2, AC3. The use case, not the ViewModel, knows how a conversation ID is built, so the UI only deals with the receiver. Tested with Turbine (it is a stream). |
| `SendMessageUseCase` | `invoke(receiverUid, text)`, in this order: blank → `ChatException.EmptyMessage`; > `MAX_MESSAGE_LENGTH` (1000) → `ChatException.MessageTooLong(max)`; no signed-in user → `AuthException.NoSignedInUser`; `pairIdOf` fails (e.g. receiver is the current user) → its `IllegalArgumentException`; then sends. Every failure is returned as `Result.failure`, never thrown. | AC4, C4. Same limit as the security rules. Validates the **raw** text without trimming, so what is checked is what is sent. Text checks come first, so invalid input never reaches auth or the repository. |
| `ChatException` | Sealed: `EmptyMessage`, `MessageTooLong(max)`, `ContactBlocked`. | Typed failures the ViewModel can map to states (AC6, AC7). |

---

## Data

| Element | Decision | Why |
|---|---|---|
| `MessageDto` | Default values on every field; `sentAt` annotated `@ServerTimestamp`. | `toObject()` needs a no-arg constructor (project convention). `@ServerTimestamp` lets the server set the time, never the device clock. |
| `ChatRemoteDataSource.observeMessages` | `callbackFlow` around a snapshot listener, ordered by `sentAt` ascending; listener removed in `awaitClose`. | AC2 (oldest first), AC3 (real time). `awaitClose` prevents a leaked listener when the collector stops. |
| Listener option | `addSnapshotListener(MetadataChanges.INCLUDE)`. | AC4: when the server confirms a message, only its **metadata** changes (`hasPendingWrites` goes from `true` to `false`), not its data. Without `INCLUDE`, no new snapshot is emitted for that change, so the "sending" indicator would never disappear. |
| Pending messages | Read snapshots with `ServerTimestampBehavior.ESTIMATE`; `isPending = metadata.hasPendingWrites()`. | A local, unconfirmed write has no server time yet; ESTIMATE gives it a local estimate so it sorts at the bottom instead of being `null`. `hasPendingWrites()` is exactly "not confirmed by the server" (AC4, AC5). |
| `DocumentSnapshot.toMessage()` | Returns `null`, and the message is **skipped silently**, when the document can't be deserialized, has no `sentAt`, or has a blank `senderUid`. | One malformed document must not fail the whole conversation (which would show the Error state, AC8). **Trade-off:** a skipped message is invisible to both participants and leaves no trace (not logged). With the security rules (AC9) such documents should not exist. |
| `sendMessage` | Adds the document and **awaits server confirmation**, returning `Result`. | A failure can be reported (AC6). The UI does not wait on it: the listener shows the message at once (AC4). |
| Permission denied | `PERMISSION_DENIED` from the listener is mapped to `ChatException.ContactBlocked(cause = error)`: the original Firestore error is kept as the cause. | AC7: a refused read means the contact is no longer accepted. The cause is kept because `PERMISSION_DENIED` can also come from missing rules or a wrong conversation ID; without it, the logs would only say "Contact is blocked" and hide the real origin. |
| Firestore cache | **Unchanged:** memory cache only (`FirestoreModule`, from the Logout plan). | Keeps Logout AC5. Consequence: the offline queue lives in memory only, so AC5 holds while the app runs (spec Q1). |
| `ChatRepositoryModule` | Hilt `@Binds` `ChatRepositoryImpl` → `ChatRepository`. | Constructor injection, interface binding (project convention). |
| `ChatRemoteDataSource` | **Concrete class** (no interface), injected with `FirebaseFirestore` and `FirebaseAuth`. `sendMessage` reads the sender UID from `FirebaseAuth.currentUser` itself. | As built. See open point below. |

### Alternatives considered (data)

| Option | Rejected because |
|---|---|
| `ServerTimestampBehavior.NONE` (the default) | A message not yet confirmed has `sentAt == null`; `toMessage()` skips it, so the "sending" bubble would never appear (AC4, AC5). |
| `ServerTimestampBehavior.PREVIOUS` | Returns the previous server value, which a brand-new message doesn't have: also `null`, same problem. |
| Device time (`Timestamp.now()`) for `sentAt` | Device clocks can be wrong or changed, so the two participants could see a different order (AC2). The security rules also require `sentAt == request.time` (AC9). |
| `sendMessage` without `await()` (fire-and-forget) | A rejected write could never be reported, so AC6 would be impossible. Awaiting costs nothing in the UI: the listener already shows the message (AC4). |

### Open point: `ChatRemoteDataSource` (not decided)

The code differs from two choices made elsewhere in the project:

| Difference | Elsewhere | Consequence |
|---|---|---|
| Concrete class, no interface | `AuthRemoteDataSource` is an interface so tests can mock it (project rule: mock interfaces, never concrete classes). | `ChatRepositoryImpl` can't be unit-tested with a MockK mock of its data source; there are no chat data-layer tests today. |
| Sender UID read from `FirebaseAuth` in the data source | The use cases read the current UID from `AuthRepository`. | Two sources for "who is signed in" in one send. They agree today; if one changes (e.g. a test fake), they can disagree. Passing the UID from the use case down would leave one source. |

Options are to extract an interface and/or pass `senderUid` from `SendMessageUseCase`. Neither changes behavior; not applied yet.

---

## Security rules (AC9)

| Rule | Enforces |
|---|---|
| `read`, `create` only if `request.auth.uid` is one of the two UIDs in `pairId` **and** `contactRequests/{pairId}.status == "ACCEPTED"` | C2 |
| `request.resource.data.senderUid == request.auth.uid` | Nobody sends on someone else's behalf |
| `text is string && text.size() >= 1 && text.size() <= 1000` | C4 |
| `sentAt == request.time` | Server time only |
| `request.resource.data.keys().hasOnly(['senderUid', 'text', 'sentAt'])` | No extra fields |
| `update, delete: if false` | Messages are immutable from the app (deletion on block is a follow-up, done server side) |

> **Where the rules live:** in the Firebase console only. There is no `firestore.rules` in the repo yet,
> so this table can't be checked against the code and nothing tests the rules (follow-up in `tasks.md`).

### Alternatives considered (validation)

| Option | Rejected because |
|---|---|
| Validate on the server only | No immediate feedback: the user would type, send, and only then learn the text was refused (AC4). |
| Validate in the app only | A modified client can bypass it; only the server actually guarantees C2 and C4 (AC9). |

---

## Presentation

| Element | Decision | Why |
|---|---|---|
| `ChatRoute` | `receiverUid`, `receiverName`, `receiverPhotoUrl?`. Read in `ChatViewModel` via `SavedStateHandle`. | AC1. Name and photo passed along to save a Firestore read (accepted trade-off in the spec). |
| `ChatUiState` | `Loading`, `Empty`, `Content(messages)`, `Blocked`, `Error(message)`. | One state per case in AC2, AC7, AC8. |
| State exposure | `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)`, collected with `collectAsStateWithLifecycle()`. | AC2, AC3. The listener stops when the screen is in the background, saving reads, but survives a rotation (under 5 s) without re-subscribing. |
| Send | Clears the field immediately; failures come back as a one-shot event shown as the "Message not sent." toast. | AC4, AC6. A one-shot event, not a state, so the toast is not shown again after rotation. **Known gap:** `ChatScreen` doesn't collect `errorEvent` yet, so the toast never appears (see `tasks.md`). |
| `MessageUiMapper` | Maps `Message` to `MessageUi` (mine/theirs, formatted time, pending flag). | Keeps formatting out of the ViewModel and the Composable; unit-testable. |
| `ChatScreen` / `ChatContent` | Stateful/stateless split; the input bar is replaced by a notice in `Blocked`. | Project convention; AC7. |
| Entry point | "Message" button on `AttendeeDetailScreen`, shown only when the contact request status is `ACCEPTED`. | AC1. |

### Alternative considered

| Option | Rejected because |
|---|---|
| Re-read the receiver's attendee document in the chat | One extra read per chat open, for data the previous screen already has. |
| Load in `init{}` into a `MutableStateFlow` (the project's usual one-shot pattern) | The real-time listener would stay open from creation until the ViewModel is cleared, including while the app is in the background, costing reads for nothing. `WhileSubscribed` ties it to the screen being visible (AC3). |
| `SharingStarted.WhileSubscribed(0)` | A rotation briefly has no collector, so the listener would close and re-open, re-reading the whole conversation. 5 s covers a configuration change. |
| `SharingStarted.Eagerly` | Same issue as `init{}`: the listener runs as long as the ViewModel lives. |

---

## Coverage

| AC | Covered by |
|---|---|
| AC1 | `AttendeeDetailScreen` button condition, `ChatRoute(receiverUid = authUid)` |
| AC2 | `ChatUiState` (`Loading` / `Empty` / `Content`), `orderBy(sentAt)` |
| AC3 | Snapshot listener in `ChatRemoteDataSource`, `ObserveMessagesUseCase` |
| AC4 | `SendMessageUseCase` validation, `isPending` via `hasPendingWrites()` + `MetadataChanges.INCLUDE`, field cleared in `ChatViewModel` |
| AC5 | Firestore's in-memory write queue + `isPending` (restart case: spec Q1) |
| AC6 | `sendMessage` returns `Result`; one-shot event → toast |
| AC7 | `PERMISSION_DENIED` → `ChatException.ContactBlocked` → `ChatUiState.Blocked` |
| AC8 | Any other error → `ChatUiState.Error` |
| AC9 | Firestore Security Rules (table above) |
