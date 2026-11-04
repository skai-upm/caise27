# Proposal

## Why

The project is only a Java 21 Maven skeleton (`application-bootstrap`). The first functional slice of the terminal application is basic user management: users must be created with validated data, modified or deleted only with their current credentials, kept unique by DNI and email, and survive application restarts through a plain text file.

## What Changes

- Add an encapsulated user model (first name, surnames, DNI, email, password, phone, card number) whose DNI cannot change after creation.
- Add text commands `USER CREATE`, `USER DELETE` and `USER UPDATE`, split with `String.split` (no external parsing libraries); invalid or incomplete commands are rejected without terminating the program.
- Validate Spanish DNI (8 digits + correct check letter), basic email format without regular expressions, password character categories, and 16-digit card numbers; the phone is stored as supplied.
- Reject users whose DNI or email already exists, and email updates that collide with another user.
- Authenticate deletion and update by current email + password; updatable fields are represented by an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- Persist users to a semicolon-separated text file (one user per line) through a dedicated persistence class, and reload them at startup.
- The main class reads commands from the terminal (standard input) and prints one result line per command.

## Capabilities

### New Capabilities
- `user-model`: The user's data, encapsulation and immutability of the DNI.
- `user-validation`: Validation rules for DNI, email, password, card number and phone.
- `user-management`: `USER CREATE/DELETE/UPDATE` command syntax, uniqueness, authentication and safe rejection of invalid commands.
- `user-file-persistence`: Semicolon-separated text-file storage of users and their recovery at startup.

### Modified Capabilities
_None._ (`application-bootstrap` requirements are preserved; its "no domain functionality in bootstrap" requirement described the bootstrap change and is not contradicted by adding this epic — see design.md.)

## Impact

- New production classes in `es.upm.park` (user, field enum, validator, user manager, command processor, file storage); `App` becomes the terminal loop.
- New JUnit 5 tests; still only the Java standard library at runtime.
- A `users.txt` data file is created in the working directory at runtime.
- Out of scope: HELP/EXIT and file-input mode, attractions, groups, tickets, passes, encryption, databases, regular expressions, frameworks.
