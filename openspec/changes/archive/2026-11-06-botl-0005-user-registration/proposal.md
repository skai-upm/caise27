# Proposal

## Why

Milestone F1-C03 (Delivery 1, step 5) requires the application to accept the `USER CREATE` command and add new users that satisfy the validation rules already defined (`valid-users`) and the uniqueness of DNI and email. The application currently only prints a start banner: there is no command interpretation and no place where users are kept.

## What Changes

- Read text commands interactively from standard input, one per line, and interpret them with basic String operations and `split` on `;` (no external libraries).
- Recognize `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and create the user only when all fields are valid and neither the DNI nor the email is already used by another user.
- Reply to every command with a success or error message; an incorrect or unknown command produces an error message and never terminates the application.
- Keep created users in memory for the current execution (persistence is milestone F1-C04).

### In scope
- Source sections 4 (command-based interface, as far as needed for `USER CREATE`), 5 (user creation and its validations) and 6 (uniqueness).

### Out of scope
- `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT` behavior (F1-C05, F1-C06, F1-C08), reading commands from a file (F1-C07), persistence (F1-C04).

## Capabilities

### New Capabilities
- `command-interface`: how text commands are received from standard input, split into fields and dispatched, and how incorrect commands are handled without terminating the application.
- `user-registration`: the `USER CREATE` command, its field validation and the DNI/email uniqueness rules.

### Modified Capabilities
<!-- None. valid-users rules are reused unchanged; user-management requirements are unchanged. -->

## Impact

- `Main` gains an interactive read loop; new command-processing, user-service and in-memory user-store classes; new tests. `MainTest` adapts to supply standard input.
- No new dependencies or data stores.
