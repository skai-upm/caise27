# Proposal

## Why

The project is still the bare Quickstart skeleton (`com.parque.App` prints "Hello World!"). This change delivers the first functional slice of the terminal application: users can be created, modified and deleted through text commands, with full data validation, uniqueness by DNI and email, and their state survives restarts in a semicolon-separated text file.

## What Changes

- Add an encapsulated `User` model with first name, surname(s), DNI (immutable after creation), email, password, phone and card number.
- Add an enum of editable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); DNI is deliberately not editable.
- Add validation using only `String`/`Character` operations (no regular expressions): Spanish DNI with check-letter algorithm, basic `texto@texto.xxx` email, password with upper/lower/digit/symbol, 16-digit card.
- Add text commands `USER CREATE;…`, `USER DELETE;Correo;Contraseña` and `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, parsed with `String.split`, rejecting unknown commands and wrong parameter counts without terminating.
- Enforce DNI and email uniqueness on creation, and email uniqueness on update.
- Add a dedicated persistence class that reads/writes one user per line (`;`-separated) and reload stored users at startup.
- **Changes the entry point behaviour**: `App` no longer prints "Hello World!"; it reads commands from standard input, one per line, and prints one response per command.

## Capabilities

### New Capabilities
- `user-model`: The user's data, encapsulation, immutable DNI and the set of editable fields.
- `user-validation`: Validation rules for DNI, email, password and card number (and non-empty text fields).
- `user-management`: `USER CREATE` / `USER DELETE` / `USER UPDATE` command syntax, authentication by email+password, uniqueness, and safe rejection of invalid commands.
- `user-file-persistence`: Semicolon-separated user file, saved after every successful change and reloaded at startup.

### Modified Capabilities
- `application-bootstrap`: the requirement "No domain functionality in the bootstrap" is removed, since this change introduces the first domain functionality. The other bootstrap requirements still hold (the entry point exits with status 0 when its input ends).

## Impact

- **Code**: new classes in `com.parque` (`User`, `UserField`, `UserValidator`, `UserRepository`, `UserService`, `CommandProcessor`); `App` rewritten to a stdin command loop.
- **Data**: new file `users.txt` in the working directory (created on first successful change).
- **Dependencies**: none added (Java standard library only; JUnit 5 for tests is already present).
- **Assumptions recorded** (source does not specify): response texts (`OK: …` / `ERROR: …`); data file name/location; DNI check letter must be uppercase; first name, surname(s) and phone must be non-empty; commands with extra parameters are rejected like those with missing ones; stored passwords stay in plain text per the source.
