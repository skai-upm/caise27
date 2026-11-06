# Proposal

## Why

Milestone F1-C06 (Delivery 1, step 8) requires the `USER UPDATE` command so an authenticated user can change authorized fields, represented by an enum, without ever changing the DNI or reaching a state that creation would have rejected, and with changes persisted between runs.

## What Changes

- Recognize `USER UPDATE;Email;Password;Field;NewValue`.
- Represent modifiable fields with an enum: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. Any other field name — including `DNI` — is rejected.
- Check the current email and password before any modification.
- Validate new values with the creation rules: a new email must be valid and not used by another user; a new password must meet the password rules; a new card must have exactly 16 digits.
- Persist successful modifications in the user file; rejected modifications change nothing.

### In scope
- Source section 9.

### Out of scope
- File-based command input (F1-C07), `HELP`/`EXIT` (F1-C08).

## Capabilities

### New Capabilities
- `user-modification`: the `USER UPDATE` command, its authentication, the set of modifiable fields and the validation of new values.

### Modified Capabilities
<!-- None: user-management's immutable DNI and encapsulation requirements are respected as they stand; valid-users rules are reused unchanged. -->

## Impact

- New enum `es.upm.park.model.UserField`; `UserService.update`, `UserRepository.replace`; `CommandProcessor` dispatches `USER UPDATE`; new tests.
