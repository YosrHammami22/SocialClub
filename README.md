# So_Social

An Android app for browsing and viewing profiles, built as focused practice for
Clean Architecture, testable code, and modern Android tooling — including a
generative AI feature.

## Architecture
- Clean Architecture (data / domain / presentation layers)
- MVVM with unidirectional data flow via StateFlow
- Hilt for dependency injection
- Jetpack Compose with type-safe Navigation

## Tech
- Kotlin, Coroutines, Flow
- Retrofit (RandomUser.me API)
- Room (local persistence)
- Anthropic API — AI-generated icebreaker suggestions based on profile data
- JUnit + kotlinx-coroutines-test for unit testing

## What I focused on
- Testable architecture — every ViewModel and repository depends on interfaces,
  swappable with fakes in unit tests
- Found and fixed a real singleton-scoping bug: a missing `@Singleton` on a
  repository binding caused an in-memory cache to silently not be shared across
  ViewModels
- Accessibility: merged semantics for screen readers, content descriptions on
  interactive elements, and preview variants for dark mode and large font scaling