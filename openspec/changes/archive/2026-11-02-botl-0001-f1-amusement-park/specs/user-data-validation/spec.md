# Spec Delta

## Purpose

Determines whether data defining a user meets the assignment restrictions for DNI, email, password and card number, so that user creation and update can reuse the same checks.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter, and that letter equals the check letter from the standard Spanish DNI algorithm (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). (F1-C02)

#### Scenario: Correct check letter
- **WHEN** `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** a value with fewer or more than eight digits, or without a final letter, is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only when it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, a non-empty domain after it, and a final extension after the last `.` of exactly three characters. (F1-C02)

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). (F1-C02)

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols. (F1-C02)

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone stored without transformation
The telephone number SHALL only be stored, with no validation, encryption or other transformation required in this delivery. (F1-C02)

#### Scenario: Phone stored as given
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone is exactly `612345678`

### Requirement: Validation without regular expressions or external libraries
User data validation SHALL be implemented with basic String and character operations only, without regular expressions or external libraries. (F1-C02)

#### Scenario: Implementation inspection
- **WHEN** the validation code is inspected
- **THEN** it uses no regular-expression API and no third-party library
