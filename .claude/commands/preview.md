Add preview coverage for the Composable given as argument: $ARGUMENTS

- Apply the shared `@ThemePreviews` multi-preview annotation (light, dark,
  large font 200%, dark + large font) to the Composable — do NOT hand-write
  separate `@Preview` blocks; if `@ThemePreviews` doesn't exist yet in this
  file's module, check `core`/`ui` common packages before creating a
  duplicate.
- Feed realistic fake data via a preview parameter provider or hardcoded
  sample values — never empty strings or "Lorem ipsum" placeholders.
- If the screen has a meaningful empty/error/loading state, add one extra
  targeted `@Preview` for that state on top of `@ThemePreviews`.
- Match the accessibility conventions already used elsewhere in the app
  (merged semantics, descriptive content descriptions).
- After generating the previews, note briefly why you chose these specific
  preview states.
