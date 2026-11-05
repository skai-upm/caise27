# Proposal

## Why

Milestone F1-C05 (Delivery 1, step 7) requires users to be deletable with `USER DELETE`, only when the user is identified by email and the correct password is given, and the deletion must be reflected in the persisted users file. Today `USER DELETE` is answered as an unknown command.

## What Changes

- Implement `USER DELETE;Email;Password` (source §8): locate the user by email, verify it exists, check the password, delete the user, and update persistent information.
- An unknown email or an incorrect password rejects the command with an `ERROR` response and leaves stored and persisted users unchanged.
- Add a user-removal operation to the user store, which rewrites the users file like any other change.
- Update the command-interface specification: its "not-yet-supported command" example used `USER DELETE`, which is now supported.

In scope: `USER DELETE`, its error cases and persistence update.

Out of scope: `USER UPDATE` (next milestone), `HELP`/`EXIT`, file input.

## Capabilities

### New Capabilities
- `user-deletion`: Deleting a user with `USER DELETE;Email;Password` — identification by email, password check, and persisted removal.

### Modified Capabilities
- `command-interface`: the scenario illustrating a not-yet-implemented command changes its example from `USER DELETE` to `USER UPDATE`, because `USER DELETE` is now implemented.

## Impact

- `CommandProcessor` (new command branch), `UserRepository` (`findByEmail`, `remove`), tests.
- `user-persistence` "saved after each change" now also covers deletions, without changing its requirement text.
