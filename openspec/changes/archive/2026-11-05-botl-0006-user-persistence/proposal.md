# Proposal

## Why

Milestone F1-C04: users created with `USER CREATE` currently live only in memory and are lost when the application exits. The assignment requires users to be preserved between runs in a text file, one user per line with `;`-separated attributes, loaded at start-up, with file handling kept out of the user class and the main class.

## What Changes

- Store all registered users in a text file (`users.txt` in the working directory), one line per user: `FirstName;Surnames;DNI;Email;Password;Phone;Card`.
- Load previously stored users when the application starts, so uniqueness checks and later operations see them.
- Save the file whenever the set of users changes (currently: after a successful `USER CREATE`).
- File reading/writing in a dedicated persistence class; no databases, persistence frameworks or external libraries.
- Out of scope: persistence of deletions/updates (F1-C05/F1-C06 will reuse the same mechanism), park data (Delivery 2).

## Capabilities

### New Capabilities
- `user-persistence`: Storage of users in a text file between runs, its line format and loading at start-up.

### Modified Capabilities
<!-- None: user-registration behavior (validation, uniqueness, responses) is unchanged; persistence is an added behavior. -->

## Impact

- New `persistence` package (`UserStorage`, `UserFileStorage`); `UserRepository` loads from and saves to a storage; `App` wires the file storage.
- Creates/overwrites `users.txt` in the directory where the application runs.
