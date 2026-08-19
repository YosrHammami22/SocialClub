# "Match People" App — Build Plan (15 days: Aug 5 → Aug 20)

A list/detail people app, built with Clean Architecture, fully testable, with one AI feature inside the app and AI-assisted development throughout. This will double as your GitHub portfolio piece for the Revolut application.

---

## 1. The API

**Use [RandomUser.me](https://randomuser.me/)** — free, no API key required, purpose-built for exactly this use case.

- Endpoint: `https://randomuser.me/api/?results=20`
- Returns: name, photo (thumbnail + large), email, location, phone, date of birth, a unique ID — everything you need for a list screen + rich detail screen.
- No auth = zero setup friction, so you can start coding on Day 1, not Day 3 fighting API keys.

Example response fields you'll map:
```json
{
  "results": [
    {
      "name": { "first": "Jane", "last": "Doe" },
      "email": "jane.doe@example.com",
      "location": { "city": "Paris", "country": "France" },
      "picture": { "large": "...", "thumbnail": "..." },
      "phone": "...",
      "dob": { "age": 29 },
      "login": { "uuid": "..." }
    }
  ]
}
```

---

## 2. Architecture (Clean Architecture, 3 layers)

```
presentation/
  people-list/
    PeopleListScreen.kt (Compose)
    PeopleListViewModel.kt
  person-detail/
    PersonDetailScreen.kt (Compose)
    PersonDetailViewModel.kt

domain/
  model/Person.kt
  repository/PersonRepository.kt        (interface only)
  usecase/
    GetPeopleUseCase.kt
    GetPersonDetailUseCase.kt
    ToggleFavoriteUseCase.kt
    GenerateCompatibilitySummaryUseCase.kt   (the AI feature)

data/
  remote/
    RandomUserApiService.kt (Retrofit interface)
    dto/PersonDto.kt
    PersonMapper.kt (DTO -> domain Person)
  local/
    FavoritePersonDao.kt (Room)
    FavoritePersonEntity.kt
  repository/
    PersonRepositoryImpl.kt (implements domain interface, combines remote + local)

di/
  NetworkModule.kt   (Retrofit, OkHttp, Moshi/Gson)
  DatabaseModule.kt  (Room)
  RepositoryModule.kt (binds interface -> impl)
```

**Why this shape matters for your interview:** it's literally the same structure you already explained correctly in the mock interview (interface + implementation, domain independent of Android, ViewModel calling through use cases) — now you'll have a real, working example to point to instead of describing it abstractly.

**Evolving feature, built in from day 1:** the "Favorite" feature (Room-backed) is intentionally added partway through — this gives you a genuine story about adding a new feature to an existing architecture without breaking it, which is a great answer to "tell me about extending a feature."

---

## 3. AI Feature #1 — Inside the App: "Why You Might Match"

**Concept:** on the detail screen, add a button "Generate compatibility insight" — it sends both profiles' basic info (name, age, location, interests you can randomly assign) to an LLM API, which returns a short, fun, human-readable blurb explaining a playful "why you'd get along."

**Two versions — build the mock first, real API second:**

### Version A (Day 1 of this feature) — Mocked, no API key needed
```kotlin
class GenerateCompatibilitySummaryUseCase {
    operator fun invoke(personA: Person, personB: Person): String {
        return "You and ${personA.firstName} both share a love for exploring " +
               "new places — ${personA.city} and ${personB.city} aren't so " +
               "different after all!"
    }
}
```
This proves the architecture slot works before adding real network complexity.

### Version B — Real AI call (Anthropic API)
Swap the mock for a real call to Claude via the Messages API:
```kotlin
interface AiApiService {
    @POST("v1/messages")
    suspend fun generateSummary(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: MessageRequest
    ): MessageResponse
}

// Prompt example:
// "In one playful sentence, explain why ${personA.name} (${personA.city}) 
//  and ${personB.name} (${personB.city}) might get along. Keep it light and fun."
```
You'll need your own Anthropic API key (console.anthropic.com) — a small usage cost, but trivial for a demo app making a handful of calls. Store the key in `local.properties` (never commit it), inject it via `BuildConfig`.

**Why this is a strong interview talking point:** it shows you can integrate a second, different API beyond the main data source, handle a real async network call for a *generative* (non-deterministic) feature, and reason about product value ("this makes the match feel more personal") — directly hitting Revolut's "Product Ownership" theme.

---

## 4. AI Feature #2 — Using AI Tools to Build Faster

Use Claude (chat, or Claude Code if you install it) as a **pair programmer**, not a code-dump machine. A workflow that keeps you learning, not just copy-pasting:

1. **Scaffold with AI, then read every line.** Ask for a starting point (e.g. "generate a Retrofit service interface for RandomUser API"), then go through it line by line and explain it back to yourself out loud (ties into your daily fluency practice too).
2. **Get stuck → ask "why," not just "fix it."** When something breaks, paste the error and ask *"what's causing this, not just how to fix it"* — this is what actually rebuilds the knowledge instead of papering over the gap.
3. **Use AI for code review.** Once a piece works, ask "review this ViewModel for testability issues" — this trains your eye for the kind of thing an interviewer might probe.
4. **Use AI to generate test skeletons, then fill in assertions yourself.** Don't let AI write your test assertions — that's exactly the muscle (testing) you're trying to rebuild.

**Important for the interview:** if asked "do you use AI tools in your workflow," this project gives you a genuine, specific answer instead of a vague one — you can describe exactly how you used it (scaffolding + explaining errors + code review) and where you deliberately did the thinking yourself (test assertions, architecture decisions).

---

## 5. 15-Day Schedule (Aug 5 → Aug 20)

| Day | Date | Focus |
|---|---|---|
| 1 | Aug 5 | Project setup: empty Compose project, Hilt, Retrofit + RandomUser API call returning raw JSON in Logcat |
| 2 | Aug 6 | Data layer: DTO, mapper, domain `Person` model, repository interface + impl (remote only) |
| 3 | Aug 7 | Domain layer: `GetPeopleUseCase`; presentation: `PeopleListViewModel` + Compose list screen |
| 4 | Aug 8 | Detail screen: navigation, `GetPersonDetailUseCase`, `PersonDetailViewModel` |
| 5 | Aug 9 | **Coroutines focus day**: make sure list/detail loading uses `viewModelScope`, `Dispatchers.IO` correctly in repository — explain out loud why |
| 6 | Aug 10 | Room: `FavoritePersonEntity`, DAO, wire "favorite" toggle button on detail screen (evolving-feature story) |
| 7 | Aug 11 | **Review day**: explain your architecture out loud, end to end, no notes |
| 8 | Aug 12 | AI Feature Version A (mocked compatibility use case) wired into detail screen UI |
| 9 | Aug 13 | AI Feature Version B: real Anthropic API call, error handling (loading/success/error state) |
| 10 | Aug 14 | **Testing day**: fake repository, unit test `PeopleListViewModel` (success + error states) |
| 11 | Aug 15 | **Testing day 2**: test `GetPersonDetailUseCase`, test the favorite toggle logic |
| 12 | Aug 16 | Git/CI-CD: push to GitHub, write a clean README, set up GitHub Actions running `./gradlew test` on push |
| 13 | Aug 17 | Polish pass: rename things clearly, remove dead code, make sure it builds clean from scratch |
| 14 | Aug 18 | **Mock interview**: walk me through this exact project as if I'm the Revolut interviewer |
| 15 | Aug 19 | Final review day: rehearse STAR stories + this project's walkthrough, light only, no new coding |
| — | Aug 20 | **Interview day** |

*(Note: 15 working days, interview itself is the 20th — leaves you Aug 19 as a calm final review day, not a cram day.)*

---

## 6. The Killer Interview Line This Project Gives You

Once built, you have a genuine, specific answer to "tell me about a recent personal project":

> "To prepare for this interview, actually, I built a small people-matching app using Clean Architecture — a list and detail screen backed by a public API, Room for local favorites, full unit test coverage on the ViewModels, and I even added a small AI-generated feature that creates a compatibility summary between two profiles by calling Claude's API. It's on my GitHub — I wanted a real, current example instead of relying only on my professional projects."

This single sentence covers: initiative, current hands-on skill (not just theory), Clean Architecture, testing, Git/CI-CD, and a lightweight but genuine AI-integration story — hitting almost everything on Revolut's "nice to have" list in one shot.
