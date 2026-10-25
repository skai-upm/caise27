# Proposal

## Why

The project skeleton (Delivery 1, step 1) has no functionality. The first functional epic introduces basic user management for the amusement park: users must be created, updated and deleted through text commands with an API-like format, validated, kept unique and persisted between runs, using only the Java standard library.

## What Changes

- Terminal application that reads text commands from standard input, one per line, and prints one response per command.
- Commands interpreted exclusively with `String.split`: `USER CREATE`, `USER DELETE`, `USER UPDATE`; unknown commands and wrong parameter counts are reported without terminating the program.
- User model with encapsulated first name, surname(s), immutable DNI, email, password, telephone and card number.
- Validation without regular expressions or external libraries: Spanish DNI (format + check letter), email (`texto@texto.xxx`), password (uppercase, lowercase, digit, symbol), 16-digit card number; telephone only stored.
- Uniqueness by DNI and by email.
- Deletion and update authenticated by email + password; update of NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD through an enum, re-validating restricted fields; the DNI can never change.
- Persistence of users in a semicolon-separated text file (one user per line), loaded on startup and rewritten after each change, in a class independent of `User` and of the main class.
- No attractions, groups, tickets, fast passes, encryption, databases, frameworks or external libraries.

## Capabilities

### New Capabilities
- `command-processing`: Reading text commands from the terminal, splitting them into operation and parameters, dispatching them and reporting invalid commands or parameter counts without crashing.
- `user-management`: User data model, field validation rules, uniqueness, and the create / delete / update operations.
- `user-persistence`: Storing users in a semicolon-separated text file and reconstructing them on startup.

### Modified Capabilities
<!-- None: project-build requirements are unchanged (the build must still pass `mvn test`). -->

## Impact

- New classes in `es.upm.parque` (main class rewritten from the archetype greeting to the command loop).
- New runtime data file `data/users.txt` (created on first save; ignored by Git).
- New unit tests covering validation, operations, persistence and command processing.
