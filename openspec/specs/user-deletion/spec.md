# user-deletion Specification

## Purpose

Lets a user be removed from the Amusement Park with the `USER DELETE` command, only after identifying with the correct email and password, keeping the stored users up to date.

## Requirements

### Requirement: Delete user command
The system SHALL accept `USER DELETE;Email;Password`; it SHALL locate the user by email, verify that the user exists, check that the password matches exactly, delete the user and update the persisted user file, then print a confirmation naming the email. A command without exactly two fields MUST be rejected with an error message and change nothing.

#### Scenario: Matching email and password deletes the user
- **GIVEN** a stored user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** a confirmation containing `ana@correo.com` is printed, the user no longer exists, and the user file no longer contains its line

#### Scenario: Deleted user frees its DNI and email
- **GIVEN** the user `12345678Z` / `ana@correo.com` was deleted
- **WHEN** a valid `USER CREATE` with DNI `12345678Z` and email `ana@correo.com` is processed
- **THEN** the user is created

#### Scenario: Wrong number of fields
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error reporting the wrong number of fields is printed and no user is deleted

### Requirement: Deletion requires correct identification
An incorrect password or an email that matches no user MUST NOT delete any user; the command SHALL print an error message and leave the stored users and the user file unchanged.

#### Scenario: Incorrect password
- **GIVEN** a stored user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Otra2#` is processed
- **THEN** an error reporting an incorrect password is printed and the user and the user file are unchanged

#### Scenario: Unknown email
- **GIVEN** no stored user has email `nadie@correo.com`
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error reporting that no user has that email is printed and no user is deleted
