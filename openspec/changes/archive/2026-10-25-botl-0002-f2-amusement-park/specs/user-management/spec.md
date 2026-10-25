# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user with credentials
`USER DELETE;Correo;Contraseña` SHALL delete the user with that email only if the user exists, the password matches and the user holds no ticket (individual or internal to a group ticket) dated on or after the system date. Deleting a user SHALL also remove the user from every group and delete the user's remaining past tickets, and SHALL update the persistent storage.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user has password `Clave1!`
- **THEN** the user is deleted, the storage no longer contains it and `OK` is reported

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** an `ERROR` is reported and the user is kept

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no such user exists
- **THEN** an `ERROR` is reported

#### Scenario: Current or future ticket blocks deletion
- **WHEN** the system date is 2026-10-01, Ana holds ticket T001 for 2026-10-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR` is reported and Ana is kept

#### Scenario: Past ticket does not block deletion
- **WHEN** the system date is 2026-10-16, Ana's only ticket is T001 for 2026-10-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted together with T001 and removed from her groups
