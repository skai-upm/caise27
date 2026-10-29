# user-validation Specification

## Purpose

Provides the rules that decide whether a user's DNI, email address, password and card number are valid, reused by every operation that creates or modifies users.

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it has exactly eight digits followed by one letter, and that letter equals the standard Spanish control letter: the character at position (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`.

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** the DNIs `1234567Z`, `123456789Z`, `12345678`, or `1234567AZ` are validated
- **THEN** each is rejected

### Requirement: Email validation
An email SHALL be valid only if it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by `.` and an extension of exactly three characters. Validation SHALL use String operations and `split`, not regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` or `ana.nueva@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails from the assignment
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com`, or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is not a letter, digit or whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords from the assignment
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!`, or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards from the assignment
- **WHEN** `1234 5678 9012 3456`, `123456789012345`, or `12345678901234567` is validated
- **THEN** each is rejected

### Requirement: Telephone is stored without validation
The telephone number SHALL be stored as provided, without format validation, encryption or transformation.

#### Scenario: Any telephone value is stored
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone is exactly `612345678`
