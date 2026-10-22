# Feature: Logout

**Status:** Specify ✅ · Clarify ✅
**Next:** `plan.md` (how) · `tasks.md` (test-first steps)

> This file describes **what** Logout must do and **why**.
> It contains no technology (no Firebase, no Room, no ViewModel).
> Technical decisions belong in `plan.md`.

---

## User story

**As a** SocialClub user,
**I want to** log out,
**so that** no one can access the app on my device until my email and password are entered again.

---

## Constraints

- **C1 — Online-only app.** SocialClub only displays data from the server. Actions such as sending a contact request require a network connection and fail immediately without one. Nothing is queued for later.

---

## Acceptance criteria

### AC1 — Logout leads to the login screen, with no way back
- **Given** I am logged in and on the Settings screen
- **When** I tap **Logout**
- **Then** the Logout button is disabled and a loading indicator is shown
- **And** the login screen appears
- **And** pressing **Back** on the login screen closes the app (it never returns to Settings)

### AC2 — Logout works offline
- **Given** I am logged in and my device has no network connection
- **When** I tap **Logout**
- **Then** logout still completes and the login screen appears

> Exception to C1: logout only clears things on the device, it does not need the server.
> A user without signal must still be able to log out of a shared phone.

### AC3 — Reopening the app after logout
- **Given** I have logged out
- **When** I close and reopen the app
- **Then** the login screen appears

### AC4 — The login form is empty
- **Given** I have logged out
- **When** the login screen appears
- **Then** the email and password fields are empty (nothing from the previous user is pre-filled)

### AC5 — The next user sees nothing of the previous user
- **Given** Alice has logged out
- **When** Bob logs in on the same device
- **Then** Bob sees none of Alice's personal data:
  - her contacts
  - the contact requests she sent
  - the contact requests she received

### AC6 — A deliberate logout shows no error
- **Given** I am logged in
- **When** I tap **Logout**
- **Then** no error message is shown (in particular, no "session expired" message)

### AC7 — Opening the app while logged out shows no error
- **Given** nobody is logged in on the device
- **When** I open the app
- **Then** the login screen appears with no error message

> AC7 was discovered while writing the tests (Step 5), not during Clarify.
> When implementation exposes a gap, the spec is updated first.

---

## Decisions (from Clarify)

| Question | Decision | Reason |
|---|---|---|
| Ask for confirmation before logout? | **No.** One tap logs out. | Less friction; logout is reversible (the user can log in again). |
| Can logout fail? | **No**, not from the user's point of view. It always ends on the login screen. | Logout only clears the device (AC2), so there is no server step that can fail. |
| User taps Logout twice quickly? | The button is **disabled** with a loader until the login screen appears. | Prevents two logouts / two navigations. |
| Back button on the login screen? | **Closes the app.** | For a logged-out user there is no screen before login; any other screen would be private. |
| Which data is "the user's data"? | **Everything** the user sees: contacts, sent requests, received requests. | Users cannot post public content in SocialClub, so all of it is personal. |
| Changes not yet sent to the server when logging out? | **Not applicable.** | C1: the app has no offline mode, so nothing can be pending. |
| Logout vs. session expired? | The app must tell them apart: a deliberate logout shows no message, an expired session shows "session expired". | Same result (user logged out), different reason, different message. |

---

## Out of scope

- Deleting the account.
- Logging out from all devices at once.
- An offline mode for the rest of the app (see C1).
