# Proposal

## Why

Milestone F1-C03 requires the `USER CREATE` command to add users, satisfying all previously defined data restrictions and keeping DNI and email unique. Until now users could only be registered programmatically, so no user-dependent command could be used from the command interface.

## What Changes

- `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` (seven fields after the keyword, parsed with `split`): validates DNI, email, password and card with the existing validator, rejects a DNI or email already used by a registered user, and only then creates and registers the user, storing the password in BCrypt-protected form and the phone as given. Confirmation: `USER CREATED`.
- Rejections show an error naming the problem and do not stop command processing; nothing is created.
- The new user is persisted like any other state change (normal mode; nothing remains with `--fly`).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: user creation command and DNI/email uniqueness.

## Impact

- `ParkController.createUser`, `CommandProcessor`, tests.

## Ambiguities (flagged)

- The source example wraps the password across two lines (`Clave1` / `!`) due to formatting; it is read as one command line with password `Clave1!`.
- First name, surnames and phone have no stated restrictions and are stored as given (trimmed of surrounding spaces like every field).
- Uniqueness is checked against currently registered users; email comparison is exact.
