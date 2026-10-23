# user-management Specification

## Purpose

Defines park users: their data and invariants, the validation rules their data must satisfy, and the commands to create, delete, and modify them.

## Requirements

### Requirement: User attributes
A user SHALL have a first name, surnames, DNI, email address, password, telephone number, and card number. First name, surnames, and telephone number SHALL be non-empty. A user's DNI SHALL NOT change after the user is created.

#### Scenario: User holds all attributes
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678`, and card `1234567890123456`
- **THEN** the stored user has exactly those attribute values

#### Scenario: Empty required text field rejected
- **WHEN** `USER CREATE` is processed with an empty first name, surnames, or telephone number
- **THEN** the user is not created and an error message beginning with `ERROR` is printed

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter, and the letter SHALL equal the standard Spanish check letter: the character at position `number mod 23` in `TRWAGMYFPDXBNJZSQVHLCKE`. The letter SHALL be accepted in either case and stored in upper case.

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** a DNI with fewer or more than eight digits, a non-digit among the first eight characters, or no final letter is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL contain no whitespace and exactly one `@`, with non-empty text before it. The part after `@` SHALL have a non-empty domain name, then a final `.`, then an extension of exactly three characters, with no empty segment between dots. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit, and one symbol (a character that is not a letter, digit, or whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits, with no spaces, letters, or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone number stored as given
The telephone number SHALL be stored without encryption or transformation. No format validation beyond being non-empty SHALL be applied.

#### Scenario: Phone stored verbatim
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone value is `612345678`

### Requirement: User creation command
`USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL validate all data and, if valid and unique, create the user, persist it, and print `USER CREATED`. Otherwise it SHALL print an error beginning with `ERROR` stating the reason and create nothing.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user has that DNI or email
- **THEN** `USER CREATED` is printed and the user exists

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A`
- **THEN** an error mentioning the DNI is printed and no user is created

### Requirement: User uniqueness
No two users SHALL share the same DNI or the same email address. Emails SHALL be compared case-insensitively. Both conditions SHALL be checked before a user is created, and email uniqueness SHALL also be checked when an email is modified.

#### Scenario: Duplicate DNI
- **WHEN** `USER CREATE` is processed with the DNI of an existing user and a new email
- **THEN** an error is printed and no user is created

#### Scenario: Duplicate email
- **WHEN** `USER CREATE` is processed with a new DNI and the email of an existing user, in any letter case
- **THEN** an error is printed and no user is created

### Requirement: User deletion command
`USER DELETE;Email;Password` SHALL locate the user by email. If found and the password matches, it SHALL check that the user has no active ticket (standalone or within a group ticket) and belongs to no group locked by an active group ticket. If allowed, it SHALL remove the user from their groups and from any attraction, delete the user, persist, and print `USER DELETED`. Otherwise it SHALL print an error beginning with `ERROR` and change nothing. Historical tickets SHALL remain.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, including after a restart

#### Scenario: Wrong password
- **WHEN** `USER DELETE;pedro@correo.com;Wrong1!` is processed
- **THEN** an error is printed and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed and no user has that email
- **THEN** an error is printed

#### Scenario: Active individual ticket blocks deletion
- **WHEN** the system date is `2027-04-15`, Ana holds ticket `T001` dated `2027-04-15` or later, and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists

#### Scenario: Active group ticket blocks deletion
- **WHEN** Pedro is included in a group ticket dated on or after the system date and `USER DELETE` is processed for Pedro with the correct password
- **THEN** an error is printed and Pedro still exists

#### Scenario: Deletion allowed after future ticket removed
- **WHEN** Marta's only ticket `T002` is dated after the system date, `TICKET DELETE;T002` is processed, and then `USER DELETE;marta@correo.com;Marta4%` is processed
- **THEN** `USER DELETED` is printed

#### Scenario: Historical tickets do not block deletion
- **WHEN** the user's only tickets are dated before the system date
- **THEN** `USER DELETE` with correct credentials succeeds and the historical tickets are kept

#### Scenario: Deleted user removed from unlocked groups
- **WHEN** Laura belongs to group `G02`, which has no active group ticket, and Laura is deleted
- **THEN** `G02` no longer contains Laura


### Requirement: Modifiable fields
The fields that can be modified SHALL be exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, and `CARD`, matched case-insensitively. Any other field name, including `DNI`, SHALL be rejected.

#### Scenario: DNI cannot be modified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is printed and the user's DNI is unchanged

### Requirement: User modification command
`USER UPDATE;Email;Password;Field;NewValue` SHALL first verify that a user with that email exists and the password matches. It SHALL validate the new value with the creation rules for that field: valid and unused email, valid password, valid card, non-empty name, surnames, or phone. Only then SHALL it apply and persist the change and print `USER UPDATED`. On any failure it SHALL print an error beginning with `ERROR` and change nothing.

#### Scenario: Update phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed with correct credentials
- **THEN** `USER UPDATED` is printed and the phone is `699999999`, including after a restart

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and no other user has that email
- **THEN** `USER UPDATED` is printed and the user is found by `ana.nueva@correo.com` from then on

#### Scenario: Invalid new value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;CARD;1234` is processed
- **THEN** an error is printed and the card is unchanged

#### Scenario: Email already used by another user
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed and Pedro exists
- **THEN** an error is printed and Ana's email is unchanged

#### Scenario: Wrong password
- **WHEN** `USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999` is processed
- **THEN** an error is printed and nothing changes

### Requirement: Password protection
Passwords SHALL be checked against the password rules and then stored only as a BCrypt hash. The plain password SHALL never be kept in memory or on disk. Commands that need the password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt. Changing the password with `USER UPDATE;...;PASSWORD;<new>` SHALL validate the new password and store a newly generated hash.

#### Scenario: Stored value is a hash
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the stored password value is a BCrypt hash, not `Clave1!`

#### Scenario: Correct password verified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for that user
- **THEN** the password is accepted and `USER UPDATED` is printed

#### Scenario: Wrong password rejected
- **WHEN** `USER DELETE;ana@correo.com;clave1!` is processed
- **THEN** an error is printed and the user still exists

#### Scenario: Password change produces a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes, `Nueva2#` is accepted afterwards, and `Clave1!` is rejected

#### Scenario: Invalid new password not stored
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva` is processed
- **THEN** an error is printed and `Clave1!` still verifies
