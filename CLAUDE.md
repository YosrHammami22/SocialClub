# SocialClub — Project Memory for Claude

## What this project is
A personal Android portfolio app (event/people-matching: attendees register for
events via a Firestore-backed system, look up their registrations by email, and
browse/view other attendees at the same event). The goal is deliberate,
well-reasoned engineering: decisions should be documented and defensible, not
just functional. A future phase may add an iOS version via Kotlin Multiplatform
(KMP), so when it's an easy choice, keep domain/data-layer code reasonably
platform-agnostic (avoid `android.*` imports outside the presentation/DI layers
when there's a straightforward alternative). Don't force this — just don't
paint into a corner for phase 2.

GitHub: https://github.com/YosrHammami22/SocialClub (`develop` branch is active
work; `main`/`develop`/`feature/*` branching, currently on a secondary GitHub
account due to an unresolved account-access issue on the original one).

## Two coexisting data sources — don't conflate them
1. **RandomUser.me REST API** (`PeopleListScreen`/`PersonDetailScreen`) — a
   separate demo feature (list/detail of random fake people), unrelated to the
   event/attendee system. Retrofit + GSON, its own DTOs/mapper (`PersonDto` →
   `Person`).
2. **Firestore** (`Attendee`, `Event`, `Registration`) — the real app logic.
   Three collections: `attendees` (doc ID = the attendee's own ID), `events`,
   `registrations` (links the other two via `personId`/`eventId` reference
   fields, not embedded data).

Naming convention, used consistently: **`currentAttendee`** = the logged-in
user (whoever submitted their email on Home); **`guestAttendee`** = any other
attendee being viewed (event's guest list, or their detail screen).

## Architecture
- Single Gradle module: `:app` (will likely need modularization before a KMP
  phase — flag this if it comes up naturally, don't do it preemptively).
- Clean Architecture: `data` / `domain` / `presentation` (package name is
  `ui`, historically, not `presentation` — don't rename without a reason).
- MVVM, unidirectional data flow: ViewModel exposes `StateFlow`, Compose
  collects with `collectAsStateWithLifecycle`.
- DI: Hilt, constructor injection everywhere (never field injection, except
  `MainActivity`, which Android itself constructs). Repositories are built
  around interfaces (e.g. `AttendeeRepository` + `AttendeeRepositoryImpl`,
  `EventRepository`, `RegistrationRepository`) specifically so they can be
  swapped for mocks in tests. `@Singleton` scope matters wherever state must
  be shared across ViewModels — a real bug happened when it was missing on a
  repository binding, causing a cache to silently not be shared between the
  list and detail screens; found via targeted logging, not guesswork.
- Most ViewModels read their route argument via `SavedStateHandle` +
  trigger loading in `init{}` (not `LaunchedEffect` in the Composable) —
  this is deliberate: `init{}` runs exactly once per ViewModel instance, so
  it's inherently rotation-safe with no extra guard needed. A couple of
  ViewModels still use the more "explicit/traceable" pattern (parameter +
  `LaunchedEffect` + a manual `if (alreadyLoaded) return` guard) where that
  was chosen deliberately for traceability — both patterns are intentional,
  not inconsistency.
- **`SessionManager`** (`@Singleton`, no interface needed) holds
  `currentAttendee: StateFlow<Attendee?>` — the one piece of genuinely
  app-wide state. Any ViewModel injects it directly rather than threading
  the current user's ID through navigation route arguments. This was a
  deliberate simplification after first trying to share it via navigation
  route arguments and via a shared-ViewModel-across-back-stack-entries
  technique — both work, but were more ceremony than this app-wide need
  justified.
- Setting session state is a ViewModel responsibility, not a use case's —
  use cases stay narrowly about fetching/combining data; "this fetch means
  the user is now logged in" is a presentation-layer interpretation of that
  data, not an inherent property of fetching it.
- Navigation: **Jetpack Navigation Compose (Nav2)**, not Nav3 — type-safe
  routes via `@Serializable` objects/data classes (Nav2 2.8+), not string
  routes. Deliberate choice given Nav3's bigger conceptual shift and this
  project's timeline; awareness of Nav3 exists but no migration planned.
- Networking: Retrofit + GSON for RandomUser; a **separate** Retrofit
  instance + custom `@Qualifier` (`@AnthropicRetrofit`) for the Anthropic
  API (the Icebreaker AI feature), since the two need different base URLs.
- Backend: Firebase (Firestore + Auth planned, not yet wired for real
  sign-in — currently email-lookup-based, no password/session token yet).
- Local storage: Room was scaffolded for a "favorites" feature, then that
  feature was cut for scope reasons — don't assume Room is in active use
  unless a specific screen calls for it again.
- Images: Coil (`AsyncImage`), with `AppAvatar` as the standard wrapper
  (photo if present, else a colored initial-letter fallback — no gendered
  placeholder illustrations currently, that was intentionally dropped when
  `AppAvatar` was introduced).
- Serialization: kotlinx.serialization for Nav routes; GSON for
  Retrofit/RandomUser DTOs; Firestore's own `toObject()`/`toObjects()` for
  Firestore DTOs (which require a no-arg constructor, hence default values
  on every Firestore DTO field).
- Logging: an injected `Logger` interface (`AndroidLogger` in production,
  `FakeLogger` in tests) — never a static `AppLog` object directly in a
  class under test; this was a deliberate refactor after a static logger
  caused "Log not mocked" crashes in JVM unit tests.
- min SDK 24, target/compile SDK 34/35.

## Design system (`ui/theme/`, `ui/components/`)
- Color: Material 3 tonal palette generated via the Material Theme Builder
  (warm amber/coral seed, not a generic blue/purple) — `dynamicColor` is
  explicitly set to `false` in `SocialClubTheme`, since Android 12+'s
  wallpaper-derived dynamic color would otherwise silently override the
  deliberate palette.
- Typography: two bundled Google Fonts (not downloadable-fonts/
  `fonts_certs.xml` — bundled `.ttf` files in `res/font/`, simpler and no
  Play Services runtime dependency): **Sora** (SemiBold/Bold only) for
  display/headline/title roles, **Manrope** for body/label roles. Applied
  via `baseline.someStyle.copy(fontFamily = ...)`, preserving Material 3's
  default sizes/spacing per role while only swapping the font.
- Spacing: a `Spacing` object (`xs`/`sm`/`md`/`lg`/`xl`/`xxl`, `4.dp` to
  `48.dp`) — always used instead of inline `.dp` literals in new code.
- Shapes: `AppShapes` (Material 3 `Shapes`), plus a dedicated
  `TextFieldTopRoundedShape` (rounded top corners, square bottom) matching
  Material 3's actual filled-TextField spec — not just a uniform
  `RoundedCornerShape` applied blindly.
- Reusable components in `ui/components/`: `AppPrimaryButton` /
  `AppSecondaryButton` (with an `isLoading` spinner state),
  `AppTextField` (filled style, theme-driven colors, bundles its own error
  message), `AppAvatar` (photo-or-initials), `AppCard` (flat, zero
  elevation, tonal `surfaceContainer` background — matches a soft, flat
  reference aesthetic, not Material's default drop-shadow card), and
  semantic text wrappers `TitleText` / `SubtitleText` / `BodyText` /
  `CaptionText` / `LabelText` / `ErrorText` (each a fixed style+color
  combo, so a design change is made once, not re-decided per screen).
- Every screen wraps content in `SocialClubTheme(dynamicColor = false) {
  Surface(color = MaterialTheme.colorScheme.background) { ... } }` — a
  `Surface` is required to actually paint the background color; without
  it, `MaterialTheme` only makes colors *available*, it doesn't render one.

## Compose conventions
- Every public Composable gets preview coverage using the shared
  `@ThemePreviews` multi-preview annotation (light, dark, large font 200%,
  dark + large font) — don't hand-write four separate `@Preview` blocks.
- Feed realistic fake data in previews (real-looking names, a real
  `https://randomuser.me/...` photo URL where relevant) — never empty
  strings or placeholder text.
- Merged semantics (`Modifier.semantics(mergeDescendants = true)`) +
  `contentDescription = null` on purely decorative images +
  `onClickLabel` for meaningful action announcements on interactive rows —
  this app is meant to demonstrate real accessibility awareness, not just
  pass a glance.
- Stateful/stateless split on every screen: a `XyzScreen` (takes
  `hiltViewModel()`, collects state) delegates to a `XyzContent` (takes
  plain parameters, no Hilt) — previews and future tests target the
  `Content` function, never the `Screen` function directly, since Hilt
  can't resolve inside `@Preview`.

## Testing conventions
- JUnit4 + MockK + `kotlinx-coroutines-test`. **No Turbine** — plain
  `viewModel.uiState.value` assertions after construction (most ViewModels
  trigger their load in `init{}`, so construction alone is enough to
  assert against) have been sufficient so far; revisit only if a test
  genuinely needs to assert an intermediate emission, not just the final
  state.
- `MainDispatcherRule` (in the shared test source set) swaps
  `Dispatchers.Main` for a test dispatcher around every test —
  **`UnconfinedTestDispatcher`** specifically (not `StandardTestDispatcher`),
  chosen because ViewModel `init{}`-triggered coroutines need to complete
  before the very next line of the test reads `uiState.value`.
- Mock interfaces/use-case classes with MockK (`mockk<SomeRepository>()`,
  `coEvery { mock.fn(any()) } returns ...` / `throws ...`), never concrete
  classes. A hand-written fake (e.g. `FakePersonRepository`,
  `FakeLogger`) is also a legitimate, used pattern for simpler interfaces —
  both styles appear in this codebase deliberately, not inconsistently.
- Test naming: natural-language backtick names describing the scenario and
  expected outcome, e.g.
  `` `when attendee is found, uiState becomes Success` ``  — not a rigid
  given/when/then template.
- Coverage target per ViewModel/use case: success path, a genuine
  not-found/empty case, and an exception → Error-state case, with
  assertions on the *specific* content of the state (exact message string),
  not just its type — a type-only assertion (`state is Error`) would pass
  even with the wrong error message and hide a real bug.
- `SavedStateHandle(mapOf("key" to value))` is how route-argument-driven
  ViewModels are constructed in tests — no real Navigation/Hilt needed.

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
5. If something I ask for contradicts an existing, deliberate decision
   documented above, say so explicitly before proceeding — don't silently
   go along with a change that reverts a choice I made for a reason.
