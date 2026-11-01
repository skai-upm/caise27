# Spec Delta

## MODIFIED Requirements

### Requirement: Authenticated user deletion
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL find the user by email, check that the supplied password equals the stored password, and check that the user holds no individual or group-constituent ticket dated on or after the current system date; only then SHALL it delete the user, remove them from any group and attraction, and update persistent storage. Past tickets of the user SHALL be kept as history.

#### Scenario: Authenticated deletion
- **GIVEN** an existing user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted, persistence is updated, and a success message is reported

#### Scenario: Deletion with wrong credentials
- **GIVEN** an existing user
- **WHEN** `USER DELETE` receives an incorrect password or a nonexistent email
- **THEN** an error message is reported and no user is deleted

#### Scenario: Current or future ticket blocks user deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and a ticket dated `2026-10-15` for Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected and the user and ticket remain intact

#### Scenario: Ticket dated today blocks user deletion
- **GIVEN** `SYSTEM DATE;2026-10-15` and a ticket dated `2026-10-15` for Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected

#### Scenario: Historical ticket no longer blocks deletion
- **GIVEN** a ticket dated `2026-10-15` for Ana and system date changed to `2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** Ana is deleted, removed from her groups, and the ticket remains as a historical record

#### Scenario: Group-ticket constituent blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and a group ticket dated `2026-10-15` that includes Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected
