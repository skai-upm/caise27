# user-deletion Specification

## Purpose

Allows a user to be removed through `USER DELETE` only after the user's email and password are verified, keeping persistent data in sync (source F1-C05, section 8).

## Requirements

### Requirement: Delete user command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check that the password matches, delete the user, update the persistent information, and report success (for example `USER DELETED`).

#### Scenario: Correct credentials delete user
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is removed from the system and from the user data file, and success is reported

### Requirement: Deletion rejected without valid credentials
`USER DELETE` SHALL NOT delete any user when no user has the given email or the password is incorrect, and SHALL report an error leaving stored state unchanged.

#### Scenario: Incorrect password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed for an existing user whose password is `Clave1!`
- **THEN** an error is reported and the user remains in memory and in the user data file

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no user has that email
- **THEN** an error is reported and no user is removed
