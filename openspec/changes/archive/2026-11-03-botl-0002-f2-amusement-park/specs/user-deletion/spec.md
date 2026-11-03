# Spec Delta

## MODIFIED Requirements

### Requirement: Delete user command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check that the password matches, check that no active ticket or locked group prevents deletion, remove the user from the groups they belong to, delete the user, update the persistent information, and report success (for example `USER DELETED`).

#### Scenario: Correct credentials delete user
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is removed from the system and from the user data file, and success is reported

#### Scenario: Deleted user removed from unlocked groups
- **WHEN** Ana belongs to unlocked group `G01`, has no active tickets, and is deleted
- **THEN** Ana is no longer a member of `G01`

## ADDED Requirements

### Requirement: Active tickets block user deletion
`USER DELETE` SHALL be rejected while the user has any ticket dated on or after the current system date, whether a standalone individual ticket or an individual ticket inside a group ticket; historical tickets alone SHALL NOT block it. The lock SHALL be derived from tickets, not stored.

#### Scenario: Ticket for today blocks deletion
- **WHEN** Ana has an individual ticket dated today and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is reported and Ana still exists

#### Scenario: Ticket inside a group ticket blocks deletion
- **WHEN** Ana is part of a group ticket dated in the future and is deleted
- **THEN** an error is reported and Ana still exists

#### Scenario: Only historical tickets
- **WHEN** all of Ana's tickets are dated before the current date and she is deleted
- **THEN** Ana is deleted and her historical tickets are retained

### Requirement: Locked groups block user deletion
`USER DELETE` SHALL be rejected when the user belongs to a group locked by an active group ticket, because deleting the user would modify that group's composition.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to group `G01`, which is locked by an active group ticket, and Ana is deleted
- **THEN** an error is reported and Ana remains a member of `G01`
