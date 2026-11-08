# Feature: Chat between attendees (1-to-1)

**Status:** Specify ✅ (written after the first implementation of the domain, data and ViewModel layers)
**Next:** `plan.md` (how)

> This file describes **what** Chat must do and **why**.
> It contains no technology (no Firebase, no Room, no ViewModel).
> Technical decisions belong in `plan.md`.

---

## User story

**As an** attendee,
**I want to** exchange text messages in real time with another attendee whose contact request I have accepted (or who accepted mine),
**so that** we can keep talking after meeting at an event.

---

## Constraints

- **C1 — Online-only app (inherited from `specs/logout/spec.md`).** SocialClub only displays data from the server, and actions require a network connection. Chat has **one documented exception** to C1: sending a message offline (AC5).
- **C2 — Accepted contacts only.** Only two attendees whose contact request is accepted can see or send messages to each other.
- **C3 — Short conversations.** A conversation is expected to hold around 20 messages. All messages are shown at once; nothing is loaded page by page.
- **C4 — One message limit everywhere.** A message is 1 to 1000 characters. The app and the server enforce the same limit.

---

## Acceptance criteria

### AC1 — The Message button only exists for accepted contacts
- **Given** I am viewing another attendee's detail screen
- **When** our contact request is accepted
- **Then** a **Message** button is shown
- **When** our contact request is not accepted (none, pending, declined)
- **Then** no **Message** button is shown
- **When** I tap **Message**
- **Then** the chat opens with that attendee, showing their name and photo

> The chat is identified by the attendee's **account**, not by their attendee record: the person on the other side is the one who signed in.

### AC2 — The chat shows its loading, empty and filled states
- **Given** I have opened a chat
- **Then** a loading indicator is shown first
- **And then** either an empty state (no message yet), or the messages
- **And** messages are ordered oldest at the top, newest at the bottom

### AC3 — New messages appear in real time
- **Given** I have a chat open
- **When** the other attendee sends me a message
- **Then** it appears without any refresh action from me

### AC4 — Sending a message
- **Given** I have a chat open
- **When** the text field is blank (empty or only spaces)
- **Then** **Send** is disabled
- **When** the text is longer than 1000 characters
- **Then** the message cannot be sent
- **When** I tap **Send** with a valid text
- **Then** the text field is cleared immediately
- **And** the message appears at once with a **"sending"** indicator
- **And** the indicator disappears once the server has confirmed the message

> The text is validated and sent **exactly as typed**: it is not trimmed. What the user sees in the field is what is checked and what the other attendee receives.

### AC5 — Sending a message while offline
- **Given** I have a chat open and my device has no network connection
- **When** I send a message
- **Then** it appears with the **"sending"** indicator
- **And** it is delivered when the network comes back, while the app is still running

> Exception to C1: a chat is expected to behave like a messaging app. Losing a message because the
> signal dropped for a few seconds would be a worse experience than a short delay.
> **Open question:** delivery after the app is closed and reopened (see Open questions, Q1).

### AC6 — A failed send is reported
- **Given** I have sent a message
- **When** the server rejects it or the send fails
- **Then** a **"Message not sent."** toast is shown

### AC7 — A chat that is no longer allowed shows the Blocked state
- **Given** I open a chat (or have one open)
- **When** access is refused because our contact is no longer accepted or has been blocked
- **Then** no messages are shown
- **And** a notice replaces the input bar, so I can't type a new message

### AC8 — Any other loading error shows the Error state
- **Given** I open a chat
- **When** loading the messages fails for any reason other than AC7
- **Then** the Error state is shown

### AC9 — Only the two participants can use the chat (server side)
The server must enforce, whatever app sends the request:
- only the two participants of an **accepted** contact can read or create messages in their chat
- a message's sender is always the person sending it (nobody can send on someone else's behalf)
- the text is 1 to 1000 characters (C4)
- a message's time is the server's time, never the device's
- a message has no other information than its sender, text and time
- a message can't be edited or deleted from the app

> Why: the app's own checks (AC1, AC4) can be bypassed by a modified client. The server is the only
> place where C2 and C4 are actually guaranteed.

---

## Decisions

| Question | Decision | Reason |
|---|---|---|
| Who can chat? | Only attendees with an **accepted** contact request (C2). | Avoids unsolicited messages; the contact request is the consent step. |
| Where do messages sit relative to contact requests? | In a **separate conversation**, linked to the contact by the same pair of people. | A contact request and a conversation have different lifetimes and access rules; keeping them apart keeps each simple. The link lets the server check the contact status (AC9). |
| Whose clock dates a message? | The **server's**. | Device clocks can be wrong or changed; ordering (AC2) must be the same for both participants. |
| Does the user wait for the server after tapping Send? | **No.** The message appears at once as "sending" (AC4). | Feels instant; the "sending" indicator is honest about the state. |
| Show the receiver's name and photo how? | Passed in when the chat is opened, not reloaded. | Saves a server read. **Trade-off:** if the receiver changes their name or photo while the chat is open, the old ones stay until it is reopened. |
| Where is the 1000-character limit checked? | **Both** in the app and on the server, same limit (C4). | The app gives immediate feedback; the server is the real guarantee (AC9). |
| Trim spaces before sending? | **No.** Raw text is validated and sent (AC4). | What is checked must be what is sent; trimming in one place and not the other would let the two checks disagree. |
| Messages while offline? | Queued and sent later (AC5). | Documented exception to C1. |

---

## Open questions

### Q1 — Delivery of an offline message after the app restarts (AC5)
The original request said an offline message is also delivered **after the app is closed and reopened**,
with the queue kept on the device. This **conflicts with a Logout decision**: `specs/logout/plan.md`
stores nothing on the device's disk, so the previous user's data never stays on a shared phone
(Logout AC5). With that decision, a message still "sending" when the app is closed is lost.

| Option | Trade-off |
|---|---|
| Keep Logout's decision: delivery only while the app runs | Simple and keeps Logout AC5 intact. A message sent offline just before closing the app is lost, silently. |
| Store the queue on disk | Meets the original request. Reopens Logout AC5: the previous user's data would stay on disk after logout, so Logout's plan must change first. |

**Not decided.** AC5 currently covers delivery while the app is running only.

### Q2 — Messages still "sending" when the user logs out
Logout's decision table says nothing can be pending at logout ("C1: the app has no offline mode"). AC5 makes that
no longer true: a message can still be "sending" when the user taps Logout. The expected result (sent anyway,
dropped, or logout delayed) is not specified. This also needs a matching update to `specs/logout/spec.md`.

**Not decided.**

---

## Out of scope

- A list of all conversations (with last message and time).
- An unread message counter.
- Images, reactions, read receipts.
- Push notifications.
- Loading older messages page by page (C3).

---

## Follow-ups (separate specs)

- **Blocking:** a blocked status that records who blocked; either participant can block; blocking deletes all the conversation's messages (safe to run twice); contact request rules updated to match.
- **Conversation list:** last message and last update time per conversation.
- **Translated error messages:** errors carried as identifiers, not as ready-made English text.
