# Proposal

## Why

Milestone F1-C03 makes the application usable for its first real operation: registering users with `USER CREATE`. Until now the interactive session reads lines without interpreting them, and user validation exists only as a reusable rule set. This milestone connects input, validation and user storage, enforcing DNI/email uniqueness.

## What Changes

- Interpret each input line as a command whose elements are separated by `;`, using String operations and `split` (no external libraries).
- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`: validates the data with the existing `user-validation` rules, rejects duplicates of DNI or email, and stores the user. Prints `USER CREATED` on success or an `ERROR: ...` message on rejection.
- An unrecognised or malformed command prints an error message and the application keeps running (it MUST NOT terminate unexpectedly).
- Add a separate user storage component (in memory for now) so that the user class and main class do not store users.
- Out of scope: `USER DELETE` (F1-C05), `USER UPDATE` (F1-C06), `HELP`/`EXIT` (F1-C08), persistence across runs (F1-C04), command file input (F1-C07).

## Capabilities

### New Capabilities
- `command-interface`: How text commands are read, split into elements and dispatched, including handling of unrecognised commands.
- `user-registration`: The `USER CREATE` operation with validation and DNI/email uniqueness.

### Modified Capabilities
- `executable-application`: the interactive mode now processes each line read as a command instead of only consuming it.

## Impact

- New packages `command`, `service`, `repository`; `InteractiveSession` now delegates each line to the command processor and writes responses to standard output.
- `user-domain` and `user-validation` are reused unchanged.
