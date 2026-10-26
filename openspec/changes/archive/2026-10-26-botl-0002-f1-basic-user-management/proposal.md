# Proposal

## Why

The project is only an archetype baseline. The first functional epic must let operators manage park users from the terminal through API-like text commands, with validated data, uniqueness by DNI and email, and users that survive application restarts.

## What Changes

- The application reads text commands from the terminal; each command is split with `String.split` into operation and parameters (`USER CREATE`, `USER DELETE`, `USER UPDATE`). Invalid commands or missing parameters produce an error message and never terminate the program.
- New user model (first name, surname(s), DNI, email, password, phone, card number) with encapsulated attributes; the DNI is immutable.
- Validation without regular expressions or external libraries: Spanish DNI (8 digits + check letter), email (`texto@texto.xxx`), password (upper, lower, digit, symbol), card number (exactly 16 digits). Phone is only stored.
- Uniqueness: no two users share DNI or email, checked on create and on email update.
- Delete and update require the current email and password; update edits one field selected via an enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) and re-validates restricted fields. DNI cannot be changed.
- Users are persisted in a semicolon-separated text file (one user per line) by a dedicated storage class and reloaded at startup.
- Out of scope: attractions, groups, tickets, fast passes, encryption, regex, databases, frameworks, external libraries.

## Capabilities

### New Capabilities
- `command-processing`: How text commands are received from the terminal, split and dispatched, and how malformed commands are reported without crashing.
- `user-management`: Creating, deleting and updating users, including field validation rules and DNI/email uniqueness.
- `user-persistence`: Storing users in a semicolon-separated text file and restoring them when the application starts.

### Modified Capabilities

## Impact

- New classes under `src/main/java/es/upm/parque/` (user model, editable-field enum, validation, user registry, file storage, command processor); `App` becomes the terminal loop.
- New unit tests under `src/test/java/es/upm/parque/`.
- Runtime data file `data/users.txt` (created on demand, ignored by Git).
- No new dependencies; `project-build` requirements unchanged except that domain code now exists, which was explicitly deferred by the baseline.
