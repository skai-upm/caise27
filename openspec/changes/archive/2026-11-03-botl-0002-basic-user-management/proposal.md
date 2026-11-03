# Proposal

## Why

The project is a bare Quickstart skeleton. The first functional epic gives the terminal application validated user creation, authenticated update and deletion, and file-backed persistence across runs, driven by API-like text commands.

## What Changes

- Encapsulated user model (first name, surnames, immutable DNI, email, password, phone, card number).
- Text commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, read from the terminal and split with `String.split`; invalid or incomplete commands are rejected without terminating the program.
- Validation without regular expressions: Spanish DNI check letter, basic email shape with 3-character extension, password character categories, 16-digit card number; phone stored as given.
- Uniqueness of DNI and email on creation, and of email on update.
- Users stored one per line, `;`-separated, in a text file by a class independent of `User` and of the main class; reloaded at startup.
- Update/delete authenticated with current email and password; editable fields represented by an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- **BREAKING (spec):** the `application-bootstrap` requirement "No domain functionality" is removed, since domain functionality is now introduced.

## Capabilities

### New Capabilities
- `user-model`: user data held by the system, encapsulation and DNI immutability.
- `user-validation`: acceptance rules for DNI, email, password, card number and phone.
- `user-management`: the `USER CREATE/DELETE/UPDATE` command contract, uniqueness, authentication and malformed-command handling.
- `user-file-persistence`: line-per-user text file format, saving after successful changes and reloading at startup.

### Modified Capabilities
- `application-bootstrap`: remove "No domain functionality" (superseded by this epic).

## Impact

- New production classes in `es.upm.etsisi.amusementpark`; `App` becomes a terminal command loop.
- New file `users.txt` in the working directory at runtime (path injectable for tests).
- Only the Java standard library; JUnit 5 tests. No HELP/EXIT, file-input mode, encryption, groups, tickets or attractions.
