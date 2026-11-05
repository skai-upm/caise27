# Spec Delta

## Purpose

Defines the rules that determine whether the data defining a user (DNI, email address, password, card number) meets the restrictions of the assignment, so that user operations can reuse them.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one letter, and the letter equals the Spanish DNI check letter for that number (the letter at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct check letter accepted
- **WHEN** the DNI "12345678Z" is validated
- **THEN** it is accepted

#### Scenario: Incorrect check letter rejected
- **WHEN** the DNI "12345678A" is validated
- **THEN** it is rejected

#### Scenario: Malformed DNI rejected
- **WHEN** a DNI with fewer than eight digits ("1234567Z"), more than eight digits ("123456789Z"), a non-digit in the number ("1234567AZ") or no final letter ("12345678") is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: exactly one `@` with non-empty content before it, and after it a non-empty domain and an extension of exactly three characters separated by a dot. The check MUST be implemented with String operations and `split`, without regular expressions.

#### Scenario: Valid email accepted
- **WHEN** the email "ana@correo.com" is validated
- **THEN** it is accepted

#### Scenario: Invalid emails rejected
- **WHEN** any of "ana", "ana@correo", "@correo.com", "ana@.com" or "ana@correo.es" is validated
- **THEN** it is rejected

#### Scenario: Two-character extension rejected
- **WHEN** an email whose extension has two characters ("ana@correo.es") is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). The check MUST NOT use regular expressions or external libraries.

#### Scenario: Valid password accepted
- **WHEN** the password "Clave1!" is validated
- **THEN** it is accepted

#### Scenario: Password missing a required category rejected
- **WHEN** any of "clave1!" (no uppercase), "CLAVE1!" (no lowercase), "Clave!!" (no digit) or "Clave12" (no symbol) is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Sixteen digits accepted
- **WHEN** the card number "1234567890123456" is validated
- **THEN** it is accepted

#### Scenario: Wrong length or non-digit characters rejected
- **WHEN** any of "1234 5678 9012 3456", "123456789012345", "12345678901234567" or "123456789012345A" is validated
- **THEN** it is rejected

### Requirement: Telephone number stored without validation
The telephone number SHALL be accepted and stored as given; no format validation or transformation is applied in this delivery.

#### Scenario: Any telephone value accepted
- **WHEN** user data with telephone "600 123 456" is validated
- **THEN** the telephone does not cause the data to be rejected

### Requirement: Whole-user validation
The system SHALL provide a single check that accepts user data only when its DNI, email, password and card number are all valid, so that later user operations can reuse it.

#### Scenario: All fields valid
- **GIVEN** user data with DNI "12345678Z", email "ana@correo.com", password "Clave1!" and card number "1234567890123456"
- **WHEN** the user data is validated
- **THEN** it is accepted

#### Scenario: One invalid field rejects the user
- **GIVEN** the same user data but with DNI "12345678A"
- **WHEN** the user data is validated
- **THEN** it is rejected
