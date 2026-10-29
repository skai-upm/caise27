# Spec Delta

## ADDED Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that it exists, check the password against the stored protected value, and only then delete the user and update persistent data. An unknown user, a wrong password, a wrong number of fields, or a user that the ticket rules forbid deleting SHALL be rejected with an error message and nothing SHALL change.

#### Scenario: Source example
- **WHEN** user `ana@correo.com` with password `Clave1!` exists without active tickets and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is shown, the user no longer exists, and after a restart it is still absent

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;clave1!` is processed
- **THEN** an error message is shown and the user still exists

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message is shown

#### Scenario: Active ticket blocks deletion
- **WHEN** the user has a ticket for the current date and the correct password is given
- **THEN** an error message is shown and the user still exists

### Requirement: Deleted user leaves its groups
When a user is deleted, the system SHALL remove the user from every group it belongs to, while historical tickets SHALL keep the information needed to reconstruct them.

#### Scenario: Group membership removed, history kept
- **WHEN** `pedro@correo.com` belongs to group `G01`, had a group ticket that is now historical, and is deleted
- **THEN** `G01` no longer contains him, and the historical group ticket still lists his individual ticket with his email and DNI after a restart
