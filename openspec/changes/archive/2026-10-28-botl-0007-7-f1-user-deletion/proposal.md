# Proposal

## Why

Milestone F1-C05 adds removal of users through `USER DELETE`, allowed only when the user is identified by email and the correct password, with the result reflected in the persistent users file.

## What Changes

- Add the command `USER DELETE;Email;Password`.
- Locate the user by email, verify it exists, check the password, delete the user and rewrite the users file; print `USER DELETED`.
- Reject unknown emails and incorrect passwords with an `ERROR:` message, leaving users and file unchanged.
- Commit with the mandatory message `[F1-C05] Implement user deletion`.

## Capabilities

### New Capabilities
- `user-deletion`: The `USER DELETE` command — identification by email and password, deletion, and persistence of the result.

### Modified Capabilities
- None (command interface rules already cover argument counts and unknown commands; `USER DELETE` simply becomes a recognized command).

## Impact

- `CommandProcessor`, `UserService`, `UserRepository` gain delete support; new tests.
- Ambiguity flagged: the output texts are not mandated; `USER DELETED` (from the integration example), `ERROR: User not found` and `ERROR: Incorrect password` are used.
