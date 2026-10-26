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

> **"The login screen"** below means the first step of signing in: the screen where the user enters their email.
> (Signing in has two steps: email, then password.)

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
- **Then** the email field is empty, and so is the password field on the next step (nothing from the previous user is pre-filled)

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

### AC8 — Settings can't be left from its Back button while logging out
- **Given** I am on the Settings screen
- **When** no logout is in progress
- **Then** a **Back** button is shown at the top of the screen and returns to the previous screen
- **When** I have tapped **Logout** and it is still in progress
- **Then** that **Back** button is not shown

> Why: leaving Settings mid-logout could stop the logout halfway, before it finishes.
> AC8 was discovered when the top bar was added to Settings, after Implement.
> **Known gap:** the device's own Back gesture is not covered yet; it needs its own decision.

### AC9 — Back on the attendee screen closes the app
- **Given** I have just been identified (email only, email + password, or first-time password creation) and I am on my attendee screen
- **When** I press **Back**
- **Then** the app closes (it never returns to the login screen or the password screen)

> Why: once the user is identified, there is no reason to go back and submit another email.
> The mirror of AC1: logged out, Back can't reach private screens; logged in, Back can't reach the login screen.
> AC9 was discovered after Implement.

---

## Decisions (from Clarify)

| Question | Decision | Reason |
|---|---|---|
| Ask for confirmation before logout? | **No.** One tap logs out. | Less friction; logout is reversible (the user can log in again). |
| Can logout fail? | **No**, not from the user's point of view. It always ends on the login screen. | Logout only clears the device (AC2), so there is no server step that can fail. |
| User taps Logout twice quickly? | The button is **disabled** with a loader until the login screen appears. | Prevents two logouts / two navigations. |
| Back button on the login screen? | **Closes the app.** | For a logged-out user there is no screen before login; any other screen would be private. |
| Back button on the attendee screen? | **Closes the app.** | Once identified, there is no reason to go back and submit another email (AC9). Switching user goes through Logout. |
| Which data is "the user's data"? | **Everything** the user sees: contacts, sent requests, received requests. | Users cannot post public content in SocialClub, so all of it is personal. |
| Changes not yet sent to the server when logging out? | **Not applicable.** | C1: the app has no offline mode, so nothing can be pending. |
| Logout vs. session expired? | The app must tell them apart: a deliberate logout shows no message, an expired session shows "session expired". | Same result (user logged out), different reason, different message. |

---

## Out of scope

- Deleting the account.
- Logging out from all devices at once.
- An offline mode for the rest of the app (see C1).
