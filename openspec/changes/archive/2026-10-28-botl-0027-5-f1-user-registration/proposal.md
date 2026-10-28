# Proposal

## Why

Milestone F1-C03 requires the `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` command to add new users after validating their data with all previously defined restrictions and preserving the uniqueness of DNI and e-mail. The user model, validation rules, password protection and persistence already exist, but no command registers users.

## What Changes

- Add a registration operation in the park controller: validate DNI, e-mail, password and card (existing validator), check that no active user has the same DNI or e-mail (both checked before creating), protect the password with BCrypt and register the user with all attributes; nothing is stored if any check fails.
- Add the `USER CREATE` command (seven fields after the keyword) with the success message `USER CREATED`; incorrect requests are reported without stopping command processing.
- The created user is persisted by the existing save-after-command mechanism.
- Commit with the exact message `[F1-C03] Implement user creation`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `user-management`: adds the USER CREATE command and DNI/e-mail uniqueness.

## Impact

- `ParkController.registerUser`, `UserCreateCommand`, command registration, tests.
- **Flagged ambiguities:**
  - The source example shows the password `Clave1!` broken across two lines (`Clave1` / `!`), an artefact of the source text; commands are processed one per line, so the example is treated as the single line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`.
  - Uniqueness is checked against active users; historical ticket records of deleted users do not block reusing their DNI or e-mail.
  - E-mail uniqueness is case-sensitive (no normalisation rule is given).
