# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored one, printing `OK USER DELETED`; otherwise it SHALL print an `ERROR` line and keep the user. A deleted user SHALL also be removed from every group they belonged to. A user who has any ticket dated on or after the system date SHALL NOT be deleted.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for an existing user with that password
- **THEN** the user is removed, the file is updated and `OK USER DELETED` is printed

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE` is processed with an unknown email or a wrong password
- **THEN** no user is deleted and an `ERROR` line is printed

#### Scenario: Deleted user leaves groups
- **WHEN** Ana belongs to group `G01` and is deleted
- **THEN** `G01` no longer contains Ana, also after a restart

#### Scenario: Current or future ticket blocks deletion
- **WHEN** the system date is 2026-10-01, Ana has ticket `T001` for 2026-10-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is not deleted and an `ERROR` line is printed

#### Scenario: Past ticket does not block deletion
- **WHEN** the system date is then set to 2026-10-16 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted
