# user-management Specification

## Purpose

Defines park users, the validation rules their data must satisfy, and the commands to create, modify and delete them while keeping DNI and email unique.

## Requirements

### Requirement: User attributes
A user SHALL have a first name, surnames, DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created. First name, surnames and telephone number SHALL be non-empty; the telephone number is stored as given, without transformation or encryption.

#### Scenario: Create user with all attributes
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** `USER CREATED` is printed and a user with exactly those seven values exists

#### Scenario: Empty name rejected
- **WHEN** a `USER CREATE` command has an empty first name
- **THEN** an error is printed and no user is created

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter that matches the standard Spanish control letter (`TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23). A lowercase letter is accepted and stored in uppercase.

#### Scenario: Valid DNI
- **WHEN** a user is created with DNI `12345678Z`
- **THEN** the DNI is accepted

#### Scenario: Wrong control letter
- **WHEN** a user is created with DNI `12345678A`
- **THEN** an error about an invalid DNI is printed and no user is created

#### Scenario: Wrong length
- **WHEN** a user is created with DNI `1234567Z`
- **THEN** an error about an invalid DNI is printed and no user is created

### Requirement: Email validation
An email address SHALL match the form `text@text.xxx` without using regular expressions: exactly one `@`, non-empty content before it, a non-empty domain name after it, a final `.` followed by an extension of exactly three letters, no empty dot-separated segments, and no whitespace.

#### Scenario: Valid email
- **WHEN** a user is created with email `ana@correo.com`
- **THEN** the email is accepted

#### Scenario: Invalid emails
- **WHEN** a user is created with any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es`
- **THEN** an error about an invalid email is printed and no user is created

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace), checked without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** a user is created with password `Clave1!`
- **THEN** the password is accepted

#### Scenario: Missing character class
- **WHEN** a user is created with any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12`
- **THEN** an error about an invalid password is printed and no user is created

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** a user is created with card `1234567890123456`
- **THEN** the card is accepted

#### Scenario: Invalid cards
- **WHEN** a user is created with any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567`
- **THEN** an error about an invalid card number is printed and no user is created

### Requirement: Unique DNI and email
The system SHALL NOT hold two users with the same DNI or the same email address (emails compared ignoring case). Both conditions SHALL be checked before a user is created.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and another `USER CREATE` uses DNI `12345678Z` with a different email
- **THEN** an error about the DNI already being registered is printed and no user is created

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and another `USER CREATE` uses `ana@correo.com` with a different DNI
- **THEN** an error about the email already being registered is printed and no user is created

### Requirement: User deletion
`USER DELETE;Email;Password` SHALL locate the user by email, verify it exists and that the password matches exactly, and then delete the user only if they have no active ticket (an individual ticket or a member ticket of a group ticket dated on or after the system date) and belong to no locked group. On success the user is removed from all their groups, persistent storage is updated, and historical tickets keep the user's information. These restrictions are derived from tickets and the system date, not stored. Otherwise all data is left unchanged.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, including after a restart

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed and Ana's password is `Clave1!`
- **THEN** an authentication error is printed and the user is not deleted

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed and no such user exists
- **THEN** an error that the user was not found is printed

#### Scenario: Active individual ticket
- **WHEN** Marta has a ticket dated on or after the system date and `USER DELETE;marta@correo.com;Marta4%` is processed
- **THEN** an error is printed and Marta is not deleted

#### Scenario: Deletion allowed after deleting the future ticket
- **WHEN** Marta's only ticket T002 is in the future, `TICKET DELETE;T002` succeeds and then `USER DELETE;marta@correo.com;Marta4%` is processed
- **THEN** `USER DELETED` is printed

#### Scenario: Member of an active group ticket
- **WHEN** Ana's group G01 has a group ticket for today and Ana is deleted
- **THEN** an error is printed and Ana is not deleted

#### Scenario: Removed from unlocked groups
- **WHEN** Pedro belongs to unlocked group G02, has only historical tickets, and is deleted
- **THEN** `USER DELETED` is printed, Pedro is no longer a member of G02, and his historical tickets still exist

### Requirement: User modification
`USER UPDATE;Email;Password;Field;NewValue` SHALL authenticate the user by current email and password, then change one field. Modifiable fields are exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD` (field name matched ignoring case). The new value SHALL satisfy the same rules as on creation; a new email SHALL be valid and not used by another user. On success `USER UPDATED` is printed and persistent storage is updated.

#### Scenario: Update phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** `USER UPDATED` is printed and Ana's phone is `699999999`, including after a restart

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** the user is afterwards identified by `ana.nueva@correo.com`

#### Scenario: New email already in use
- **WHEN** Ana updates her EMAIL to `pedro@correo.com`, which belongs to another user
- **THEN** an error is printed and Ana's email is unchanged

#### Scenario: Invalid new value
- **WHEN** Ana updates PASSWORD to `clave1!` or CARD to `123`
- **THEN** a validation error is printed and the field is unchanged

#### Scenario: DNI cannot be modified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error stating that the field cannot be modified (listing the modifiable fields) is printed and the DNI is unchanged

#### Scenario: Wrong password on update
- **WHEN** `USER UPDATE;ana@correo.com;Bad1!;PHONE;600000000` is processed
- **THEN** an authentication error is printed and nothing changes
