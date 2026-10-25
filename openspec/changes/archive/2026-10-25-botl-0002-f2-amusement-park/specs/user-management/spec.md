# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user command
The system SHALL accept `USER DELETE;Correo;Contraseña`, find the user by email, check that the password matches, delete the user and update the persistent storage. If the email does not exist or the password is wrong, no user SHALL be deleted. The deletion SHALL also be rejected while the user holds any ticket dated on or after the system date. A deleted user SHALL be removed from every group they belonged to.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** the system reports `OK USER DELETED` and the user no longer exists, also after restarting the application

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is executed for a user whose password is `Clave1!`
- **THEN** the system reports an error and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is executed and no such user exists
- **THEN** the system reports an error and no user is deleted

#### Scenario: User with an active ticket
- **WHEN** the system date is 2026-10-01, Ana has a ticket for 2026-10-15 and `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** the system reports an error and Ana still exists

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of G01, has no current or future tickets and is deleted
- **THEN** G01 no longer contains Ana
