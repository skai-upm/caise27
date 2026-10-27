# Proposal

## Why

Milestone F1-C05 adds `USER DELETE`, letting a user be removed only after identifying themselves with email and password, with the deletion reflected in the persistent user file.

## What Changes

- Add `USER DELETE;Email;Password`: locate the user by email, verify it exists, check the password, delete the user and rewrite the user file; respond `USER DELETED`.
- An unknown email or an incorrect password rejects the command and leaves users (in memory and on disk) unchanged.
- Commit with the exact message `[F1-C05] Implement user deletion`.

## Capabilities

### New Capabilities
- `user-deletion`: The `USER DELETE` command — identification by email and password, deletion and persistence.

### Modified Capabilities
<!-- none -->

## Impact

- `UserRepository.remove` (saves after removal); `CommandProcessor` dispatches `USER DELETE`.
- Ambiguities flagged: password comparison is exact (case-sensitive); the command requires exactly two non-empty fields; the error for an unknown email and for a wrong password are reported separately since the source lists them as separate steps.
