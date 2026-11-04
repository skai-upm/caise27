# Spec Delta

## MODIFIED Requirements

### Requirement: Authenticated deletion
The system SHALL process `USER DELETE;Correo;Contraseña` by locating the user by email, verifying the password matches the stored password, removing the user and updating the persistent file. Deletion SHALL be rejected while the user owns any ticket dated on or after the current system date; tickets dated before it SHALL NOT, by themselves, prevent deletion.

#### Scenario: Successful deletion
- **GIVEN** an existing user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `OK` message is produced and the user no longer exists in memory or in the file

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an `ERROR` message is produced and no user is deleted

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed, or the system date is 2026-10-15 and it is processed
- **THEN** an `ERROR` message is produced and Ana is not deleted

#### Scenario: Past ticket does not block deletion
- **GIVEN** ticket `T001` for Ana dated 2026-10-15
- **WHEN** `SYSTEM DATE;2026-10-16` is processed and then `USER DELETE;ana@correo.com;Clave1!`
- **THEN** Ana is deleted and ticket `T001` is retained as history
