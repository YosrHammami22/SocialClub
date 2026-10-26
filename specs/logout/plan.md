# Plan: Logout

**Status:** Plan ✅
**Spec:** [`spec.md`](./spec.md) · **Tasks:** [`tasks.md`](./tasks.md)

> This file describes **how** the spec is built in SocialClub's architecture.
> Every decision points back to an acceptance criterion (AC) or constraint (C) in `spec.md`.

---

## Overview

```
SettingsScreen ──tap──▶ SettingsViewModel ──▶ LogoutUseCase ──▶ AuthRepository (interface)
   (Idle / LoggingOut)                                               │
                                                                     ▼
                                                            AuthRepositoryImpl
                                                     1. DeliberateLogoutFlag.markDeliberate()
                                                     2. SessionManager.clear()
                                                     3. AuthRemoteDataSource.signOut()   ← LAST

SessionManager.currentAttendee becomes null
        │
        ▼
Auth observer (app level) ──▶ AuthNavigationDecider.decide(...)
        │                          ├─ deliberate  → Home, no message        (AC6)
        │                          ├─ not deliberate → Home + "session expired" snackbar
        │                          └─ never logged in → nothing             (AC7)
        ▼
navigateToHomeClearingBackStack()  → Home is the only screen left           (AC1)
```

---

## Domain

| Element | Decision | Why |
|---|---|---|
| `AuthRepository` | Interface with `suspend fun logout()`. No Firebase types. | The domain stays free of Android/Firebase: testable with plain mocks, shareable with iOS through KMP later. |
| `LogoutUseCase` | `suspend operator fun invoke()` delegates to `AuthRepository.logout()`. Returns `Unit`. | Spec: logout cannot fail from the user's point of view, so a `Result` would carry no information. One place for logout, reusable by other flows. |

---

## Data

| Element | Decision | Why |
|---|---|---|
| `AuthRepositoryImpl.logout()` | Order: **mark deliberate → clear local data → sign out last.** | Signing out last means there is never a moment where the app thinks the user is logged out while their data is still on the device (AC5). Marking first lets the observer know the reason (AC6). |
| `DeliberateLogoutFlag` | `markDeliberate()` / `consume()`. `consume()` returns `true` once, then resets. | Tells a deliberate logout apart from an expired session. Reset is essential: otherwise a later real expiry would show no message. |
| `SessionManager.clear()` | Sets `currentAttendee` to `null`. Called **from the repository**, not a ViewModel. | Drops the current user from memory; observers react to it (AC5). Documented exception to the project rule "session state is set by ViewModels" (see `CLAUDE.md`): logout is ordered steps that the session-expired flow must reuse. |
| `AuthRemoteDataSource` | **Interface** with `fun signOut()`; the implementation wraps `firebaseAuth.signOut()`. Local only, no network. | AC2: works offline. An interface so tests can mock it (project rule: mock interfaces, never concrete classes). |
| Firestore | **Disk persistence disabled** (memory cache only), set in `FirestoreModule.provideFirestoreSettings()`. | C1: the app is online-only, so a disk cache brings no value and would keep the previous user's documents on the device (AC5). |

### Alternatives considered for Firestore

| Option | Rejected because |
|---|---|
| Keep persistence, call `clearPersistence()` on logout | It only runs on a terminated instance; after `terminate()` the singleton `FirebaseFirestore` provided by Hilt can't be reused. Too much complexity for an online-only app. |
| Keep persistence, rely only on user-scoped queries | Leaves the previous user's documents on disk. |

**Accepted trade-offs:** every screen loads from the network (slower, more Firestore reads). The memory cache lives until the process ends; this is acceptable because every query is scoped to the current user and protected by Security Rules.

---

## Presentation

