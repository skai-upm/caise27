# Spec Delta

## ADDED Requirements

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by e-mail, verify that the user exists, check the entered password against the stored protected value, delete the user (removing them from all groups) and update persistent data. An unknown e-mail, a wrong password, or the ticket and group-lock deletion rules SHALL reject the deletion with an error, leaving the user and all related data unchanged. Historical tickets of a deleted user SHALL be kept.

#### Scenario: Correct credentials
- **GIVEN** registered user `ana@correo.com` with password `Clave1!` and no active tickets
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is shown, the user no longer exists, and after a restart the user is still absent

#### Scenario: Wrong password
- **GIVEN** registered user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error is shown and the user still exists unchanged

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error is shown

#### Scenario: Active ticket blocks deletion
- **GIVEN** `ana@correo.com` has a ticket dated today
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is shown and the user still exists

#### Scenario: Removed from groups, history kept
- **GIVEN** `ana@correo.com` belongs to an unlocked group and has only past tickets
- **WHEN** the user is deleted with correct credentials
- **THEN** the user is no longer a member of the group and the past tickets still exist
