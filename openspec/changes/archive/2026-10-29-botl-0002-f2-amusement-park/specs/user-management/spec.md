# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check the password, delete the user and update persistent data. On success the application SHALL print `USER DELETED`. Deletion SHALL be rejected while the user has any active ticket (an individual ticket or a group-composition ticket dated on or after the current system date), or while the user belongs to a group locked by an active group ticket. When deletion is allowed, the user SHALL also be removed from every group they belong to. Historical tickets of the user SHALL be retained.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed for an existing user with that password
- **THEN** the user is removed, the persisted file no longer contains it, and `USER DELETED` is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is executed with an existing email and an incorrect password
- **THEN** the user is not deleted and an error message is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is executed with an email that is not registered
- **THEN** nothing is deleted and an error message is printed

#### Scenario: Active individual ticket blocks deletion
- **WHEN** the user has a ticket dated today or later and `USER DELETE` is executed with correct credentials
- **THEN** an error is printed and the user still exists

#### Scenario: Active group-composition ticket blocks deletion
- **WHEN** the user is in the composition of a group ticket dated today or later and `USER DELETE` is executed with correct credentials
- **THEN** an error is printed and the user still exists

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group that has an active group ticket and `USER DELETE` is executed with correct credentials
- **THEN** an error is printed and the user still exists

#### Scenario: Historical tickets only
- **WHEN** the user has only historical tickets and belongs only to unlocked groups, and `USER DELETE` is executed with correct credentials
- **THEN** the user is deleted, removed from those groups, and their historical tickets remain
