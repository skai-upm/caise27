# Spec Delta

## MODIFIED Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check the password, check that the user has no active ticket (an individual ticket, standalone or inside a group ticket, dated on or after the system date) and belongs to no group locked by an active group ticket, then delete the user, remove them from all their groups, and update the persistent information; on success the application reports `USER DELETED`. Historical tickets SHALL keep the deleted user's information. (F1-C05, F2-C03, F2-C04)

#### Scenario: Correct credentials
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` runs and Ana is registered with that password
- **THEN** Ana is removed from memory and from the users file, and `USER DELETED` is reported

#### Scenario: Wrong password
- **WHEN** `USER DELETE` runs with a registered email and an incorrect password
- **THEN** the user is not deleted, stored state is unchanged and an error is reported

#### Scenario: Unknown email
- **WHEN** `USER DELETE` runs with an email that no user has
- **THEN** no user is deleted and an error is reported

#### Scenario: Malformed delete
- **WHEN** `USER DELETE` runs with a number of fields other than two
- **THEN** no user is deleted and an error is reported

#### Scenario: Active ticket blocks deletion
- **WHEN** the user has an individual ticket, or a constituent ticket of a group ticket, dated today or later
- **THEN** deletion is rejected and the user remains

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group that has an active group ticket
- **THEN** deletion is rejected and the group composition is unchanged

#### Scenario: Deletion removes group membership and keeps history
- **WHEN** a user with only historical tickets, belonging only to modifiable groups, is deleted
- **THEN** the user is removed from those groups and their historical tickets remain with the user's information
