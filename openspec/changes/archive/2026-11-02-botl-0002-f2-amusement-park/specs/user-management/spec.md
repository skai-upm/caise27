# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL accept `USER DELETE;Correo;Contraseña`, find the user by email, and delete the user and update persistent storage only when the user exists, the password matches the stored password, and the user holds no ticket (standalone or constituent of a group ticket) dated on or after the system date. A deleted user SHALL be removed from every group; their past tickets SHALL be retained as history.

#### Scenario: Authenticated deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** the user is deleted and persistent storage no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` is processed with a nonexistent email or an incorrect password
- **THEN** an error message is shown and no user is deleted

#### Scenario: Current or future ticket blocks deletion
- **WHEN** `SYSTEM DATE;2026-10-01` is set, Ana has ticket `T001` dated 2026-10-15, and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** deletion is rejected and Ana and `T001` remain

#### Scenario: Ticket dated today blocks deletion
- **WHEN** the system date equals the date of Ana's ticket and Ana is deleted with valid credentials
- **THEN** deletion is rejected

#### Scenario: Historical ticket does not block deletion
- **WHEN** Ana's only ticket is dated 2026-10-15, `SYSTEM DATE;2026-10-16` is set, and Ana is deleted with valid credentials
- **THEN** Ana is deleted, removed from her groups, and the ticket remains as history
