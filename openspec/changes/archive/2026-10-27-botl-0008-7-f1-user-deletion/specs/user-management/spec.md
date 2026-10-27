# Spec Delta

## ADDED Requirements

### Requirement: User deletion command
The application SHALL accept `USER DELETE;Email;Password` with exactly two data elements. It SHALL locate the user by email, verify the user exists, check that the password matches, delete the user and update the persisted users, confirming with `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** the user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed, the user no longer exists and is no longer in the users file

#### Scenario: Unknown user
- **WHEN** no user has email `nadie@correo.com` and `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error indicating the user does not exist is printed and no user is deleted

#### Scenario: Wrong number of elements
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error describing the expected syntax is printed and no user is deleted

### Requirement: Password required to delete
An incorrect password SHALL NOT allow a user to be deleted.

#### Scenario: Incorrect password
- **WHEN** the user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error about the incorrect password is printed and the user still exists
