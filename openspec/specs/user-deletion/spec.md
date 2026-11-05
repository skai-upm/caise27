# user-deletion Specification

## Purpose
Defines how a user is deleted with the `USER DELETE` command: identification by email, password verification, removal, and update of the persisted users.

## Requirements

### Requirement: USER DELETE command format
The system SHALL accept the command `USER DELETE;Email;Password` with exactly two data elements after the command name. A `USER DELETE` with a different number of elements MUST be rejected with an `ERROR` response and no user deleted.

#### Scenario: Missing password element
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an `ERROR` response is printed and no user is deleted

### Requirement: Deletion with correct credentials
The system SHALL locate the user by email, verify that it exists, check that the given password equals the user's password, delete the user, update the persisted users file, and print a response starting with `OK`.

#### Scenario: Matching email and password
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** a response starting with `OK` is printed
- **AND** the user no longer exists
- **AND** the users file no longer contains the user's line

#### Scenario: Deleted user's DNI and email become available
- **WHEN** a user has been deleted
- **THEN** a later `USER CREATE` with the same DNI and email succeeds

#### Scenario: Deletion survives restart
- **WHEN** a user is deleted and the application is started again
- **THEN** the deleted user is not loaded

### Requirement: Deletion refused without identification
The system MUST NOT delete a user when no user has the given email or when the given password is incorrect; it SHALL print an `ERROR` response and leave stored and persisted users unchanged.

#### Scenario: Incorrect password
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an `ERROR` response is printed
- **AND** the user still exists with unchanged data and the users file is unchanged

#### Scenario: Unknown email
- **WHEN** no user has email `nadie@correo.com` and `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an `ERROR` response is printed and the stored users are unchanged

### Requirement: Deleted user leaves all groups
When a user is deleted with `USER DELETE`, the system SHALL also remove that user from every group they belonged to, so that groups only contain existing users.

#### Scenario: Group membership removed on deletion
- **WHEN** `ana@correo.com` belongs to group `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** group `G01` no longer contains her
