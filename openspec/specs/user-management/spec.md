# user-management Specification

## Purpose

Defines park users and the operations to register, delete and modify them, guaranteeing that every stored user satisfies the validation and uniqueness rules.

## Requirements

### Requirement: User data
Each user SHALL have a first name, surnames, DNI, email address, password, telephone number and card number. A user's DNI SHALL NOT change after creation.

#### Scenario: DNI is immutable
- **WHEN** any modification is requested for an existing user
- **THEN** the user's DNI remains the one given at creation

### Requirement: User creation
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create a user only if all data passes user validation and neither the DNI nor the email address is already used by another user. On success the application SHALL print `USER CREATED`; otherwise it SHALL print an error describing the first failed rule and store nothing.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed with no existing users
- **THEN** the user is stored and `USER CREATED` is printed

#### Scenario: Invalid data
- **WHEN** `USER CREATE` is processed with DNI `12345678A`
- **THEN** an error about the DNI is printed and no user is stored

#### Scenario: Duplicate DNI
- **WHEN** `USER CREATE` is processed with the DNI of an existing user and a new email address
- **THEN** an error stating the DNI is already registered is printed and no user is stored

#### Scenario: Duplicate email
- **WHEN** `USER CREATE` is processed with the email of an existing user and a new DNI
- **THEN** an error stating the email is already registered is printed and no user is stored

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists and that the password matches, delete the user and update persistent data. On success the application SHALL print `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed for an existing user with that password
- **THEN** the user is removed, the change is persisted and `USER DELETED` is printed

#### Scenario: Unknown user
- **WHEN** `USER DELETE` is processed with an email that belongs to no user
- **THEN** an error stating the user does not exist is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and an incorrect password
- **THEN** an error is printed and the user is not deleted

### Requirement: User modification
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL, after verifying that the user exists and the password matches, change the given field to the new value and persist the change. Modifiable fields SHALL be exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD` (case-insensitive). On success the application SHALL print `USER UPDATED`.

#### Scenario: Update phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for an existing user with that password
- **THEN** the phone becomes `699999999`, the change is persisted and `USER UPDATED` is printed

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** the user is afterwards identified by `ana.nueva@correo.com` and no longer by `ana@correo.com`

#### Scenario: DNI cannot be modified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error stating the field is not modifiable is printed and the user is unchanged

#### Scenario: Wrong password
- **WHEN** `USER UPDATE` is processed with an incorrect password
- **THEN** an error is printed and the user is unchanged

### Requirement: Updated values are validated
A modification SHALL NOT produce a state that user creation would reject: a new email SHALL be valid and not used by another user, a new password SHALL satisfy the password rules, a new card SHALL contain exactly 16 digits, and new names or phone SHALL be non-blank.

#### Scenario: Email used by another user
- **WHEN** `USER UPDATE` changes Ana's email to the email of Pedro, another existing user
- **THEN** an error is printed and Ana's email is unchanged

#### Scenario: Invalid new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave` is processed
- **THEN** an error about the password is printed and the password is unchanged
