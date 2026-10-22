# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
The `USER DELETE;Email;Password` command SHALL locate the user by email, verify the password, and reject the deletion while the user holds an active ticket (stand-alone or part of a group ticket) or belongs to a locked group. Otherwise it SHALL remove the user from every group, delete the user, keep their historical tickets, update persistent data and print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed for an existing user with that password
- **THEN** the user is removed and `USER DELETED` is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an incorrect password
- **THEN** an error is printed and the user is not deleted

#### Scenario: Unknown user
- **WHEN** `USER DELETE` is processed with an email that belongs to no user
- **THEN** an error is printed

#### Scenario: Active individual ticket
- **WHEN** `USER DELETE` is processed for a user holding a ticket dated on or after the system date
- **THEN** an error is printed and the user is not deleted

#### Scenario: Member of a locked group
- **WHEN** `USER DELETE` is processed for a user whose group has an active group ticket
- **THEN** an error is printed and the user is not deleted

#### Scenario: Deletion after removing a future ticket
- **WHEN** Marta's only ticket T002 is in the future, `TICKET DELETE;T002` is processed and then `USER DELETE` for Marta
- **THEN** Marta is deleted

#### Scenario: Removed from unlocked groups
- **WHEN** a user who belongs to an unlocked group is deleted
- **THEN** the group no longer contains the user
