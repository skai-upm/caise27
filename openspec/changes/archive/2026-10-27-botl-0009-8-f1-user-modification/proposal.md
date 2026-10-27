# Proposal

## Why

Milestone F1-C06 makes `USER UPDATE` available so users can change their authorised fields (represented by an enum) while the DNI stays immutable, without ever reaching a state that user creation would have rejected, and with changes persisted between runs.

## What Changes

- New `USER UPDATE;Email;Password;Field;NewValue` command; prints `USER UPDATED` on success.
- Modifiable fields are represented by an enum: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. `DNI` and any other field are rejected.
- The current email and password are checked before any modification.
- New values follow the creation rules: a new email must be valid and not used by another user; a new password must meet the password rules; a new card must have exactly 16 digits.
- Changes are written to the users file.
- Record the mandatory milestone commit `[F1-C06] Implement user update` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `user-management`: adds user modification of enum-defined fields with identification, validation, uniqueness and persistence.

## Impact

- New `UserField` enum; `UserService.updateUser`; `UserRepository` re-keys a user whose email changes; `CommandProcessor` dispatches `USER UPDATE`.
