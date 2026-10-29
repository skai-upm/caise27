# Spec Delta

## Purpose

Defines the restrictions that user data (DNI, email, password and card number) must satisfy before it is accepted by any user operation.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one letter that matches the standard Spanish DNI control algorithm (letter at index `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** the DNI `1234567Z` or `123456789` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only if it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by `.` and an extension of exactly three characters. Validation MUST use String operations and `split`, without regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is not a letter, digit or whitespace). Validation MUST NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone is stored as given
The telephone number SHALL be stored as provided, without validation, encryption or transformation.

#### Scenario: Telephone stored verbatim
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is `612345678`
