# Spec Delta

## ADDED Requirements

### Requirement: Delete a user
The system SHALL, for `USER DELETE;Email;Password`, locate the user by email, verify that the user exists, check the password against the stored protected value, apply the user-deletion rules of the park (no active ticket, no membership of a locked group, removal from groups when allowed), delete the user, update the stored data, and display `USER DELETED`. If the user does not exist, the password is incorrect, a deletion rule prevents it, or the command does not have exactly two fields, it SHALL display an error message and change nothing.

#### Scenario: Correct email and password
- **GIVEN** user `ana@correo.com` with password `Clave1!` and no active tickets
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is displayed, the user no longer exists, and after a restart the user is still absent

#### Scenario: Incorrect password
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error message is displayed and the user remains unchanged

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message is displayed

#### Scenario: Active ticket prevents deletion
- **GIVEN** user `ana@correo.com` has a ticket dated today
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message is displayed and the user and the ticket remain

#### Scenario: Deleted user leaves groups, historical tickets kept
- **GIVEN** user `ana@correo.com` belongs to modifiable group `G01` and only has tickets dated before today
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted, is no longer a member of `G01`, and the historical tickets still exist
