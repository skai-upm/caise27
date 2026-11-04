## MODIFIED Requirements

### Requirement: Delete user
The system SHALL process `USER DELETE;Correo;Contraseña`, deleting the user only if a user with that email exists and the password matches the stored one. A deleted user SHALL also be removed from the members of every group.

#### Scenario: Successful deletion
- **GIVEN** an existing user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is removed and an `OK:` response is printed

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` response is printed and no user is removed

#### Scenario: Deleted user leaves their groups
- **GIVEN** Ana is a member of `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `G01` still exists and no longer contains Ana, also after a restart
