Write or update a Firestore security rule for: $ARGUMENTS

- State the approach first: which document(s) the rule reads (`resource.data`,
  `request.resource.data`, `request.auth`) and why, before writing the rule
  itself — per this project's working style in CLAUDE.md.
- `resource.data` is the document's state BEFORE this write; `request.resource.data`
  is what's being written. A rule checking "is this field already set" must use
  `resource.data`; a rule checking "what is the caller claiming" must use
  `request.resource.data`. Don't conflate them — this is exactly the mistake
  that breaks the Attendee authUid "claim" write (see `red-flags.md`, flag 4).
- `request.auth` is populated server-side from the caller's verified Firebase
  Auth ID token — never trust a client-supplied field as a substitute for it.
- Never assume a document field exists without checking: dot-notation access
  to a missing field errors out the rule evaluation (denies), rather than
  returning null/false quietly. Use `('field' in resource.data)` or
  `resource.data.get('field', default)` when a field might be absent.
- A UI control being hidden or disabled is never a substitute for a rule — if
  a case must be blocked, write the rule for it; don't rely on the Compose layer.
- Check `red-flags.md` in this project for open decisions (e.g. Flag 3:
  denormalized `receiverAuthUid` vs. a `get()` lookup) before locking in a
  design the rule depends on — ask me which way we're going if it's unresolved.
- Current rules in this project are documented as wide open
  (`allow write: if true`) per CLAUDE.md's "Known gap" — don't copy that as a pattern.
- Put the rule in `firestore.rules` at the project root, matching existing style.
- After writing it, list 1-2 concrete inputs that would now be denied that
  weren't before (or vice versa) — this becomes the "walk me through this
  rule" answer for an interview.