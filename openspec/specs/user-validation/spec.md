# user-validation Specification

## Purpose

Defines the data validation rules a user's DNI, email address, password and card number must satisfy before they are accepted by any user operation.

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter, and that letter matches the standard Spanish DNI control letter (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong length or characters
- **WHEN** a DNI with fewer or more than eight digits, or without a final letter, is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only when it has the form `text@text.xxx`: non-empty content before a single `@`, and after `@` a non-empty domain followed by a final `.` extension of exactly three characters. Validation SHALL use String operations and `split`, not regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone stored as given
The telephone number SHALL be stored as provided, without format validation, encryption or other transformation.

#### Scenario: Telephone not transformed
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is exactly `612345678`

### Requirement: Password protection with BCrypt
A new or changed password SHALL first pass the password validation rules, and then only its BCrypt hash SHALL be stored. Password-checking operations (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt. A password change SHALL store a newly generated hash. Files from earlier deliveries need not be migrated.

#### Scenario: Correct password verified against hash
- **WHEN** a user created with `Clave1!` runs `USER UPDATE;<email>;Clave1!;PHONE;699999999`
- **THEN** the update succeeds

#### Scenario: Wrong password rejected
- **WHEN** `USER DELETE;<email>;Wrong1!` is processed
- **THEN** an error is printed and the user still exists

#### Scenario: Password change rehashed
- **WHEN** `USER UPDATE;<email>;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored hash changes, `Nueva2#` authenticates, `Clave1!` no longer does, and no plain password is stored

#### Scenario: Invalid new password not stored
- **WHEN** `USER UPDATE;<email>;Clave1!;PASSWORD;clave1!` is processed
- **THEN** an error is printed and the stored hash is unchanged
