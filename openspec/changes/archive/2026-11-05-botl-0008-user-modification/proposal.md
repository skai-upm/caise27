# Proposal

## Why

Milestone F1-C06 makes `USER UPDATE` available so users can change their data after registration. The change must be authenticated, limited to an enumerated set of fields (never the DNI), must not produce a state that creation would have rejected, and must persist.

## What Changes

- Add `USER UPDATE;Email;Password;Field;NewValue` with modifiable fields represented by an enum: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`.
- Check the current email and password before any modification.
- Validate new values with the creation rules: a new email must be valid and not used by another user; a new password must meet the password rules; a new card must have exactly 16 digits.
- Reject `DNI` (immutable) and any unknown field with an `ERROR: ...` message; print `USER UPDATED` on success and persist the change to `users.txt`.
- Out of scope: HELP/EXIT, command files.

## Capabilities

### New Capabilities
- `user-modification`: The `USER UPDATE` operation, its modifiable fields, authentication, validation and persistence.

### Modified Capabilities
<!-- None: user-domain already guarantees an immutable DNI; validation rules and persistence format are unchanged. -->

## Impact

- New `model.UserField` enum; `UserService.updateUser`; `UserRepository.update` (saves); new `CommandProcessor` branch; tests.
