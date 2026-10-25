# user-validation Specification

## Purpose

Defines the validation rules for user data (DNI, email address, password and card number) that every user operation applies before accepting new or changed data.

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter, and the letter equals the character at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE` (standard Spanish DNI algorithm).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong length or format
- **WHEN** the DNI `1234567Z` or `123456789` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, a non-empty domain name after it, and a final extension after the last `.` of exactly three characters. Whitespace SHALL NOT be allowed. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** the email `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** the password `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** the card number `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Mandatory text fields
First name, surnames and telephone number SHALL be non-blank. The telephone number SHALL be stored as given, without further format checks or transformation.

#### Scenario: Blank first name
- **WHEN** a user is created with a blank first name
- **THEN** the operation is rejected with an error message
