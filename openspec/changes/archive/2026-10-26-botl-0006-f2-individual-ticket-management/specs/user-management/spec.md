# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored one; the persistent file SHALL then be updated. A deleted user SHALL also be removed from every group it belonged to. Deletion SHALL be rejected while the user holds any ticket dated on or after the system date; past tickets SHALL NOT prevent deletion and SHALL remain as history.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and the credentials match
- **THEN** the user is removed and no longer appears in the persistent file

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE` is processed with an unknown email or a wrong password
- **THEN** an error is reported and no user is deleted

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana, also after a restart

#### Scenario: Current or future ticket blocks deletion
- **WHEN** `SYSTEM DATE;2026-10-01`, `TICKET CREATE;T001;ana@correo.com;2026-10-15` and `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the deletion is rejected

#### Scenario: Only past tickets
- **WHEN** after the previous scenario `SYSTEM DATE;2026-10-16` and `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** Ana is deleted and T001 remains stored as history
