# Proposal

## Why

The project currently has only a skeleton entry point. The first functional epic must let operators manage park users from the terminal through API-like text commands, with strict validation and file-based persistence, so that later epics (groups, tickets, attractions) have registered users to build on.

## What Changes

- Add a terminal command interface that reads text commands, splits them with `String.split`, and reports invalid commands or missing parameters without terminating.
- Add `USER CREATE`, `USER DELETE` and `USER UPDATE` commands.
- Add a user model (first name, surname(s), DNI, email, password, phone, card number) with encapsulated attributes and an immutable DNI.
- Validate DNI (Spanish check-letter algorithm), email (`texto@texto.xxx`, no regex), password (upper, lower, digit, symbol; no regex) and card number (exactly 16 digits).
- Enforce uniqueness by DNI and by email.
- Persist users in a semicolon-separated text file (one user per line) and reload them on start-up, using a class separate from both the user model and the main class.
- Editable fields for `USER UPDATE` are modelled as an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- Out of scope: attractions, groups, tickets, passes, encryption, external libraries, regular expressions, databases, frameworks.

## Capabilities

### New Capabilities
- `user-management`: Creating, updating and deleting park users, their validation rules, uniqueness constraints and persistent storage.
- `command-interface`: How the application receives text commands from the terminal, parses them and reports results or errors.

### Modified Capabilities
<!-- none: project-build requirements are unchanged; the project keeps building with mvn test -->

## Impact

- New classes in `es.upm.parque`: user model, editable-field enum, validator, user manager, file repository, command processor; `App` becomes the terminal loop.
- New data file `data/users.txt` created at run time (ignored by Git).
- New unit tests for validation, persistence, management rules and command parsing.
- No new dependencies.
