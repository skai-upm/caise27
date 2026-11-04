# Spec Delta

## MODIFIED Requirements

### Requirement: Authenticated deletion
The system SHALL process `USER DELETE;Correo;Contraseña` by locating the user by email, verifying the password matches, removing the user and updating the persistent file; it SHALL NOT delete anything when the email does not exist, the password is wrong, or the user has any ticket dated on or after the configured system date. Tickets dated before the system date SHALL NOT, by themselves, prevent deletion.

#### Scenario: Successful deletion
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is removed and is absent after restart

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** the command is rejected and stored users are unchanged

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed, and again after `SYSTEM DATE;2026-10-15`
- **THEN** both deletions are rejected and Ana remains registered

#### Scenario: Past ticket alone does not block
- **GIVEN** Ana's only ticket `T001` is dated `2026-10-15`
- **WHEN** `SYSTEM DATE;2026-10-16` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** Ana is deleted and ticket `T001` is retained as history
