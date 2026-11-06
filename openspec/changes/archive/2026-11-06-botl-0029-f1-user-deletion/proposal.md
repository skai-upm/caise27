# Proposal

## Why

Milestone F1-C05 adds `USER DELETE;Email;Password`. It must locate the user by e-mail, check that they exist, check the password, delete the user and update persistent data. A wrong password must never delete a user. Central management already has the derived deletion guard (active tickets, locked groups) and BCrypt verification, but nothing deletes users yet.

## What Changes

- Add `ParkController.deleteUser(email, password)`, which does, in order:
  1. Finds the user by e-mail and rejects unknown ones.
  2. Verifies the password against the stored BCrypt value.
  3. Applies the existing guard (`tickets` capability), so deletion is rejected while the user has an active ticket or belongs to a group locked by an active group ticket.
  4. Removes the user from every group, then from the registry.
- Historical tickets are kept as fiscal records with the user's e-mail.
- Add `USER DELETE;Email;Password`, which prints `USER DELETED`. Persistent data is updated by the after-command save.
- Rejected deletions change nothing.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `users`: adds user deletion with password verification.

## Impact

- `ParkController.deleteUser` and `UserCommands` (`USER DELETE`), with tests.
