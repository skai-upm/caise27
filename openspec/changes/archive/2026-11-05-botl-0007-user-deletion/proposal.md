# Proposal

## Why

Milestone F1-C05 adds the ability to remove users with `USER DELETE`. Users can currently only be created (and are persisted in `users.txt`); there is no way to remove one, and deletion must be protected by the user's password and reflected in the persisted data.

## What Changes

- Add `USER DELETE;Email;Password`: locate the user by email, verify it exists, check the password, delete the user and update `users.txt`. Prints `USER DELETED` on success.
- Unknown email, wrong password or a wrong number of elements are rejected with an `ERROR: ...` message and leave users and file unchanged.
- Out of scope: `USER UPDATE` (F1-C06), HELP/EXIT, command files.

## Capabilities

### New Capabilities
- `user-deletion`: The `USER DELETE` operation, its identification conditions and persistence of the result.

### Modified Capabilities
<!-- None: creation, validation, command splitting and persistence format are unchanged. -->

## Impact

- `UserRepository.remove`, `UserService.deleteUser`, a new branch in `CommandProcessor`; tests.
