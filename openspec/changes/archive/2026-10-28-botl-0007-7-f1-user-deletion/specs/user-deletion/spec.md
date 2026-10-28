# Spec Delta

## Purpose

Defines deletion of users through the `USER DELETE` command, which requires identifying the user by email address and the correct password, and keeps persistent data in sync.

## ADDED Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check that the password matches, delete the user, update the persistent users file and print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed, the user no longer exists, and the users file no longer contains the user's line

#### Scenario: Deleted user's DNI and email become available
- **WHEN** a user has been deleted
- **THEN** a new `USER CREATE` with the same DNI and email succeeds

### Requirement: Deletion requires an existing user
If no user has the given email, the deletion SHALL be rejected with an `ERROR:` message and nothing SHALL change.

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no user has that email
- **THEN** an `ERROR:` line reporting that the user was not found is printed and users and file are unchanged

### Requirement: Incorrect password prevents deletion
If the password does not match the user's password, the user SHALL NOT be deleted and an `ERROR:` message SHALL be printed.

#### Scenario: Wrong password
- **WHEN** a user with email `ana@correo.com` and password `Clave1!` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an `ERROR:` line reporting an incorrect password is printed and the user still exists, also in the users file
