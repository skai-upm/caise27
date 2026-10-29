# user-management Specification

## Purpose

Defines park users and the operations to create, delete and modify them through USER commands, preserving validation rules, DNI/email uniqueness and DNI immutability.

## Requirements

### Requirement: User data
Each user SHALL hold first name, surname(s), DNI, email address, password, telephone number and card number. The DNI MUST NOT change after the user is created.

#### Scenario: User retains its data
- **WHEN** a user is created with `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the user holds exactly those seven values

### Requirement: User creation
`USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create and store a new user only when all data passes the user validation rules and neither the DNI nor the email is already used by an existing user; otherwise no user is created and an error is reported.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** the user is stored and `USER CREATED` is shown

#### Scenario: Invalid data rejected
- **WHEN** USER CREATE is processed with DNI `12345678A`
- **THEN** no user is created and an error is shown

#### Scenario: Duplicate DNI rejected
- **WHEN** USER CREATE is processed with DNI `12345678Z` while another user already has that DNI
- **THEN** no user is created and an error is shown

#### Scenario: Duplicate email rejected
- **WHEN** USER CREATE is processed with email `ana@correo.com` while another user already has that email
- **THEN** no user is created and an error is shown

### Requirement: User deletion
`USER DELETE;Email;Password` SHALL locate the user by email, verify the user exists, check the password, delete the user and update persistent information. A nonexistent user or an incorrect password MUST NOT delete any user. Deletion MUST also be rejected while the user has an active ticket (individual or part of a group ticket) or belongs to a group locked by an active group ticket; when allowed, the user is removed from all their groups and their historical tickets are retained.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and Pedro exists with that password
- **THEN** Pedro is removed, the persistent data no longer contains Pedro and `USER DELETED` is shown

#### Scenario: Incorrect password
- **WHEN** USER DELETE is processed for an existing email with a wrong password
- **THEN** the user is not deleted and an error is shown

#### Scenario: Unknown email
- **WHEN** USER DELETE is processed for an email that no user has
- **THEN** nothing is deleted and an error is shown

#### Scenario: Active individual ticket blocks deletion
- **WHEN** Ana has a ticket dated on or after the current system date
- **THEN** USER DELETE for Ana is rejected

#### Scenario: Active group ticket composition blocks deletion
- **WHEN** Ana is in the composition of a group ticket dated on or after the current system date
- **THEN** USER DELETE for Ana is rejected

#### Scenario: Locked group blocks deletion
- **WHEN** Ana belongs to a group with an active group ticket and Ana has no ticket of her own
- **THEN** USER DELETE for Ana is rejected

#### Scenario: Historical tickets do not block
- **WHEN** Ana has only tickets dated before the current date and belongs to a modifiable group
- **THEN** Ana is deleted, removed from the group, and her historical tickets remain

### Requirement: Modifiable fields enumeration
The fields that can be modified SHALL be represented by an enumeration with exactly the values `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. The DNI is not modifiable.

#### Scenario: DNI cannot be updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the update is rejected with an error and Ana's DNI is unchanged

#### Scenario: Unknown field
- **WHEN** USER UPDATE is processed with field `AGE`
- **THEN** the update is rejected with an error

### Requirement: User modification
`USER UPDATE;Email;Password;Field;NewValue` SHALL first verify the current email and password, then apply the new value only if it satisfies the same validation rules as creation (a new email must be valid and not used by another user; a new password must meet the password rules; a new card must have exactly 16 digits), and update persistent information.

#### Scenario: Successful update
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for existing user Ana
- **THEN** Ana's telephone becomes `699999999`, it is persisted and `USER UPDATED` is shown

#### Scenario: Email changed
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** Ana is subsequently found by `ana.nueva@correo.com` and no longer by `ana@correo.com`

#### Scenario: Wrong credentials
- **WHEN** USER UPDATE is processed with a wrong password or unknown email
- **THEN** nothing is modified and an error is shown

#### Scenario: Invalid new value
- **WHEN** USER UPDATE sets PASSWORD to `clave1!` or CARD to `123456789012345` or EMAIL to `ana@correo.es`
- **THEN** nothing is modified and an error is shown

#### Scenario: Email used by another user
- **WHEN** USER UPDATE sets Ana's EMAIL to `pedro@correo.com` and Pedro exists
- **THEN** nothing is modified and an error is shown
