# Spec Delta

## Purpose

Deletes users through the `USER DELETE` command only after their email and password are verified, keeping storage in sync.

## ADDED Requirements

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify the user exists, check the password, delete the user, update persistent storage, and report `USER DELETED`.

#### Scenario: Correct credentials
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for an existing user with that password
- **THEN** the user is removed from memory and from the storage file and `USER DELETED` is reported

### Requirement: Deletion requires correct credentials
A `USER DELETE` with an unknown email or an incorrect password SHALL NOT delete any user.

#### Scenario: Wrong password
- **WHEN** `USER DELETE` names an existing email with an incorrect password
- **THEN** an error is reported and stored users are unchanged

#### Scenario: Unknown email
- **WHEN** `USER DELETE` names an email no user has
- **THEN** an error is reported and stored users are unchanged
