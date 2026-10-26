# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored password; the user SHALL also be removed from every group it belongs to, and the persistent files SHALL then be updated. Otherwise no user SHALL be deleted and an error SHALL be reported.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the user is removed and no longer appears in the persistent file

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is received
- **THEN** the user is not deleted and an authentication error is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is received
- **THEN** no user is deleted and an error is printed

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of group `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** Ana is no longer a member of `G01`, including after a restart
