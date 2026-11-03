# Spec Delta

## Purpose

Determines whether user data meets the assignment's DNI, email, password and card-number rules, reusable by every user operation.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of eight digits followed by a letter that matches the number according to the standard Spanish DNI algorithm (letter table `TRWAGMYFPDXBNJZSQVHLCKE` indexed by number modulo 23).

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
An email SHALL be valid only when it has the form `text@text.xxx`: non-empty content before `@`, a non-empty domain after `@`, and an extension of exactly three characters after the final dot.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** each is rejected

### Requirement: Telephone is stored without validation
The telephone number SHALL be stored as given, without validation, encryption or transformation.

#### Scenario: Telephone stored verbatim
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is `612345678`

### Requirement: Validation without regular expressions
Validation SHALL be implemented with basic String operations and `split`, without regular expressions or external libraries.

#### Scenario: Code inspection
- **WHEN** the validation code is inspected
- **THEN** it uses no regular expressions and no external libraries
