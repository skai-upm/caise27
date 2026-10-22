# Spec Delta

## Purpose

Defines the lifecycle of park users: registering them, deleting them and modifying their data, while keeping DNI and email unique and the DNI immutable.

## ADDED Requirements

### Requirement: User creation
The `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` command SHALL create a user only when all data passes validation and no existing user has the same DNI or the same email address (email compared case-insensitively). On success the application SHALL print `USER CREATED`.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no such user exists
- **THEN** the user is stored and `USER CREATED` is printed

#### Scenario: Invalid data
- **WHEN** a `USER CREATE` command has the DNI `12345678A`
- **THEN** an error is printed and no user is created

#### Scenario: Duplicate DNI
- **WHEN** a `USER CREATE` command uses the DNI of an existing user with a different email
- **THEN** an error is printed and no user is created

#### Scenario: Duplicate email
- **WHEN** a `USER CREATE` command uses the email of an existing user with a different DNI
- **THEN** an error is printed and no user is created

### Requirement: User deletion
The `USER DELETE;Email;Password` command SHALL locate the user by email, verify the password, delete the user and update persistent data. On success the application SHALL print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed for an existing user with that password
- **THEN** the user is removed and `USER DELETED` is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an incorrect password
- **THEN** an error is printed and the user is not deleted

#### Scenario: Unknown user
- **WHEN** `USER DELETE` is processed with an email that belongs to no user
- **THEN** an error is printed

### Requirement: User modification
The `USER UPDATE;Email;Password;Field;NewValue` command SHALL verify the current email and password and then change the given field, which SHALL be one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (case-insensitive). The new value SHALL satisfy the same validation as on creation, and a new email SHALL not be used by another user. On success the application SHALL print `USER UPDATED` and persist the change.

#### Scenario: Update phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed with correct credentials
- **THEN** the phone is changed and `USER UPDATED` is printed

#### Scenario: Update email to one already in use
- **WHEN** `USER UPDATE` sets `EMAIL` to the email of another user
- **THEN** an error is printed and the user is unchanged

#### Scenario: Invalid new value
- **WHEN** `USER UPDATE` sets `PASSWORD` to `clave1!`
- **THEN** an error is printed and the password is unchanged

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with an incorrect password
- **THEN** an error is printed and the user is unchanged

### Requirement: Immutable DNI
A user's DNI SHALL NOT be modifiable after creation; `DNI` (or any field outside the allowed set) SHALL be rejected as an update field.

#### Scenario: Attempt to change DNI
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is printed and the DNI is unchanged
