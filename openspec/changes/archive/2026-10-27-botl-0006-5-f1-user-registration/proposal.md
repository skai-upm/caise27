# Proposal

## Why

Milestone F1-C03 makes the application accept `USER CREATE` to add new users. The operation must apply all user data restrictions already defined (F1-C02) and keep DNI and email addresses unique, using the existing classes cooperatively to interpret the request, validate the data and create the user.

## What Changes

- Commands are interpreted by splitting on `;` with basic String operations (no external libraries); the first element is the command name.
- New `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` command: validates the data, checks DNI and email uniqueness, then stores the user and prints `USER CREATED`.
- Rejected creations (wrong number of fields, invalid data, duplicate DNI or email) print an error and store nothing; the application keeps running.
- Record the mandatory milestone commit `[F1-C03] Implement user creation` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `user-management`: adds user creation through `USER CREATE`, validation before storage, and DNI/email uniqueness.
- `command-processing`: adds `;`-separated command interpretation and states that incorrect commands never terminate the application.

## Impact

- New `UserService` (create user: validate, check uniqueness, store) and `UserOperationException`.
- `CommandProcessor` dispatches `USER CREATE`; `UserRepository` gains DNI lookup.
- `USER DELETE`/`USER UPDATE` remain for F1-C05/F1-C06; file persistence for F1-C04.
