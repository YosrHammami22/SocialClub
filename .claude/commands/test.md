Write unit tests for: $ARGUMENTS

- Use JUnit4 + MockK, mocking only interfaces (never concrete classes).
- For StateFlow/Flow assertions, use Turbine's `test { }` block.
- Cover: initial state, success path, error/failure path, and at least one
  edge case specific to this class's own logic (not a generic case).
- Follow the existing naming convention: `` `given X when Y then Z`() ``.
- Put the file in the mirrored path under `src/test/java`.
- Before writing the tests, briefly state which behaviors you identified as
  worth testing and why — this becomes the "why did you test this" answer
  for an interview.
