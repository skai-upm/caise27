# Proposal

## Why

Milestone F1-C03 makes the application usable: it must accept the `USER CREATE` command and add new users that satisfy the validation rules of F1-C02 while keeping DNI and email addresses unique. This also introduces the text-command interface every later operation uses.

## What Changes

- Introduce the command-based interface: each input line is a command whose elements are separated by `;`, interpreted with basic String operations including `split`, without external libraries. Malformed or unknown commands produce an error message and never terminate the application.
- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`: validates DNI, email, password and card number, rejects duplicate DNI or email, and stores the user in an in-memory user store; prints `USER CREATED` on success.
- Add a separate class responsible for storing and retrieving users (section 3.2), and a class coordinating interpretation, validation and creation, keeping `App` free of these responsibilities.
- Commit with the mandatory message `[F1-C03] Implement user creation`.

## Capabilities

### New Capabilities
- `command-interface`: Format and interpretation of text commands (`;`-separated elements, recognition of the command name, robust handling of incorrect commands).
- `user-registration`: The `USER CREATE` command — arguments, validation, uniqueness of DNI and email, and storage of the new user.

### Modified Capabilities
- None.

## Impact

- New packages for command handling, user service and user storage; `App.processLine` delegates to the command processor and prints its response.
- `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT` are recognized by later milestones (F1-C05, F1-C06, F1-C08); until then they are reported as unknown commands without terminating.
- Ambiguities flagged:
  - Exact output texts are not mandated (the integration example shows `USER CREATED`); errors are printed as `ERROR: <reason>`.
  - The statement's examples break `Clave1!` across two lines; this is treated as a layout artifact — a command is one line.
  - No rule is given for empty first name/surnames or for case sensitivity of emails; empty values are not specially rejected and email uniqueness compares the exact text.
  - Elements are trimmed of surrounding whitespace; blank input lines are ignored.
