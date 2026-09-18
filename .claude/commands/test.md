Write unit tests for: $ARGUMENTS

- Use JUnit4 + MockK, mocking only interfaces or use-case classes with
  `mockk<Type>()` (never concrete implementation classes). A hand-written
  fake (e.g. `FakePersonRepository`, `FakeLogger`) is also acceptable for
  simple interfaces — match whichever style existing tests for this class's
  dependencies already use, for consistency.
- No Turbine. Assert directly on `viewModel.uiState.value` — most
  ViewModels trigger loading in `init{}`, so construction alone is enough
  before asserting. Only reach for a different approach if the test
  genuinely needs to assert an intermediate emission, not just the final
  state — flag this explicitly if it comes up, don't add Turbine silently.
- Use `MainDispatcherRule` (already in the shared test source set) via
  `@get:Rule`.
- Cover: success path, a genuine not-found/empty case, and an
  exception → Error-state case, plus at least one edge case specific to
  this class's own logic (not a generic case).
- Assert the *specific* content of the resulting state — exact error
  message strings, exact returned values — not just its type. A type-only
  assertion (`state is Error`) would pass even with the wrong message and
  hide a real bug; this project's tests always check the actual content.
- Test naming: natural-language backtick names describing the scenario and
  expected outcome, e.g. `` `when attendee is found, uiState becomes
  Success`() `` — not a given/when/then template.
- If the class under test takes a `SavedStateHandle`, construct it with
  `SavedStateHandle(mapOf("key" to value))` directly — no real
  Navigation/Hilt needed.
- Put the file in the mirrored path under `src/test/java`.
- Before writing the tests, briefly state which behaviors you identified as
  worth testing and why — this becomes the "why did you test this" answer
  for an interview.
