# user-data-validation Specification

## Purpose

Determines whether user data meets the assignment restrictions for DNI, email, password and card number, reusable by every user operation that accepts such data.

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter that matches the standard Spanish DNI check letter for that number (letter at index `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct check letter
- **WHEN** `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong length or non-digits
- **WHEN** a DNI with fewer or more than eight digits, or with a non-digit among the first eight characters, is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only when it has the form `text@text.xxx`: non-empty content before a single `@`, and after it a non-empty domain followed by `.` and an extension of exactly three characters.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
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

### Requirement: Telephone is stored without validation
In Delivery 1 the telephone number SHALL only be stored; no format validation, encryption or other transformation SHALL be applied.

#### Scenario: Telephone stored as given
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is exactly `612345678`

### Requirement: Validation without regular expressions or external libraries
DNI, email, password and card-number validation SHALL be implemented with basic Java `String`/character operations (email using `split`), without regular expressions or external libraries.

#### Scenario: Implementation constraint
- **WHEN** the validation code is inspected
- **THEN** it uses no regular expression APIs and no non-standard libraries
