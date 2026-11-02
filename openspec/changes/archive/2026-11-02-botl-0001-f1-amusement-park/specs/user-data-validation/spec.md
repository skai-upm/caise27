# Spec Delta

## Purpose

Defines the rules that decide whether data describing a user is acceptable, so that creation and update operations can reuse the same checks (F1-C02).

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter that equals the standard Spanish check letter for that number (letter table `TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23).

#### Scenario: Correct check letter
- **WHEN** `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Incorrect check letter
- **WHEN** `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** a value with fewer or more than eight digits, or without a final letter, is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only when it has the form `text@text.xxx`: non-empty content before a single `@`, and after it a non-empty domain followed by `.` and an extension of exactly three characters. Validation SHALL use only basic String operations and `split`, without regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone stored as given
In Delivery 1 the telephone number SHALL only be stored, without validation, encryption or any other transformation.

#### Scenario: Telephone preserved
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is exactly `612345678`
