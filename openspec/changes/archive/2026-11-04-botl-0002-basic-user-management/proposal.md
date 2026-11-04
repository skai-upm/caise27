# Proposal

## Why

The project is an empty Maven skeleton. This change delivers the first functional slice of the terminal application: users can be created, updated and deleted through text commands, with validated data, unique identities and state that survives a restart.

## What Changes

- Add an encapsulated user model (first name, surnames, immutable DNI, email, password, phone, card number).
- Add text commands `USER CREATE`, `USER DELETE` and `USER UPDATE`, parsed with `String.split` only.
- Validate DNI (check letter), email (basic `texto@texto.xxx` shape), password (upper, lower, digit, symbol) and card number (exactly 16 digits) without regular expressions or external libraries; phone is stored as given.
- Reject duplicate DNI or email, on creation and on email change.
- Authenticate `USER DELETE`/`USER UPDATE` with current email and password; editable fields are an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); DNI is never editable.
- Persist users in a semicolon-separated text file (one user per line) through a dedicated persistence class; reload them on startup.
- The application reads commands from the terminal (standard input) and prints one result line per command; invalid commands never terminate it.

## Capabilities

### New Capabilities
- `user-model`: the user record, its fields, encapsulation and DNI immutability.
- `user-validation`: validation rules for DNI, email, password and card number.
- `user-management`: the `USER CREATE` / `USER DELETE` / `USER UPDATE` commands, their parsing, authentication, uniqueness and error handling.
- `user-file-persistence`: the text-file format and restart recovery of users.

### Modified Capabilities
- `application-bootstrap`: the "No domain functionality in the bootstrap" requirement is removed, because this change intentionally introduces domain functionality.

## Impact

- New production classes in `es.upm.etsisi.amusementpark`; `App` changes from "Hello World" to a terminal command loop.
- New data file `users.txt` in the working directory (ignored by Git).
- No new dependencies; tests use the existing JUnit 5 setup.
- Out of scope: HELP/EXIT, file-input mode, attractions, groups, tickets, passes, encryption.
