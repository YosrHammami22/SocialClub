# SocialClub — Project Memory for Claude

## What this project is
A personal Android portfolio app. The goal is deliberate, well-reasoned
engineering: decisions should be documented and defensible, not just
functional. A future phase will add an iOS version via Kotlin Multiplatform
(KMP), so when it's an easy choice, keep domain/data-layer code reasonably
platform-agnostic (avoid `android.*` imports outside the presentation/DI
layers when there's a straightforward alternative). Don't force this — just
don't paint into a corner for phase 2.

GitHub: https://github.com/YosrHammami22/SocialClub (develop branch is active work)

## Architecture
- Single Gradle module: `:app` (will likely need modularization before the
  KMP phase — flag this if it comes up naturally, don't do it preemptively).
- Clean Architecture: data / domain / presentation.
- MVVM with unidirectional data flow: ViewModel exposes StateFlow, the
  Compose UI collects it with `collectAsStateWithLifecycle`.
- DI: Hilt. ViewModels and Repositories are built around interfaces
  (e.g. `UserRepository` interface + `UserRepositoryImpl`) specifically so
  they can be swapped for fakes/mocks in tests.
- Networking: Retrofit + GSON. Local storage: Room. Backend: Firebase
  (Firestore + Auth).
- Images: Coil. Serialization: kotlinx.serialization where GSON isn't
  already the established choice for that layer.
- min SDK 24, target/compile SDK 34.

## Compose conventions
- Every public Composable gets preview coverage using the shared
  `@ThemePreviews` multi-preview annotation (light, dark, large font 200%,
  dark + large font) — don't hand-write four separate `@Preview` blocks,
  use the annotation.
- Feed realistic fake data via a preview parameter provider or hardcoded
  sample values — never empty strings or placeholder text.
- Merged semantics + descriptive content descriptions on interactive
  elements — this app is meant to demonstrate accessibility awareness, not
  just pass a quick glance.
- State hoisting: Composables take state + event lambdas; only the screen
  root reaches into the ViewModel directly.

## Testing conventions
- JUnit4 + MockK + Turbine + kotlinx-coroutines-test.
- Mock interfaces only, never concrete classes.
- Flow/StateFlow assertions go through Turbine's `test { }` block.
- Test naming: `` `given X when Y then Z`() ``.
- Every ViewModel and UseCase gets: initial state, success path, error path,
  and at least one edge case specific to its own logic.

## How I want you to work with me
1. Before writing non-trivial logic, briefly state the approach and
   trade-offs (or ask what I'd try first) rather than defaulting straight
   to a full solution — unless I explicitly say "just implement it."
2. After writing non-trivial logic, add a short comment explaining *why*,
   not just *what*.
3. After a meaningful change, call out 1-2 edge cases or trade-offs worth
   discussing in review.
4. Don't introduce a new library or pattern without a one-line reason it's
   the right tool here — every dependency should be justifiable.
