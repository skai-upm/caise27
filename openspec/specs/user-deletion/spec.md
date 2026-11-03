# user-deletion Specification

## Purpose

Deletes users through the `USER DELETE` command only after their email and password are verified, keeping storage in sync.

## Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify the user exists, check the password, delete the user, update persistent storage, and report `USER DELETED`.

#### Scenario: Correct credentials
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for an existing user with that password
- **THEN** the user is removed from memory and from the storage file and `USER DELETED` is reported

### Requirement: Deletion requires correct credentials
A `USER DELETE` with an unknown email or an incorrect password SHALL NOT delete any user.

#### Scenario: Wrong password
- **WHEN** `USER DELETE` names an existing email with an incorrect password
- **THEN** an error is reported and stored users are unchanged

#### Scenario: Unknown email
- **WHEN** `USER DELETE` names an email no user has
- **THEN** an error is reported and stored users are unchanged

### Requirement: Deletion constrained by tickets and groups
`USER DELETE` SHALL be rejected while the user has an active ticket (dated on or after the current system date), standalone or composing a group ticket, or belongs to a group locked by an active group ticket; when permitted, the user SHALL also be removed from all their groups, and historical tickets SHALL be retained.

#### Scenario: Active individual ticket
- **WHEN** a user with a ticket dated today requests deletion with correct credentials
- **THEN** the deletion is rejected

#### Scenario: Member of a locked group
- **WHEN** a user belongs to a group with an active group ticket and requests deletion
- **THEN** the deletion is rejected

#### Scenario: Only historical tickets
- **WHEN** a user whose tickets are all historical is deleted
- **THEN** the user is deleted, removed from their groups, and the historical tickets remain
