# SocialClub — Project Memory for Claude

## What this project is
A personal portfolio Android app. The goal is to learn AI-assisted development
and to be able to defend every decision in a technical interview — not just
"it works," but "I can explain why I built it this way." A future phase will
add an iOS version via Kotlin Multiplatform (KMP), so when it's an easy
choice, keep domain/data-layer code reasonably platform-agnostic (avoid
`android.*` imports outside the presentation/DI layers when there's a
straightforward alternative). Don't force this — just don't paint into a
corner for phase 2.

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
- Every public Composable gets `@Preview` coverage: light + dark, plus one
  large-font-scale variant (`fontScale = 2f`) for accessibility.
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

## How I want you to work with me (read this before generating code)
This repo exists so I can defend it in interviews, so don't just hand me
finished solutions by default:

1. When I ask for a new feature or fix, briefly state the approach and
   trade-offs (or ask what I'd try first) before writing code — unless I
   explicitly say "just implement it."
2. After writing non-trivial logic, add a short comment explaining *why*,
   not just *what*.
3. After any meaningful change, list 1-2 interview-style questions someone
   might reasonably ask about that code (edge cases, alternatives
   considered, what breaks at scale).
4. Don't introduce a new library or pattern without a one-line reason it's
   the right tool here — every dependency needs to be justifiable out loud.
