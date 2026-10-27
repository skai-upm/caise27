# Spec Delta

## Purpose

Defines the `USER DELETE` command that removes a registered user after identification by email and password, keeping persistent data in sync.

## ADDED Requirements

### Requirement: USER DELETE syntax
The command `USER DELETE;Email;Password` SHALL require exactly two non-empty fields; otherwise it SHALL be rejected with a format error and no user is deleted.

#### Scenario: Missing password
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error is reported and no user is deleted

### Requirement: Deletion after identification
The application SHALL locate the user by email, verify the user exists, check the password and only then delete the user, responding `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `USER DELETED` and no user with email `ana@correo.com` or DNI `12345678Z` remains

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no user has that email
- **THEN** an error indicating the user does not exist is reported

#### Scenario: Incorrect password
- **WHEN** user `ana@correo.com` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error indicating an incorrect password is reported and the user is not deleted

### Requirement: Deletion persisted
A successful deletion SHALL update the persistent user information so that the user is absent after the application restarts.

#### Scenario: Deleted user absent after restart
- **WHEN** a user is deleted and the application is started again
- **THEN** the user file no longer contains that user and a USER CREATE reusing its DNI and email succeeds
