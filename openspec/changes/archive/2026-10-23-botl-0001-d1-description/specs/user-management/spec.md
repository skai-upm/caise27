# Spec Delta

## Purpose

Defines how park users are registered, removed and modified through text commands, including authentication by email and password and the uniqueness of DNI and email.

## ADDED Requirements

### Requirement: User creation
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create a user only when every value passes user validation and neither the DNI nor the email is already registered; on success it SHALL print `USER CREATED`.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user exists
- **THEN** the user is stored and `USER CREATED` is printed

#### Scenario: Invalid data
- **WHEN** a `USER CREATE` command contains an invalid DNI, email, password or card
- **THEN** no user is created and an error message names the invalid field

### Requirement: User uniqueness
No two users SHALL share the same DNI or the same email address (emails compared ignoring letter case); both conditions SHALL be checked before creating a user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and another `USER CREATE` uses `12345678Z`
- **THEN** the creation is rejected with an error message

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and another `USER CREATE` uses `ana@correo.com`
- **THEN** the creation is rejected with an error message

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists and that the password matches, delete the user and update persistent data; on success it SHALL print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for an existing user with that password
- **THEN** the user is removed, the change is persisted and `USER DELETED` is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** the user is not deleted and an error message is printed

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed
- **THEN** an error message is printed and no data changes

### Requirement: User modification
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL, after checking the current email and password, change one field among `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (field name matched ignoring case); the new value SHALL satisfy the same validation as on creation, a new email SHALL not belong to another user, and on success the change SHALL be persisted and `USER UPDATED` printed.

#### Scenario: Update phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for an existing user
- **THEN** the phone is changed, persisted and `USER UPDATED` is printed

#### Scenario: Update email to one in use
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed while `pedro@correo.com` belongs to another user
- **THEN** the update is rejected with an error message

#### Scenario: Invalid new value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;CARD;1234` is processed
- **THEN** the update is rejected and the stored card is unchanged

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with a wrong password
- **THEN** no field changes and an error message is printed

### Requirement: DNI immutability
The DNI of a user SHALL NOT be modifiable after creation.

#### Scenario: Attempt to update DNI
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the update is rejected with an error message listing the modifiable fields
