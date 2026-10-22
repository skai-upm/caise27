# user-validation Specification

## Purpose

Defines the data rules a user's DNI, email address, password and card number must satisfy, reused by every operation that creates or changes user data.

## Requirements

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter, and the letter SHALL match the Spanish check letter for the number (`TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23). A lowercase letter SHALL be accepted and treated as uppercase.

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong length
- **WHEN** the DNI `1234567Z` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL have the form `text@text.xxx`: exactly one `@`, non-empty content before it, a non-empty domain name after it, a `.`, and an extension of exactly three letters, with no whitespace. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Mandatory text fields
First name, surnames and telephone number SHALL be non-empty. The telephone number SHALL only be stored, without further format validation or transformation.

#### Scenario: Empty first name
- **WHEN** a user is created with an empty first name
- **THEN** the data is rejected
