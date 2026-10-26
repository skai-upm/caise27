# Proposal

## Why

The project skeleton exists but does nothing. The first functional epic of Delivery 1 is basic user management: park users must be registered, edited, removed and kept across executions, driven by API-like text commands typed into the terminal.

## What Changes

- The application reads text commands from standard input, one per line, and splits each into operation and parameters with `String.split` (`;` separator).
- New commands `USER CREATE`, `USER DELETE` and `USER UPDATE` with the syntax given in the epic.
- A user model with first name, surname(s), DNI, email, password, telephone and card number; encapsulated attributes, DNI immutable.
- Validation without regular expressions or external libraries: Spanish DNI (8 digits + control letter), email (`texto@texto.xxx`), password (upper, lower, digit, symbol), card number (exactly 16 digits).
- Uniqueness of DNI and email across users.
- Persistence of users in a `;`-separated text file (one user per line), loaded at startup and rewritten after each change.
- Invalid commands or missing parameters produce an error message and never terminate the program unexpectedly.
- Out of scope: attractions, groups, tickets, passes, encryption, external libraries, HELP/EXIT and file input (later epics).

## Capabilities

### New Capabilities
- `command-processing`: How text commands are received, split into operation and parameters, dispatched, and how malformed commands are reported.
- `user-management`: Creating, deleting and updating users, data validation rules, uniqueness, and persistent storage of users.

### Modified Capabilities
<!-- None: project-build requirements are unchanged (mvn test must keep passing). -->

## Impact

- New production classes in `es.upm.parque` (user model, editable-field enum, validation, user management, file storage, command processing) and changes to `App`.
- New unit tests under `src/test/java`.
- A runtime data file `data/users.txt` is created in the working directory; `data/` is added to `.gitignore`.
- Only the Java standard library is used at runtime.
