# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists and that the password matches, and reject the deletion if the user has any active ticket (standalone or composing a group ticket) or belongs to a locked group; otherwise it SHALL remove the user from all groups, delete the user and update persistent data, printing `USER DELETED`. Historical tickets of the user SHALL be kept.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for an existing user with that password
- **THEN** the user is removed, the change is persisted and `USER DELETED` is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** the user is not deleted and an error message is printed

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed
- **THEN** an error message is printed and no data changes

#### Scenario: Active ticket blocks deletion
- **WHEN** Marta has a ticket dated today or later and `USER DELETE` is processed for her
- **THEN** an error is printed and Marta still exists

#### Scenario: Deletion after future ticket removed
- **WHEN** Marta's only ticket is dated after today, it is deleted with `TICKET DELETE`, and then `USER DELETE` is processed
- **THEN** Marta is deleted

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group with an active group ticket
- **THEN** `USER DELETE` is rejected with an error

#### Scenario: Removed from groups
- **WHEN** a user who belongs to an unlocked group and has only historical tickets is deleted
- **THEN** the user no longer appears in that group and their historical tickets still exist
