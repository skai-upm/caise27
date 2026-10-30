# user-data-validation Specification

## Purpose

Defines the rules that decide whether a user's DNI, email address, password and card number are acceptable, reused by every user operation that sets those values.

## Requirements

### Requirement: DNI validation
A DNI SHALL be accepted only if it consists of exactly eight digits followed by one uppercase letter equal to the letter of the standard Spanish DNI algorithm (`TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** the DNI `1234567Z`, `123456789Z` or `12345678` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL be accepted only if it matches `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by `.` and an extension of exactly three characters. The check SHALL use String operations and `split`, not regular expressions.

#### Scenario: Valid email
- **WHEN** the email `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be accepted only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace), checked without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** the password `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be accepted only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** the card number `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone stored without validation
The telephone number SHALL be stored as given, without validation, encryption or transformation.

#### Scenario: Phone kept verbatim
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone is `612345678`
