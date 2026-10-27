# Proposal

## Why

Milestone F1-C04 requires users to survive between runs: they must be stored in a text file in the format specified by the assignment and loaded again when the application starts, without giving the user model or the main class persistence responsibilities.

## What Changes

- Store users in a text file, one user per line, attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card (e.g. `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`).
- Load previously stored users when the application starts; save whenever the set of users changes, so data persists however the run ends.
- File reading/writing lives in a dedicated class, separate from `User` and `App`; no databases, persistence frameworks or external libraries.
- Record the mandatory milestone commit `[F1-C04] Add user persistence` once verified.

## Capabilities

### New Capabilities
- `persistence`: How application state is saved to and restored from text files between runs (users now; groups, tickets and attractions in later deliveries).

### Modified Capabilities
<!-- None: user-management behaviour (creation, validation, uniqueness) is unchanged; uniqueness now also covers users loaded from the file, which follows from existing requirements. -->

## Impact

- New `UserFileStorage` class; `UserRepository` loads on creation and saves on change through it.
- `App` wires the repository to the users file (`data/users.txt`).
- Tests use temporary directories so they never touch the real data file.
