# Spec Delta

## Purpose

Defines the validation rules applied to user data on creation and on update, implemented with plain string and character operations and no regular expressions.

## ADDED Requirements

### Requirement: Spanish DNI validation
A DNI SHALL be accepted only if it consists of exactly eight digits followed by one uppercase letter equal to `TRWAGMYFPDXBNJZSQVHLCKE` at index (number mod 23).

#### Scenario: Valid DNI
- **WHEN** DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** DNI `1234567Z`, `123456789Z` or `12345678` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be accepted only if it has exactly one `@` with non-empty text before it, and the part after it contains a period with a non-empty domain before the last period and exactly three characters after it.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be accepted only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace).

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing a category
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be accepted only if it consists of exactly 16 characters, all ASCII digits `0`–`9`.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` or `123456789012345A` is validated
- **THEN** it is rejected

### Requirement: Free-text fields
First name, surname(s) and telephone number SHALL be stored as supplied, without transformation or encryption, and SHALL be rejected only when empty or blank.

#### Scenario: Phone stored as supplied
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone is exactly `612345678`

#### Scenario: Blank name rejected
- **WHEN** a user is created with an empty first name
- **THEN** creation is rejected
