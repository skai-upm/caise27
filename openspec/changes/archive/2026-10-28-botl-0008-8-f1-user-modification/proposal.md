# Proposal

## Why

Milestone F1-C06 adds modification of user data through `USER UPDATE`, limited to authorized fields represented by an enum, keeping the DNI immutable and never producing a state that creation would have rejected. Changes must persist between runs.

## What Changes

- Add the command `USER UPDATE;Email;Password;Field;NewValue`.
- Represent modifiable fields with an enum: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. Any other field (including `DNI`) is rejected.
- Check current email and password before modifying; validate the new value with the creation rules (valid and unused email, valid password, 16-digit card); persist the result and print `USER UPDATED`.
- Commit with the mandatory message `[F1-C06] Implement user update`.

## Capabilities

### New Capabilities
- `user-modification`: The `USER UPDATE` command — identification, modifiable-field enum, validation/uniqueness of new values, DNI immutability and persistence.

### Modified Capabilities
- None.

## Impact

- New `model.UserField` enum; `UserService.updateUser`; `CommandProcessor` handles `USER UPDATE`.
- Ambiguities flagged:
  - Field names are matched exactly as listed (uppercase); other spellings are rejected as invalid fields.
  - `NAME`/`SURNAME` update the first name and the surname(s) attribute respectively; `PHONE` is stored without validation (as in creation).
  - Setting the email to the user's own current email is accepted (it is not "used by another user").
  - Output texts are not mandated; `USER UPDATED` (integration example) and `ERROR: <reason>` are used.
