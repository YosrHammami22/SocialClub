# Plan: Chat between attendees (1-to-1)

**Status:** Plan ✅ (written after the first implementation of the domain, data and ViewModel layers)
**Spec:** [`spec.md`](./spec.md)

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
| `ObserveMessagesUseCase` | Returns the repository's cold `Flow`, forwarding items and errors unchanged. | AC2, AC3. Tested with Turbine (it is a stream). |
| `SendMessageUseCase` | Validates the **raw** text: blank → `ChatException.EmptyMessage`, > 1000 → `ChatException.MessageTooLong(max)`; then sends. | AC4, C4. Same limit as the security rules. Validates without trimming, so what is checked is what is sent. |
| `ChatException` | Sealed: `EmptyMessage`, `MessageTooLong(max)`, `ContactBlocked`. | Typed failures the ViewModel can map to states (AC6, AC7). |

---

## Data

| Element | Decision | Why |
|---|---|---|
| `MessageDto` | Default values on every field; `sentAt` annotated `@ServerTimestamp`. | `toObject()` needs a no-arg constructor (project convention). `@ServerTimestamp` lets the server set the time, never the device clock. |
| `ChatRemoteDataSource.observeMessages` | `callbackFlow` around a snapshot listener, ordered by `sentAt` ascending; listener removed in `awaitClose`. | AC2 (oldest first), AC3 (real time). `awaitClose` prevents a leaked listener when the collector stops. |
| Pending messages | Read snapshots with `ServerTimestampBehavior.ESTIMATE`; `isPending = metadata.hasPendingWrites()`. | A local, unconfirmed write has no server time yet; ESTIMATE gives it a local estimate so it sorts at the bottom instead of being `null`. `hasPendingWrites()` is exactly "not confirmed by the server" (AC4, AC5). |
| `sendMessage` | Adds the document and **awaits server confirmation**, returning `Result`. | A failure can be reported (AC6). The UI does not wait on it: the listener shows the message at once (AC4). |
| Permission denied | `PERMISSION_DENIED` from the listener is mapped to `ChatException.ContactBlocked`. | AC7: a refused read means the contact is no longer accepted. |
| Firestore cache | **Unchanged:** memory cache only (`FirestoreModule`, from the Logout plan). | Keeps Logout AC5. Consequence: the offline queue lives in memory only, so AC5 holds while the app runs (spec Q1). |
| `ChatRepositoryModule` | Hilt `@Binds` `ChatRepositoryImpl` → `ChatRepository`. | Constructor injection, interface binding (project convention). |

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

---

## Presentation

| Element | Decision | Why |
|---|---|---|
| `ChatRoute` | `receiverUid`, `receiverName`, `receiverPhotoUrl?`. Read in `ChatViewModel` via `SavedStateHandle`. | AC1. Name and photo passed along to save a Firestore read (accepted trade-off in the spec). |
| `ChatUiState` | `Loading`, `Empty`, `Content(messages)`, `Blocked`, `Error(message)`. | One state per case in AC2, AC7, AC8. |
| State exposure | `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)`, collected with `collectAsStateWithLifecycle()`. | The listener stops when the screen is in the background, saving reads, but survives a rotation (under 5 s) without re-subscribing. |
| Send | Clears the field immediately; failures come back as a one-shot event shown as the "Message not sent." toast. | AC4, AC6. A one-shot event, not a state, so the toast is not shown again after rotation. |
| `MessageUiMapper` | Maps `Message` to `MessageUi` (mine/theirs, formatted time, pending flag). | Keeps formatting out of the ViewModel and the Composable; unit-testable. |
| `ChatScreen` / `ChatContent` | Stateful/stateless split; the input bar is replaced by a notice in `Blocked`. | Project convention; AC7. |
| Entry point | "Message" button on `AttendeeDetailScreen`, shown only when the contact request status is `ACCEPTED`. | AC1. |

### Alternative considered

| Option | Rejected because |
|---|---|
| Re-read the receiver's attendee document in the chat | One extra read per chat open, for data the previous screen already has. |

---

## Coverage

| AC | Covered by |
|---|---|
| AC1 | `AttendeeDetailScreen` button condition, `ChatRoute(receiverUid = authUid)` |
| AC2 | `ChatUiState` (`Loading` / `Empty` / `Content`), `orderBy(sentAt)` |
| AC3 | Snapshot listener in `ChatRemoteDataSource`, `ObserveMessagesUseCase` |
| AC4 | `SendMessageUseCase` validation, `isPending` via `hasPendingWrites()`, field cleared in `ChatViewModel` |
| AC5 | Firestore's in-memory write queue + `isPending` (restart case: spec Q1) |
| AC6 | `sendMessage` returns `Result`; one-shot event → toast |
| AC7 | `PERMISSION_DENIED` → `ChatException.ContactBlocked` → `ChatUiState.Blocked` |
| AC8 | Any other error → `ChatUiState.Error` |
| AC9 | Firestore Security Rules (table above) |
