# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user
`USER DELETE;Correo;Contraseña` SHALL find the user by email, check that it exists and that the supplied password matches the stored one, delete it and update the persistent file. If the email does not exist or the password is wrong, the user SHALL NOT be deleted and the response is `ERROR INVALID CREDENTIALS`. A user holding any ticket dated on or after the system date SHALL NOT be deleted and the response is `ERROR USER HAS ACTIVE TICKETS`; past tickets alone do not prevent deletion.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `OK USER DELETED` and the user no longer exists, also after restarting

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is processed for that user
- **THEN** the response is `ERROR INVALID CREDENTIALS` and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** the response is `ERROR INVALID CREDENTIALS`

#### Scenario: Current or future ticket blocks deletion
- **WHEN** the system date is 2026-10-01, Ana holds ticket T001 for 2026-10-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `ERROR USER HAS ACTIVE TICKETS` and Ana still exists

#### Scenario: Past ticket does not block deletion
- **WHEN** after that `SYSTEM DATE;2026-10-16` is processed and `USER DELETE;ana@correo.com;Clave1!` is processed again
- **THEN** the response is `OK USER DELETED` and ticket T001 is kept as a historical record
