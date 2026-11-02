# Spec Delta

## Purpose

Deletes users with the `USER DELETE` command only after the user is located by email and the password is verified, updating persistent storage.

## ADDED Requirements

### Requirement: USER DELETE command
The system SHALL accept `USER DELETE;Email;Password` and SHALL locate the user by email, verify that the user exists, check the password, delete the user and update persistent storage, reporting `USER DELETED`.

#### Scenario: Correct credentials
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and Ana is registered with that password
- **THEN** Ana is removed from the registered users and from the storage file, and `USER DELETED` is reported

### Requirement: Deletion requires valid identification
An unknown email or an incorrect password SHALL NOT delete any user.

#### Scenario: Incorrect password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** no user is deleted, stored state is unchanged and an error is reported

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed
- **THEN** no user is deleted, stored state is unchanged and an error is reported
