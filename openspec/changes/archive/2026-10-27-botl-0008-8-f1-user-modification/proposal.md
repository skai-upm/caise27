# Proposal

## Why

Milestone F1-C06 adds `USER UPDATE` so users can change their data after identifying with email and password, without ever reaching a state that USER CREATE would have rejected and with changes persisted.

## What Changes

- Add `USER UPDATE;Email;Password;Field;NewValue`.
- Represent modifiable fields with an enum: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. The DNI is not modifiable.
- Check current email and password before any modification.
- Validate new values with the creation rules: email valid and not used by another user, password rules, card exactly 16 digits.
- Persist the modification; respond `USER UPDATED`.
- Commit with the exact message `[F1-C06] Implement user update`.

## Capabilities

### New Capabilities
- `user-modification`: The `USER UPDATE` command, modifiable fields enum, identification, validation and persistence.

### Modified Capabilities
<!-- none -->

## Impact

- New `model/UserField` enum; `UserRepository.update`; `CommandProcessor` dispatches USER UPDATE.
- Ambiguities flagged: `NAME` maps to the first name and `SURNAME` to the surname(s) attribute; field names are matched case-sensitively as written; `DNI` (or any other name) as field is rejected as a non-modifiable/unknown field; setting EMAIL to the user's own current email is allowed (it is not "used by another user"); NAME/SURNAME/PHONE only require a non-empty value since no other rule exists for them.
