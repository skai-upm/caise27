# Spec Delta

## Purpose

Defines the validity rules applied to user data before it is stored, both on creation and on update, using only standard Java string operations.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter equal to the standard Spanish check letter for those digits (`TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Correct shape but wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** a DNI with fewer or more than eight digits, or without a final letter, is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only if it contains exactly one `@` with non-empty text before it, and the text after it contains a period where the domain name before the last period is non-empty and the extension after the last period has exactly three characters. Regular expressions SHALL NOT be used.

#### Scenario: Valid email
- **WHEN** the email `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Regular expressions SHALL NOT be used.

#### Scenario: Valid password
- **WHEN** the password `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing a character category
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 characters, all of them ASCII digits.

#### Scenario: Valid card
- **WHEN** the card number `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Mandatory text fields
First name, surname(s) and telephone number SHALL be non-blank. The telephone number SHALL be stored exactly as supplied, without any further format check or transformation.

#### Scenario: Blank first name
- **WHEN** a user creation supplies an empty first name
- **THEN** the creation is rejected

#### Scenario: Phone stored unchanged
- **WHEN** a user is created with phone `+34 612 345 678`
- **THEN** the stored phone is exactly `+34 612 345 678`
