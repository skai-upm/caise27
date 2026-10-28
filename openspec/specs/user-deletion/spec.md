# user-deletion Specification

## Purpose
Defines deletion of users through the `USER DELETE` command, which requires identifying the user by email address and the correct password, and keeps persistent data in sync.

## Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check that the password matches, delete the user, update the persistent users file and print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed, the user no longer exists, and the users file no longer contains the user's line

#### Scenario: Deleted user's DNI and email become available
- **WHEN** a user has been deleted
- **THEN** a new `USER CREATE` with the same DNI and email succeeds

### Requirement: Deletion requires an existing user
If no user has the given email, the deletion SHALL be rejected with an `ERROR:` message and nothing SHALL change.

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no user has that email
- **THEN** an `ERROR:` line reporting that the user was not found is printed and users and file are unchanged

### Requirement: Incorrect password prevents deletion
If the password does not match the user's password, the user SHALL NOT be deleted and an `ERROR:` message SHALL be printed.

#### Scenario: Wrong password
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an `ERROR:` line reporting an incorrect password is printed and the user still exists, also in the users file

### Requirement: Deleted users leave their groups
When a user is deleted, the user SHALL also be removed from every group it belonged to, so that groups only contain existing users.

#### Scenario: Member is deleted
- **WHEN** Ana belongs to group `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana

### Requirement: Active tickets block user deletion
`USER DELETE` SHALL be rejected with an `ERROR:` message while any ticket associated with the user (an individual ticket, including those comprising a group ticket) is dated on or after the current system date. Historical tickets alone SHALL NOT prevent deletion. The user's locked state SHALL be derived from the existing tickets and the current system date, never stored.

#### Scenario: User with a ticket for today
- **WHEN** the current system date is 2027-04-15, Ana has ticket `T001` dated 2027-04-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` line about active tickets is printed and Ana still exists

#### Scenario: User with only historical tickets
- **WHEN** Ana's only ticket is dated before the current system date and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed and the historical ticket is kept with its user information

#### Scenario: Future ticket deleted first
- **WHEN** Ana's only ticket is dated after the current system date, `TICKET DELETE` removes it, and then `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed

### Requirement: Locked groups block user deletion
`USER DELETE` SHALL be rejected with an `ERROR:` message if the user belongs to a group that is locked by an active group ticket, because removing the user would modify that group's composition.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to a group with a group ticket dated on or after the current system date and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` line about the locked group is printed and Ana still exists and remains in the group
