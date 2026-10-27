# Spec Delta

## Purpose

Defines the restrictions user data must satisfy — DNI, email address, password and card number — so every user operation can reuse the same checks.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one uppercase letter that matches the standard Spanish DNI check letter (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct check letter
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** the DNIs `1234567Z`, `123456789Z`, `1234567AZ` or `12345678` are validated
- **THEN** each is rejected

### Requirement: Email validation
An email address SHALL be valid only when it has the form `text@text.xxx`: non-empty content before a single `@`, and after it a non-empty domain, a dot and an extension of exactly three characters.

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

### Requirement: Telephone stored without validation
The telephone number SHALL be stored as given, without validation or transformation.

#### Scenario: Any telephone accepted
- **WHEN** user data with telephone `612345678` is validated
- **THEN** the telephone does not cause the data to be rejected

### Requirement: No regular expressions
Validation SHALL be implemented with basic String operations and split, without regular expressions or external libraries.

#### Scenario: Implementation inspection
- **WHEN** the validation source is inspected
- **THEN** it uses no `java.util.regex` types, `matches`, `replaceAll` or similar regex-based methods
