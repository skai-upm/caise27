# user-management Specification

## Purpose

Defines the park user and the operations to create, delete and update users while keeping DNI and email unique and the DNI immutable.

## Requirements

### Requirement: User data
Each user SHALL hold first name, surname(s), DNI, email address, password, telephone number and card number. A user's DNI SHALL NOT change after creation.

#### Scenario: DNI cannot be changed
- **WHEN** a user exists with DNI `12345678Z`
- **THEN** no operation offered by the application changes that user's DNI

### Requirement: Create user
`USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create a user when all data passes validation and neither the DNI nor the email is already used by another user, printing `USER CREATED`. Otherwise it SHALL create nothing and print an error message.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed with no existing users
- **THEN** `USER CREATED` is printed and the user exists

#### Scenario: Invalid data rejected
- **WHEN** a `USER CREATE` command has an invalid DNI, email, password or card
- **THEN** an error is printed and no user is created

#### Scenario: Duplicate DNI rejected
- **WHEN** a `USER CREATE` command uses a DNI already belonging to an existing user
- **THEN** an error is printed and no user is created

#### Scenario: Duplicate email rejected
- **WHEN** a `USER CREATE` command uses an email already belonging to an existing user
- **THEN** an error is printed and no user is created

#### Scenario: Wrong number of fields
- **WHEN** a `USER CREATE` command does not have exactly seven data fields
- **THEN** an error is printed and no user is created

### Requirement: Delete user
`USER DELETE;Email;Password` SHALL locate the user by email, verify the user exists, verify the password, delete the user, update persistent data and print `USER DELETED`. An unknown email or incorrect password SHALL leave the user in place and print an error.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, also after restart

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and a wrong password
- **THEN** an error is printed and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed with an email of no existing user
- **THEN** an error is printed and no user changes

### Requirement: Update user
`USER UPDATE;Email;Password;Field;NewValue` SHALL, after verifying the current email and password, set one field among `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` to the new value, persist the change and print `USER UPDATED`. New values SHALL satisfy the creation validation rules; a new email SHALL NOT be used by another user. `DNI` and any other field name SHALL be rejected.

#### Scenario: Update phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for an existing user with that password
- **THEN** `USER UPDATED` is printed and the phone is `699999999`, also after restart

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and no other user has that email
- **THEN** `USER UPDATED` is printed and the user is found by `ana.nueva@correo.com`

#### Scenario: Update name with spaces
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the user's first name is `Ana Maria`

#### Scenario: Email taken by another user
- **WHEN** the new email belongs to another user
- **THEN** an error is printed and the user is unchanged

#### Scenario: Invalid new value
- **WHEN** the new value of `EMAIL`, `PASSWORD` or `CARD` fails validation
- **THEN** an error is printed and the user is unchanged

#### Scenario: DNI not modifiable
- **WHEN** the field is `DNI` or an unknown name
- **THEN** an error is printed and the user is unchanged

#### Scenario: Wrong credentials
- **WHEN** the email is unknown or the password is wrong
- **THEN** an error is printed and no user changes
