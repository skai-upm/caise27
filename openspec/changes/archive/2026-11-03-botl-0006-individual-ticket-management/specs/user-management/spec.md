# Spec Delta

## MODIFIED Requirements

### Requirement: Authenticated deletion
The system SHALL process `USER DELETE;Correo;Contraseña` by locating the user by email, verifying the password matches the stored one, and only then removing the user. The deletion SHALL be rejected while the user has any ticket dated on or after the current system date; tickets dated before the system date SHALL NOT, by themselves, prevent the deletion.

#### Scenario: Successful deletion
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user no longer exists

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error is reported and no user is deleted

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Ticket for the system date blocks deletion
- **GIVEN** Ana's ticket dated 2026-10-15 and system date 2026-10-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Past ticket does not block deletion
- **GIVEN** Ana's only ticket is dated 2026-10-15 and `SYSTEM DATE;2026-10-16` is processed
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted
