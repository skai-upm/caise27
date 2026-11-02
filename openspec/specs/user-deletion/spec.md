# user-deletion Specification

## Purpose

Lets a user be removed with `USER DELETE` only when the email identifies an existing user and the supplied password matches, keeping persistent data in sync.

## Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check the password, delete the user and update the persistent information; on success the application reports `USER DELETED`. (F1-C05)

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
