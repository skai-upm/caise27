# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion command
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL find the user by email, check the supplied password matches the stored password, and only then delete the user and update persistent storage. Deletion SHALL be rejected while the user holds any ticket (individual or group constituent) dated on or after the system date; past tickets alone SHALL NOT block it. A deleted user SHALL be removed from every group it belonged to.

#### Scenario: Authenticated deletion
- **WHEN** a user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the persistent store no longer contains it

#### Scenario: Wrong password or unknown email on delete
- **WHEN** `USER DELETE` is processed with an incorrect password or an email that is not registered
- **THEN** no user is deleted and an error message is reported

#### Scenario: Current or future ticket blocks deletion
- **WHEN** `SYSTEM DATE;2026-10-01` is set, `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** deletion is rejected and Ana and `T001` remain

#### Scenario: Historical ticket does not block deletion
- **WHEN** the system date is then changed to `2026-10-16` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and `T001` remains as a historical record

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of an unlocked group `G01` and is deleted
- **THEN** `G01` no longer contains Ana, including after a restart
