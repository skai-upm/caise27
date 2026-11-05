# user-deletion Specification

## Purpose

Defines the `USER DELETE` operation, which removes a registered user identified by email address once the password is confirmed, and keeps persisted users consistent with the deletion.

## Requirements

### Requirement: USER DELETE command
The application SHALL accept `USER DELETE;Email;Password`. It SHALL locate the user by email, verify that the user exists, check that the password matches the user's password, delete the user and print `USER DELETED`.

#### Scenario: Matching email and password delete the user
- **GIVEN** a registered user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed and no user with email `ana@correo.com` remains

#### Scenario: Deleted user's DNI and email can be registered again
- **GIVEN** the user with DNI `12345678Z` and email `ana@correo.com` was deleted
- **WHEN** a valid `USER CREATE` with that DNI and email is processed
- **THEN** `USER CREATED` is printed

### Requirement: Deletion requires identification
A deletion SHALL be refused with an error message starting with `ERROR`, leaving all users unchanged, when no user has the given email or when the password does not match. An incorrect password MUST NOT allow the user to be deleted.

#### Scenario: Incorrect password rejected
- **GIVEN** a registered user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** an error message starting with `ERROR` is printed and the user is still registered

#### Scenario: Unknown email rejected
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and the registered users are unchanged

#### Scenario: Wrong number of elements rejected
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and no user is deleted

### Requirement: Deletion is persisted
A successful deletion SHALL be reflected in the persisted users file, so that the deleted user is not loaded on the next start-up; a rejected deletion MUST leave the file unchanged.

#### Scenario: Deleted user absent after restart
- **GIVEN** users Ana and Pedro stored in `users.txt`
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and the application is restarted
- **THEN** `users.txt` contains only Ana's line and only Ana is loaded

### Requirement: Deleted users leave their groups
When a user is deleted, the system SHALL also remove that user from every group it belongs to, so that groups only contain registered users.

#### Scenario: Member deleted
- **GIVEN** Ana belongs to group `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana

### Requirement: Active tickets block user deletion
A user SHALL NOT be deleted while any active ticket (date equal to or later than the current system date) is associated with them; the deletion is rejected with an error message starting with `ERROR` and nothing changes. Historical tickets (earlier dates) SHALL NOT prevent deletion and SHALL be retained after the user is deleted. The blocked state is derived from the existing tickets and the current date, not stored in the user.

#### Scenario: Active ticket blocks deletion
- **GIVEN** current date 2027-04-01 and Ana has ticket `T001` on 2027-04-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana is still registered

#### Scenario: Ticket for today blocks deletion
- **GIVEN** current date 2027-04-15 and Ana has ticket `T001` on 2027-04-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the deletion is rejected

#### Scenario: Only historical tickets
- **GIVEN** Ana's only ticket `T001` is dated 2027-04-15 and the current date is 2027-04-16
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed and ticket `T001` is still retained

#### Scenario: Deleting the future ticket unblocks the user
- **GIVEN** current date 2027-04-01 and Ana's only ticket `T001` on 2027-04-15
- **WHEN** `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** both succeed

### Requirement: Group tickets block user deletion
`USER DELETE` SHALL be rejected with an error message starting with `ERROR` when the user has an active individual ticket that belongs to a group ticket composition, or when the user belongs to a group that is locked by an active group ticket, since deleting the user would modify that group. If all the user's groups are modifiable, the user is removed from them as part of the deletion.

#### Scenario: Active composition ticket blocks deletion
- **GIVEN** Ana is in the composition of active group ticket `TG01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana is still registered

#### Scenario: Member of a locked group not deleted
- **GIVEN** Ana belongs to group `G01`, which is locked by the active group ticket `TG01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana remains a member of `G01`
