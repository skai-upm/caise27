# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored one; the persistent file SHALL then be updated. A deleted user SHALL also be removed from every group it belonged to.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and the credentials match
- **THEN** the user is removed and no longer appears in the persistent file

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE` is processed with an unknown email or a wrong password
- **THEN** an error is reported and no user is deleted

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana, also after a restart
