# Tasks: Logout

**Status:** Tasks ✅ · Implement ⏳
**Spec:** [`spec.md`](./spec.md) · **Plan:** [`plan.md`](./plan.md)

## How to work through this list

For **each** task, in order:

1. 🔴 **Red** — run the task's test. It fails (often it doesn't even compile yet).
2. 🟢 **Green** — write the **minimum** code to make it pass. **Never edit the test to make it pass.**
3. 🔵 **Refactor** — clean up, tests stay green.
4. ✅ **Prove it** — temporarily break the code and check the test turns red. Then restore.
5. Tick the box below.

Order is **inside-out**: domain (fastest, no Android) → data → presentation → navigation → slow tests → manual checks.

---

## Unit tests (`app/src/test/...`)

- [x] **T1 · Domain · AC1**
  `LogoutUseCase()` calls `AuthRepository.logout()` exactly once.
  Test: `domain/usecase/LogoutUseCaseTest.kt`

- [x] **T2 · Data · AC5, AC6**
  `AuthRepositoryImpl.logout()` runs in order: `markDeliberate()` → `SessionManager.clear()` → `signOut()`.
  Test: `data/repository/AuthRepositoryImplTest.kt` (real `SessionManager` and flag; only `AuthRemoteDataSource` is mocked, and its `signOut()` records the state at the moment it's called)

- [x] **T3 · Data · AC5**
  `SessionManager.clear()` sets `currentAttendee` to `null`.
  Test: `data/session/SessionManagerTest.kt`

- [x] **T4 · Data · AC6**
  `DeliberateLogoutFlag.consume()` returns `true` once after `markDeliberate()`, then `false`.
  Test: `data/session/DeliberateLogoutFlagTest.kt`

- [x] **T5 · Presentation · AC1**
  `SettingsViewModel.onLogoutClick()` emits `LoggingOut`; a second tap while logging out does not call the use case again.
  Test: `ui/settings/SettingsViewModelTest.kt` (`CompletableDeferred` gate, assertions on `uiState.value`)

- [x] **T7 · Navigation · AC6, AC7**
  `AuthNavigationDecider.decide(...)`: deliberate → SignIn, no message; not deliberate → SignIn + "session expired"; never logged in → nothing; still logged in → nothing.
  Test: `ui/navigation/AuthNavigationDeciderTest.kt`

- [ ] **T10 · Presentation · AC4**
  `SignInViewModel` starts with empty email and password.
  Test: `ui/signIn/SignInViewModelTest.kt`

## Instrumented tests (`app/src/androidTest/...`)

- [ ] **T6 · Presentation (UI) · AC1**
  `SettingsContent` in `LoggingOut`: button disabled, loader visible. In `Idle`: button enabled, no loader.
  Test: `ui/settings/SettingsContentTest.kt`

- [ ] **T8 · Navigation · AC1, AC5**
  After `navigateToSignInClearingBackStack()`, SignIn is the only screen on the back stack.
  Test: `ui/navigation/LogoutNavigationTest.kt` (`TestNavHostController`)

- [ ] **T9 · Config · AC5, C1**
  `FirestoreModule.provideFirestoreSettings()` uses a memory-only cache.
  Test: `di/FirestoreSettingsTest.kt`

## Wiring (no dedicated test; covered by T11)

- [ ] **W1** — Hilt: bind `AuthRepository` → `AuthRepositoryImpl`; provide `DeliberateLogoutFlag` and `SessionManager` as `@Singleton`; apply the Firestore settings.
- [ ] **W2** — App-level auth observer: collect `currentAttendee`, call `AuthNavigationDecider`, navigate and show the message.
- [ ] **W3** — Settings screen: Logout button calls `SettingsViewModel.onLogoutClick()`.

## Manual checks

- [ ] **T11 · AC2, AC3, AC7**
  - [ ] Airplane mode on → tap Logout → SignIn appears (AC2)
  - [ ] After logout, kill the app and reopen it → SignIn appears (AC3)
  - [ ] Fresh install or logged out → open the app → no error message (AC7)
  - [ ] Log in as user A, log out, log in as user B → nothing from A is visible (AC5, end to end)

---

## Dependencies to add (if missing)

- `testImplementation("io.mockk:mockk:<version>")`
- `androidTestImplementation("androidx.navigation:navigation-testing:<version>")`
