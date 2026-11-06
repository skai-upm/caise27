# Proposal

## Why

Milestone F1-C04 requires users to outlive a single execution. They must be stored in a text file with one user per line and attributes separated by `;`, and loaded again when the application starts. Reading and writing must live in a class separate from the user class and the main class. Users created with `USER CREATE` are currently lost at exit, unlike groups, tickets, passes and attractions.

## What Changes

- Store users in `users.txt` inside the data directory. Each line holds `FirstName;Surnames;DNI;Email;Password;Phone;Card`, in the source's order. The password field holds the protected (BCrypt) value required by Delivery 3, never the plain text.
- Save the users after every successful user operation, and load them when the application starts, before any command runs.
- Malformed user lines are reported and skipped. Startup continues with the remaining users.
- `--fly` runs use the same user repository configured with the temporary directory, so they neither see nor modify normal user data.

### In scope
- User repository, storage port, save/load wiring, `--fly` coverage, tests.

### Out of scope
- USER DELETE / UPDATE (later milestones), which will reuse the same saving path. Migration of plain-text password files (not required).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: adds user persistence between executions, the line format, and `--fly` isolation for users.

## Impact

- New `business.controller.UserStorage` port and `persistence.UserRepository`. `StoragePaths.users()`. `UserController` saves through the injected port, and `UserRegistry.restore`. `AppConfiguration` and `App` load users at startup.
- Tests, UML, README.
