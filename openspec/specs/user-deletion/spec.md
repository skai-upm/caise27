# user-deletion Specification

## Purpose

Defines the `USER DELETE` command that removes a registered user after identification by email and password, keeping persistent data in sync.

## Requirements

### Requirement: USER DELETE syntax
The command `USER DELETE;Email;Password` SHALL require exactly two non-empty fields; otherwise it SHALL be rejected with a format error and no user is deleted.

#### Scenario: Missing password
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error is reported and no user is deleted

### Requirement: Deletion after identification
The application SHALL locate the user by email, verify the user exists, check the password and only then delete the user, responding `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `USER DELETED` and no user with email `ana@correo.com` or DNI `12345678Z` remains

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no user has that email
- **THEN** an error indicating the user does not exist is reported

#### Scenario: Incorrect password
- **WHEN** user `ana@correo.com` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error indicating an incorrect password is reported and the user is not deleted

### Requirement: Deletion persisted
A successful deletion SHALL update the persistent user information so that the user is absent after the application restarts.

#### Scenario: Deleted user absent after restart
- **WHEN** a user is deleted and the application is started again
- **THEN** the user file no longer contains that user and a USER CREATE reusing its DNI and email succeeds

### Requirement: Active tickets block user deletion
USER DELETE SHALL be rejected while any ticket dated on or after the current system date is associated with the user; tickets dated before the current date SHALL NOT prevent deletion. This state SHALL be derived from the existing tickets and the system date, not stored on the user.

#### Scenario: User with a future ticket
- **WHEN** the system date is 2027-04-10, Ana has ticket `T001` for 2027-04-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error about active tickets is reported and Ana is not deleted

#### Scenario: Future ticket deleted first
- **WHEN** Ana's only active ticket `T001` is deleted with `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `USER DELETED`

#### Scenario: Only historical tickets
- **WHEN** Ana's only ticket is dated 2027-04-15 and the system date has been moved to 2027-04-16
- **THEN** `USER DELETE;ana@correo.com;Clave1!` responds `USER DELETED` and the historical ticket is still kept

### Requirement: Members of locked groups cannot be deleted
USER DELETE SHALL be rejected when the user belongs to a group that has an associated group ticket dated on or after the current system date; when every group of the user is modifiable, the user is removed from them as part of the deletion.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to `G01`, which has an active group ticket, and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is reported and Ana is neither deleted nor removed from `G01`

#### Scenario: Component ticket counts as active ticket
- **WHEN** Ana's only active ticket is the component `TG01:12345678Z` of a future group ticket
- **THEN** USER DELETE for Ana is rejected
