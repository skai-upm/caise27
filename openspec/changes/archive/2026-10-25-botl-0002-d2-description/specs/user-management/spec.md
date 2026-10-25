# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists and that the password matches, verify that deletion is allowed by the user's tickets and groups, remove the user from every group they belong to, delete the user and update persistent data. On success the application SHALL print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed for an existing user with that password
- **THEN** the user is removed, the change is persisted and `USER DELETED` is printed

#### Scenario: Unknown user
- **WHEN** `USER DELETE` is processed with an email that belongs to no user
- **THEN** an error stating the user does not exist is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and an incorrect password
- **THEN** an error is printed and the user is not deleted

#### Scenario: Removed from groups
- **WHEN** a user belonging to unlocked group G02 is deleted
- **THEN** G02 no longer contains the user

## ADDED Requirements

### Requirement: Active tickets block user deletion
`USER DELETE` SHALL be rejected while any active ticket (date on or after the current system date) is associated with the user, whether an individual ticket or an individual ticket within a group ticket. Historical tickets alone SHALL NOT block deletion. The blocked state SHALL be derived from existing tickets and the current date, not stored.

#### Scenario: Future ticket blocks deletion
- **WHEN** Marta has ticket T002 dated 2027-04-20, the system date is 2027-04-15, and `USER DELETE` is requested for Marta
- **THEN** an error is printed and Marta is not deleted

#### Scenario: Deleting the future ticket unblocks
- **WHEN** `TICKET DELETE;T002` succeeds and `USER DELETE` is then requested for Marta
- **THEN** Marta is deleted

#### Scenario: Ticket for today blocks deletion
- **WHEN** the user has a ticket dated the current system date
- **THEN** `USER DELETE` is rejected

#### Scenario: Only historical tickets
- **WHEN** the user's only tickets are dated before the current system date
- **THEN** `USER DELETE` succeeds and the historical tickets are retained

### Requirement: Locked groups block user deletion
`USER DELETE` SHALL be rejected if the user belongs to a group that is locked by an active group ticket, because deleting the user would modify that group's composition.

#### Scenario: Member of locked group
- **WHEN** the user belongs to group G01, G01 has a group ticket dated after the current date that does not include the user, and `USER DELETE` is requested
- **THEN** an error is printed and the user is not deleted
