# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL locate the user by email, verify the password matches, delete the user and update persistent storage. If the email does not exist or the password is wrong, nothing SHALL be deleted. Deletion SHALL also be rejected while the user has any ticket (individual or within a group ticket) dated on or after the system date. A deleted user SHALL be removed from every group; past tickets SHALL be kept as history.

#### Scenario: Authenticated deletion
- **GIVEN** a user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the persistent file no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` receives a nonexistent email or an incorrect password
- **THEN** no user is deleted and an error message is reported

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and ticket `T001` for Ana dated 2026-10-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected and Ana and `T001` remain

#### Scenario: Historical ticket does not block deletion
- **GIVEN** the ticket above and `SYSTEM DATE;2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** Ana is deleted, removed from her groups, and `T001` remains as a historical record

#### Scenario: Group ticket member cannot be deleted
- **GIVEN** Ana belongs to a group ticket dated on or after the system date
- **WHEN** Ana's deletion is requested
- **THEN** deletion is rejected