| Element | Decision | Why |
|---|---|---|
| `SettingsViewModel` | Exposes `StateFlow<SettingsUiState>` with `Idle` and `LoggingOut`. A tap while `LoggingOut` is ignored. | AC1: button disabled + loader, no double logout. |
| No "done" state | The ViewModel does **not** navigate. | Navigation to SignIn lives in one place (the auth observer) for every reason a user gets logged out. |
| `SettingsContent` | Stateless composable `(uiState, onLogoutClick, onBackClick: (() -> Unit)? = null)`, with test tags `SettingsTestTags.LOGOUT_BUTTON` / `LOGOUT_LOADER`. | Testable UI without ViewModel or Hilt. `onBackClick` is optional so the existing T6 calls stay unchanged. |
| Top bar | `AppTopBar` with the Settings title. Its back button gets `onBackClick` in `Idle` and `null` (no button) in `LoggingOut`. `AppNavHost` wires it to `popBackStack()`. | AC8: popping Settings destroys `SettingsViewModel`, cancelling `viewModelScope` mid-logout — possibly between `SessionManager.clear()` and `signOut()`, breaking the order above (AC5). |
| Entry point | A settings icon on `AttendeeScreen` (the current attendee's screen, first screen after login) opens `SettingsRoute`. | Spec says "on the Settings screen" but not how to reach it; decided during Implement. |
| `HomeViewModel` / `SignInViewModel` | Initial state: empty email (Home) and empty password (SignIn). | AC4. Two new tests added to the existing test classes; existing tests untouched. |

---

## Navigation

| Element | Decision | Why |
|---|---|---|
| Auth observer | Observes `currentAttendee`, remembers whether the user **was** logged in, and asks `AuthNavigationDecider`. | One place decides what happens when the user becomes `null`. |
| `AuthNavigationDecider.decide(wasLoggedIn, isLoggedIn, wasDeliberateLogout)` | Pure function returning `AuthNavigation.None` or `AuthNavigation.ToSignIn(showSessionExpired)`. | The decision is unit-testable without Android. `wasLoggedIn` prevents a false "session expired" at app start (AC7). |
| Login screen = `HomeRoute` | Logout lands on Home (the email step), not `SignInRoute`. | `SignInRoute(email)` needs an email; passing the previous user's would break AC4. Home is already the start destination, so AC3 needs no change. Decided during Implement. |
| `NavController.navigateToHomeClearingBackStack()` | `navigate(HomeRoute) { popUpTo(graph.id) { inclusive = true }; launchSingleTop = true }` | AC1: Home is the only screen, so Back closes the app with no special code. Removed screens' ViewModels are destroyed, dropping the previous user's data from memory (AC5). |
| `NavController.navigateToAttendeeClearingBackStack(email)` | `navigate(AttendeeRoute(email)) { popUpTo(graph.id) { inclusive = true }; launchSingleTop = true }`, used by all three ways in (Home, SignIn, CreatePassword). | AC9: the attendee screen is the only screen, so Back closes the app with no special code — same technique as AC1. Home/SignIn/CreatePassword ViewModels are destroyed, so no typed email or password stays in memory. Replaces the earlier `popUpTo<SignInRoute>` / `popUpTo<CreatePasswordRoute>`, which kept Home under the attendee screen. |
| "Session expired" message | Snackbar from an app-level `SnackbarHostState`, shown after navigating. | Survives the navigation (it isn't owned by a destroyed screen). Known gap: nothing ends a session without a logout yet, so this branch has no trigger until one exists. |

### Alternative considered

| Option | Rejected because |
|---|---|
| Intercept Back on SignIn and close the app | A patch: old screens stay on the stack and their ViewModels keep the previous user's data in memory. |

### Open question (AC8 known gap)

The system Back gesture can still pop Settings mid-logout. Options, not decided yet:

| Option | Trade-off |
|---|---|
| `BackHandler(enabled = isLoggingOut) {}` in `SettingsContent` | Small, local. Still ties logout's completion to the screen's lifetime. |
| Run `logout()` in an app-level `CoroutineScope` (e.g. in `AuthRepositoryImpl`) | Logout can never be cut short, whatever the UI does. Adds an injected scope. |

---

## Coverage

| AC | Covered by |
|---|---|
| AC1 | `SettingsViewModel` states, `SettingsContent`, back stack clearing |
| AC2 | Logout is local only |
| AC3 | Start destination when logged out |
| AC4 | `HomeViewModel` + `SignInViewModel` initial state |
| AC5 | Clearing order, `SessionManager.clear()`, Firestore memory-only cache, back stack clearing |
| AC6 | `DeliberateLogoutFlag` + `AuthNavigationDecider` |
| AC7 | `AuthNavigationDecider` (`wasLoggedIn`) |
| AC8 | `SettingsContent` top bar (T12) |
| AC9 | Back stack clearing on the way in (T13) |

**Accepted trade-offs (AC9):** the "Get from API" demo is only reachable from Home, so it is out of reach once identified.
Reopening the app while still signed in starts on Home, not the attendee screen — a separate gap, not covered by AC9.
