# user-data-validation Specification

## Purpose

Defines the reusable validity rules for user DNI, email, password and card number that every user-creating or user-modifying operation must apply (source F1-C02, sections 5.1-5.5).

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter that equals the check letter of the standard Spanish DNI algorithm (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct check letter accepted
- **WHEN** `12345678Z` is validated as a DNI
- **THEN** it is accepted

#### Scenario: Wrong check letter rejected
- **WHEN** `12345678A` is validated as a DNI
- **THEN** it is rejected

#### Scenario: Wrong length or format rejected
- **WHEN** a DNI with fewer or more than eight digits, a non-digit in the first eight positions, or no final letter is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only when it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by a dot and an extension of exactly three characters.

#### Scenario: Valid email accepted
- **WHEN** `ana@correo.com` is validated as an email
- **THEN** it is accepted

#### Scenario: Invalid emails rejected
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated as an email
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace).

#### Scenario: Valid password accepted
- **WHEN** `Clave1!` is validated as a password
- **THEN** it is accepted

#### Scenario: Passwords missing a character class rejected
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated as a password
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card accepted
- **WHEN** `1234567890123456` is validated as a card number
- **THEN** it is accepted

#### Scenario: Invalid cards rejected
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated as a card number
- **THEN** it is rejected

### Requirement: Telephone number is stored without transformation
The telephone number SHALL be stored exactly as provided, without format validation, encryption or any other transformation in this delivery.

#### Scenario: Telephone stored verbatim
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone number is `612345678`

### Requirement: Validation without regular expressions or external libraries
All user-data validation SHALL be implemented with basic String/character operations and `split`, without regular expressions or external libraries.

#### Scenario: No regular expressions in validation
- **WHEN** the validation source code is inspected
- **THEN** it uses no `java.util.regex` types, no `String.matches`/`replaceAll` patterns and no third-party libraries
