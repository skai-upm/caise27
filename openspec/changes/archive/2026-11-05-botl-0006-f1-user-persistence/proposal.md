# Proposal

## Why

Milestone F1-C04 (Delivery 1, step 6) requires users to survive between runs of the application. Today users created with `USER CREATE` live only in the in-memory `UserRepository` and are lost when the process ends.

## What Changes

- Persist users to a plain text file in the format of source §7: one user per line, attributes in the order first name; surnames; DNI; email; password; telephone; card number, separated by `;`.
- Load previously stored users when the application starts, so that DNI/email uniqueness and later operations see them.
- Save the stored users after every successful change to the user set. The application currently ends only when standard input ends (EXIT comes in a later milestone), so a user is persisted as soon as it is created.
- Implement file reading and writing in a dedicated class, separate from the user class and the main class (source §3.2 and §7), using only the Java standard library. No databases, persistence frameworks or external libraries.

In scope: the users file format, loading at start-up, saving after creation, and tolerance of a missing file.

Out of scope: persistence of other entities (Delivery 2), configurable file location via commands, and encryption of stored data.

## Capabilities

### New Capabilities
- `user-persistence`: Storage of users in a `;`-separated text file across application runs: the file format, loading at start-up and saving after changes.

### Modified Capabilities
- None. `user-registration` behavior (validation, uniqueness, responses) is unchanged; it now also applies to users loaded from the file because they are in the same store. `user-model` already requires the user object to perform no persistence.

## Impact

- New class `persistence.UserFileStore`; `UserRepository` gains load-on-construction and save-after-add through it; `App` wires the default file `users.txt` in the working directory.
- New runtime file `users.txt` (git-ignored).
