# Proposal

## Why

Milestone F1-C05 adds `USER DELETE;Email;Password`: locate the user by e-mail, verify that it exists, check the password, delete the user and update the persistent data; a wrong password must never delete the user. The deletion rules of later deliveries (active tickets, locked groups, historical tickets kept) already exist as a controller operation without a command.

## What Changes

- Add an authenticated deletion operation: unknown e-mail → not-found error; wrong password (checked against the stored BCrypt value) → authentication error and nothing deleted; otherwise the existing deletion rules apply (rejected while the user has an active ticket or belongs to a locked group; removal from groups; historical tickets kept).
- Add the `USER DELETE` command (two fields) with message `USER DELETED`; the result is persisted by the save-after-command mechanism.
- Commit with the exact message `[F1-C05] Implement user deletion`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `user-management`: adds the USER DELETE command.

## Impact

- `ParkController.deleteUser(email, password)`, `UserDeleteCommand`, tests.
- **Flagged ambiguity:** an unknown e-mail and a wrong password produce different error messages, following the source's ordered steps (verify existence, then check the password).
