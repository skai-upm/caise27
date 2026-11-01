# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user command
The system SHALL accept `USER DELETE;Correo;Contraseña`, locate the user by email, and delete it only if the password matches the stored password, then update persistent storage. An unknown email or wrong password SHALL leave all users unchanged. Deletion SHALL also be rejected while the user holds any ticket (individual or constituent of a group ticket) dated on or after the system date, or belongs to a group locked by a group ticket. A deleted user SHALL be removed from every group and attraction; their historical tickets SHALL be retained.

#### Scenario: Authenticated deletion
- **GIVEN** user `ana@correo.com` with password `Clave1!` exists
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the user file no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` is processed with a nonexistent email or an incorrect password
- **THEN** no user is deleted

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and ticket `T001` for `ana@correo.com` dated `2026-10-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** deletion is rejected and both the user and the ticket remain

#### Scenario: Ticket dated today blocks deletion
- **GIVEN** the system date equals the date of a ticket held by the user
- **WHEN** the user is deleted
- **THEN** deletion is rejected

#### Scenario: Historical ticket does not block deletion
- **GIVEN** ticket `T001` for `ana@correo.com` dated `2026-10-15` and the system date changed to `2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and ticket `T001` is still retained as a historical record, also after a restart

#### Scenario: Deleted user leaves groups
- **GIVEN** an unlocked group containing the user
- **WHEN** the user is deleted
- **THEN** the group no longer lists the user, also after a restart
