# Spec Delta

## Purpose

Defines the data validation rules a user's DNI, email address, password and card number must satisfy before they are accepted by any user operation.

## ADDED Requirements

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
