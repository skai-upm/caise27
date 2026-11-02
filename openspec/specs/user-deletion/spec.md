# user-deletion Specification

## Purpose

Defines the `USER DELETE` command that removes a user only after the user is identified by email and the password is verified (F1-C05).

## Requirements

### Requirement: USER DELETE command
The system SHALL accept `USER DELETE;Email;Password`. It SHALL locate the user by email, verify that the user exists, check the password, delete the user, and update the persistent information, then report success (for example `USER DELETED`).

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** the user is removed from memory and from the users file and success is reported

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed with an email no user has
- **THEN** no user is removed, the users file is unchanged and an error is reported

#### Scenario: Incorrect password
- **WHEN** `USER DELETE` is processed with an existing email and a wrong password
- **THEN** the user is not deleted, the users file is unchanged and an error is reported

#### Scenario: Malformed delete command
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** no user is removed and an error is reported
