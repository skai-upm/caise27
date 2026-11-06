# Proposal

## Why

Milestone F1-C05 (Delivery 1, step 7) requires deleting users with `USER DELETE`, only when the user identifies with the correct email and password, and reflecting the deletion in the persisted user file. Today users can only be created.

## What Changes

- Recognize `USER DELETE;Email;Password`.
- Locate the user by email, verify it exists, check the password, delete the user and update the user file (source section 8, steps 1–5).
- Reject the command, leaving users and the file unchanged, when no user has that email or the password is incorrect.

### In scope
- Source section 8 and its integration with the existing command processing and user file.

### Out of scope
- `USER UPDATE` (F1-C06), file-based command input (F1-C07), `HELP`/`EXIT` (F1-C08).

## Capabilities

### New Capabilities
- `user-deletion`: the `USER DELETE` command, its email/password identification and its effect on stored users.

### Modified Capabilities
<!-- None: command-interface, user-registration and user-persistence requirements are unchanged; deletion reuses them. -->

## Impact

- `UserService` gains `delete`, `UserRepository` gains `findByEmail`/`remove` (persisted), `CommandProcessor` dispatches `USER DELETE`; new tests.
