# Proposal

## Why

Milestone F1-C04 requires users to persist between runs in a text file, one user per line with attributes separated by `;`, loaded again at startup. Reading and writing must live in a class separate from the user class and the main class. Users registered with `USER CREATE` currently disappear when the application ends.

## What Changes

- Add `persistence.UserRepository` (`users.txt`), one line per user: `FirstName;Surnames;DNI;Email;Password;Phone;Card`. The password field contains the stored BCrypt value, never the plain password (Delivery 3 `password-protection` rule, which overrides the plain example of Delivery 1).
- `ParkStorage` receives the user repository by injection and loads users first, before attractions, groups, tickets and passes, and saves them with the rest of the state. Normal and `--fly` modes therefore both cover users through the same repository with different directories.
- `ParkController.restoreUser(...)` rebuilds a persisted user. It keeps DNI and e-mail unique and doesn't re-hash the stored password. Malformed or duplicate lines are reported as warnings and skipped.
- Neither `User` nor `App` contains file I/O.

### Out of scope
- Migrating plain-text password files from earlier versions (explicitly not required).

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `park-persistence`: adds the requirement that users survive restarts.

## Impact

- New `UserRepository`.
- `ParkStorage` (constructor and factory).
- `ParkController.restoreUser`.
- UML and tests.
