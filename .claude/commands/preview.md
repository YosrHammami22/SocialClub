Add preview coverage for the Composable given as argument: $ARGUMENTS

- First check whether this Composable takes `viewModel: X = hiltViewModel()`
  as a default parameter. If so, it cannot be previewed directly — Hilt
  can't resolve inside `@Preview`. Split it into a stateful `XyzScreen`
  (keeps the `hiltViewModel()` default, collects state) and a stateless
  `XyzContent` (takes the resolved state and event lambdas as plain
  parameters, no Hilt) if this split doesn't already exist. Previews always
  target the `Content` function, never the `Screen` function.
- Apply the shared `@ThemePreviews` multi-preview annotation (light, dark,
  large font 200%, dark + large font) to each preview — do NOT hand-write
  separate `@Preview` blocks; check `ui/theme/preview` before assuming it
  doesn't exist.
- Wrap every preview body in `SocialClubTheme(dynamicColor = false) {
  Surface(color = MaterialTheme.colorScheme.background) { ... } }` —
  without the `Surface`, the background color never actually renders, even
  with the theme applied.
- Feed realistic fake data via a preview parameter provider or hardcoded
  sample values (real-looking names, a real photo URL where relevant) —
  never empty strings or "Lorem ipsum" placeholders.
- If the screen has a sealed UI state with meaningful Loading/Error/Empty/
  NotFound branches, add one preview per branch, not just one for the
  "happy path" — this project's screens are covered in every state, not
  only Success.
- Match the accessibility conventions already used elsewhere in the app
  (merged semantics, descriptive content descriptions, `null` on purely
  decorative images).
- After generating the previews, note briefly why you chose these specific
  preview states.
