# user-validation Specification

## Purpose
Defines the validation rules applied to user data — DNI, email address, password and card number — so that every user operation can determine whether the data defining a user is valid.

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter that matches the standard Spanish DNI control letter for that number (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct control letter
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is valid

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is invalid

#### Scenario: Wrong format
- **WHEN** a DNI with fewer or more than eight digits, without a final letter, or with non-digit characters in the first eight positions is validated
- **THEN** it is invalid

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: non-empty content before a single `@`, a non-empty domain after it, and an extension of exactly three characters after the domain's last dot. The check SHALL use String operations and `split`, without regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is valid

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is invalid

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is not a letter, digit or whitespace), checked without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is valid

#### Scenario: Missing character class
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is invalid

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card number
- **WHEN** `1234567890123456` is validated
- **THEN** it is valid

#### Scenario: Invalid card numbers
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is invalid

### Requirement: Telephone stored without validation
The telephone number SHALL be stored as provided, without validation, encryption or any other transformation.

#### Scenario: Telephone kept as given
- **WHEN** a user's data is validated with any telephone value
- **THEN** the telephone value does not affect the validation result and is kept unchanged

### Requirement: Whole-user validation
The system SHALL determine that a user's data is valid only when its DNI, email address, password and card number are all valid, so that user operations can reuse the same checks.

#### Scenario: One invalid field
- **WHEN** a user whose data is valid except for the card number is validated
- **THEN** the user's data is reported as invalid
