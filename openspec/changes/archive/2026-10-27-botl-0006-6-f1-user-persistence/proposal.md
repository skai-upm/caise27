# Proposal

## Why

Milestone F1-C04: users currently live only in memory and are lost when the application ends. They must be stored in a text file in the format the assignment defines and loaded again at start-up, with file I/O kept out of the user and main classes.

## What Changes

- Persist users in a text file, one user per line, attributes separated by `;` in the order FirstName;Surnames;DNI;Email;Password;Phone;Card.
- Load previously stored users when the application starts; save the file whenever the set of users changes (currently after `USER CREATE`).
- Implement reading/writing in a dedicated persistence class, used by the user storage class; `User` and `Main` gain no file logic beyond `Main` choosing the file location.
- Commit with the exact message `[F1-C04] Add user persistence`.

## Capabilities

### New Capabilities
- `user-persistence`: Storage of users across runs in a `;`-separated text file and their loading at start-up.

### Modified Capabilities
<!-- none -->

## Impact

- New `persistence/UserFileStore`; `UserRepository` loads from and saves through it; `Main` wires the default file.
- Ambiguities flagged: the source gives no file name — `users.txt` in the working directory is used. A missing file means "no users yet". Lines that do not have exactly seven fields are skipped on load (controlled handling, no crash). The file is rewritten completely on every change.
