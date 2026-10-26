---
name: e2e
description: Run the SocialClub end-to-end UI tests on the Android emulator through adb (sign-in, first connection, clear data, Firebase session, rotation, background). Use when asked to test the sign-in flow on the emulator, to run the e2e tests, or to add an e2e scenario.
argument-hint: "[scenario words | --list]"
allowed-tools:
  - Bash(python3 ${CLAUDE_SKILL_DIR}/run.py *)
  - Bash(adb devices)
---

# End-to-end tests on the emulator

These tests drive the installed app through adb exactly as a user would (type, tap,
rotate, leave and come back) and check the screen and logcat. They are separate from
the JVM unit tests (`/test`).

## Run

```bash
python3 ${CLAUDE_SKILL_DIR}/run.py $ARGUMENTS
```

- No argument runs every scenario. Words filter by file name: `signin`, `rotation`, `04`.
- `--list` prints the scenarios. `--verbose` prints every adb command.
- A full run takes several minutes: give the Bash call a 10-minute timeout.
- Exit code: 0 all passed, 1 at least one FAIL or ERROR, 2 setup problem.

## Before running

1. An emulator must be running (`adb devices`). The runner refuses a physical phone
   unless `--serial <id>` is passed. Never pass a phone's serial unless the user asks
   for it: every scenario clears the app's data on the device.
2. The installed build must match the code. If Kotlin sources changed since the last
   install, run `./gradlew installDebug` first.
3. `accounts.env` must exist next to this file (it is git-ignored). If it is missing,
   ask the user for the test accounts; never invent credentials and never commit them.

## Scenarios

| File | What it proves |
|---|---|
| `01_signin_after_clear_data` | Activated attendee on a clean install: Home → password screen → Attendee, with no `PERMISSION_DENIED` |
| `02_signin_wrong_password` | Wrong password keeps the user on the sign-in screen and creates no session |
| `03_first_connection` | Attendee with no password yet is sent to Create password |
| `04_session_persistence` | The Firebase session survives a restart, belongs to one user only, and is removed by clearing data |
| `05_rotation` | Rotating keeps the typed password and does not navigate again |
| `06_background` | Leaving and returning keeps the sign-in screen state and the Attendee screen |
| `07_unknown_email` | Unknown email stays on Home with the "not found" message |

## Reading the result

- **FAIL**: the app did not behave as expected. The line gives the expected and actual
  screen, and the evidence files in `reports/<timestamp>/`: read the `.png` screenshot,
  the `.logcat.txt` and the `.screen.txt` before concluding. Then find the cause in the
  code and, as CLAUDE.md asks, state the approach before changing anything.
- **note** lines that name a screenshot: open that image and confirm the message is
  really displayed. Text inside a Compose text field cannot be read through adb.
- **SKIP**: the scenario could not run in this setup (the reason is printed).
- **ERROR**: a bug in a scenario or in `run.py`, not in the app. Fix the test.

## Rules

- Never finish the Create password flow for `FIRST_CONNECTION_EMAIL`: it activates the
  account for good and `03_first_connection` can never pass again with that email.
- Keep wrong-password attempts to one per run: Firebase temporarily blocks an account
  after repeated failures.
- The tests use the real Firebase project. Scenarios only read and sign in. Ask the
  user before adding one that writes Firestore data.

## Adding a scenario

Create `scenarios/NN_short_name.py`. The first docstring line is its title. Copy
`07_unknown_email.py` as a start.

```python
"""One line saying what this proves."""


def run(t):
    t.fresh_start()
    t.sign_in(t.accounts.activated[0])
    t.expect_no_permission_denied()
```

What `t` offers:

- **Data**: `t.accounts.activated` (list with `.email`, `.password`),
  `t.accounts.first_connection`, `t.strings["sign_in_button"]` (from `strings.xml`).
- **App state**: `t.fresh_start()` clears data and opens Home; `t.restart()` kills and
  reopens, keeping data; `t.background(seconds)`; `t.rotate(landscape=True)`.
- **Actions**: `t.submit_email(email)`, `t.enter_password(pw)`, `t.submit_password(pw)`,
  `t.tap_sign_in()`, `t.sign_in(account)`, `t.tap_text("label")`,
  `t.type_into_field(text, index=0)`.
- **Expectations**: `t.expect_screen("home" | "signin" | "create_password" | "attendee", why=...)`,
  `t.expect_stays_on(screen, seconds)`, `t.expect_text("...")`,
  `t.expect_no_permission_denied()`, `t.check(condition, "what is true")`,
  `t.look_for_message(before, "screenshot_name")`, `t.log_contains("...")`.
- **Reporting**: `t.step("...")`, `t.note("...")`, `t.skip("reason")`.

For a new screen, add its title to `screen()` and its route to `ROUTES` in `run.py`.

## How screens are recognised

1. By a title read from `app/src/main/res/values/strings.xml` (`sign_in_title`,
   `create_password_title`, `get_from_api`), so renaming a label does not break tests.
2. Otherwise by the last `Navigation -> ...Route` line that `AppNavHost` logs. This is
   the only marker of the Attendee screen. If that log is removed, the Attendee screen
   is accepted as "none of the other screens" and a note says so.

## Troubleshooting

- "Cannot find "…" to tap": the label on screen differs from `strings.xml`. The message
  lists what is on screen.
- "Pressing Back to close the keyboard left …": the keyboard detection
  (`mInputShown` in `dumpsys input_method`) is wrong on this emulator image. Adjust
  `hide_keyboard()` in `run.py`.
- `05_rotation` skipped: the emulator did not rotate. Check that the activity is not
  locked to portrait.
