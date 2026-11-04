# Proposal

## Why

The project is only a bootstrapped Maven skeleton. This change delivers the first functional slice of the terminal application: users can be created, updated and deleted through API-like text commands, with validated data, uniqueness by DNI and email, and file-backed state that survives restarts.

## What Changes

- Add an encapsulated user model (first name, surnames, DNI, email, password, phone, card number) whose DNI cannot change after creation.
- Add text commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, split with `String.split` (no external parsing libraries, no regular-expression validation).
- Validate DNI (8 digits + correct check letter), email (basic `texto@texto.xxx` shape), password (uppercase, lowercase, digit, symbol) and card (exactly 16 digits); store phone as supplied.
- Reject users whose DNI or email already exists, including email changes during update.
- Authenticate deletion and update with the current email and password; editable fields are represented by an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- Persist users as one semicolon-separated line each in a text file, written by a dedicated persistence class and reloaded on startup.
- Invalid or malformed commands are rejected with an error message and never terminate the application or partially create/modify a user.
- Remove the bootstrap-only "No premature domain functionality" requirement, which no longer holds once domain features exist.

Source change ID suggestion `add-basic-user-management` is replaced by the BOTL ID `botl-0002-2-f1-basic-user-management`.

## Capabilities

### New Capabilities
- `user-model`: User data held by the system, encapsulation and DNI immutability.
- `user-validation`: Validation rules for DNI, email, password, card and required fields.
- `user-management`: Create, delete and update operations, uniqueness and authentication.
- `user-file-persistence`: Semicolon-separated text-file storage and restart recovery of users.
- `command-processing`: Reading API-like text commands from the terminal, splitting them, dispatching supported commands and rejecting invalid ones safely.

### Modified Capabilities
- `application-bootstrap`: remove the "No premature domain functionality" requirement (it scoped only the bootstrap change).

## Impact

- New production classes in `es.upm.park` (user model, field enum, validator, user manager, file store, command processor); `App` becomes the terminal loop.
- New data file `users.txt` in the working directory at runtime (not committed).
- No new dependencies; Java standard library only. Passwords remain unencrypted by design in this delivery.
