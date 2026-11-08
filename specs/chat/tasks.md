# Tasks: Chat between attendees (1-to-1)

**Status:** Tasks ✅ (retroactive) · Implement ⏳ (known gaps below)
**Spec:** [`spec.md`](./spec.md) · **Plan:** [`plan.md`](./plan.md)

> **Retroactive list.** The domain, data and ViewModel layers were built **before** the spec
> (commits `56e47f9`…`0f1653a`), so the checked tasks below record what exists and which test
> covers it. They were **not** necessarily done test-first. Every unchecked task follows the normal
> loop below.

## How to work through this list

For **each** open task, in order:

1. 🔴 **Red** — run the task's test. It fails (often it doesn't even compile yet).
2. 🟢 **Green** — write the **minimum** code to make it pass. **Never edit the test to make it pass.**
3. 🔵 **Refactor** — clean up, tests stay green.
4. ✅ **Prove it** — temporarily break the code and check the test turns red. Then restore.
5. Tick the box below.

---

## Done — unit tests (`app/src/test/...`)

- [x] **T1 · Domain · AC9 (pair), AC1**
  `pairIdOf(a, b)`: same ID in either order; rejects blank UIDs, `_` in a UID, and two equal UIDs.
  Test: `domain/model/PairIdTest.kt`

- [x] **T2 · Domain · AC4, C4**
  `SendMessageUseCase`: blank → `EmptyMessage`; > 1000 raw chars → `MessageTooLong` (`max` checked), including
  999 chars + 2 spaces; exactly 1000 → sent; no signed-in user → `NoSignedInUser`; receiver = self →
  `IllegalArgumentException`; raw (untrimmed) text sent once to the pair conversation; repository failure returned as is.
  Test: `domain/usecase/SendMessageUseCaseTest.kt`

- [x] **T3 · Domain · AC2, AC3**
  `ObserveMessagesUseCase`: observes the pair conversation and forwards lists unchanged; no signed-in user →
  fails without calling the repository; repository error propagated.
  Test: `domain/usecase/ObserveMessagesUseCaseTest.kt` (Turbine)

- [x] **T4 · Domain · AC7**
  `ChatException.ContactBlocked` keeps the original error as its cause; other exceptions unchanged.
  Test: `domain/model/ChatExceptionTest.kt`

- [x] **T5 · Presentation · AC2, AC4**
  `Message.toUi()`: `isMine` (same UID / other UID / no current user), `"HH:mm"` in the given time zone,
  `isPending` forwarded.
  Test: `ui/chat/MessageUiMapperTest.kt` (fixed `ZoneId`)

- [x] **T6 · Presentation · AC2, AC3, AC7, AC8**
  `ChatViewModel.uiState`: `Loading` first; empty list → `Empty`; messages → `Content`; `Empty` → `Content` on a
  new message; `ContactBlocked` → `Blocked`; any other error → `Error("Couldn't load this conversation.")`.
  Test: `ui/chat/ChatViewModelTest.kt` (a `backgroundScope` collector keeps `WhileSubscribed` running)

- [x] **T7 · Presentation · AC4, AC6**
  `ChatViewModel.onSend()`: sends the raw draft once to the route's receiver; clears the draft before the send
  completes; failure → `errorEvent` emits `"Message not sent."`; success → no event.
  Test: `ui/chat/ChatViewModelTest.kt`

## Done — code without a unit test

- [x] **T8 · Data · AC2, AC3, AC4, AC5, AC7**
  `MessageDto` (`@ServerTimestamp`), `toMessage()` (ESTIMATE + `hasPendingWrites`), `ChatRemoteDataSource`
  (`callbackFlow`, `MetadataChanges.INCLUDE`, `PERMISSION_DENIED` → `ContactBlocked(cause)`, `add().await()`),
  `ChatRepositoryImpl`, `ChatRepositoryModule`.
  No test: `ChatRemoteDataSource` is a concrete class around Firestore (see T14).

- [x] **T9 · UI · AC1, AC2, AC4, AC7, AC8**
  `ChatScreen` / `ChatContent` (all states, input bar, blocked notice, pending spinner), "Message" button on
  `AttendeeDetailScreen` (only in `Accepted`), `ChatRoute` + `AppNavHost` wiring.
  Checked through `@ThemePreviews` only.

---

## Open — known gaps in V1

- [ ] **T10 · UI · AC6**
  `ChatScreen` collects `viewModel.errorEvent` and shows the "Message not sent." toast (same pattern as
  `AttendeeDetailScreen`). Today the event is emitted but never shown.

- [ ] **T11 · Spec, then UI · AC4**
  A draft over 1000 characters is cleared on Send and then refused, so the text is lost. **Spec first:** reword
  AC4 (e.g. "Send is disabled above 1000 characters; the text stays in the field"), then implement test-first.

- [ ] **T12 · Spec · AC5 (Q1)**
  Decide offline delivery after an app restart vs Logout's memory-only Firestore cache. See `spec.md` Q1.

- [ ] **T13 · Spec · Logout ↔ Chat (Q2)**
  Decide what happens to messages still "sending" at logout, and update `specs/logout/spec.md` to match.

- [ ] **T14 · Data · test coverage**
  `ChatRemoteDataSource` open point (`plan.md`): extract an interface and/or pass `senderUid` from the use case,
  then add `ChatRepositoryImpl` tests.

- [ ] **T15 · UI test · AC1**
  Compose test: the Message button exists only in `Accepted`, and tapping it passes the attendee's `authUid`.

## Open — follow-ups (separate specs)

- [ ] **T16 · Security rules · AC9**
  Copy the deployed rules into `firestore.rules`, add `firebase.json`, deploy with the Firebase CLI
  (`firebase deploy --only firestore:rules`), and test them against the emulator.

- [ ] **T17 · Blocking**
  `BLOCKED` status + `blockedBy`; either participant can block; Cloud Function deleting all messages (idempotent);
  `contactRequests` rules updated. Needs its own `specs/blocking/`.

- [ ] **T18 · Conversation list**
  `lastMessage` / `updatedAt` on the `conversations/{pairId}` parent document. Needs its own spec.

- [ ] **T19 · Localized errors**
  Errors as `@StringRes` or an error enum instead of English `String`s in `ChatUiState.Error` and `errorEvent`.
