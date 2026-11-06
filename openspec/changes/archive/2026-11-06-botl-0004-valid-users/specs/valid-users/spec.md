# Spec Delta

## Purpose

Determines whether the data that defines a user of the Amusement Park meets the assignment's rules for DNI, email address, password and card number, so user operations can reuse one consistent check.

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one uppercase letter equal to the letter obtained by the standard Spanish DNI algorithm (the number modulo 23 indexing `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct check letter is accepted
- **GIVEN** the DNI `12345678Z`
- **WHEN** it is validated
- **THEN** it is accepted as valid

#### Scenario: Incorrect check letter is rejected
- **GIVEN** the DNI `12345678A`
- **WHEN** it is validated
- **THEN** it is rejected

#### Scenario: Malformed DNI is rejected
- **GIVEN** a DNI with fewer or more than eight digits, a non-digit among the first eight characters, or no final letter (e.g. `1234567Z`, `123456789Z`, `1234567AZ`, `12345678`)
- **WHEN** it is validated
- **THEN** it is rejected

### Requirement: Email address validation
An email address SHALL be valid only when it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by a dot and an extension of exactly three characters. The check MUST use String operations and `split`, never regular expressions.

#### Scenario: Well-formed email is accepted
- **GIVEN** the email `ana@correo.com`
- **WHEN** it is validated
- **THEN** it is accepted as valid

#### Scenario: Malformed emails are rejected
- **GIVEN** any of the emails `ana`, `ana@correo`, `@correo.com`, `ana@.com`
- **WHEN** it is validated
- **THEN** it is rejected

#### Scenario: Two-character extension is rejected
- **GIVEN** the email `ana@correo.es`
- **WHEN** it is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol, where a symbol is any character that is not a letter, a digit or whitespace. The check MUST NOT use regular expressions or external libraries.

#### Scenario: Password with all categories is accepted
- **GIVEN** the password `Clave1!`
- **WHEN** it is validated
- **THEN** it is accepted as valid

#### Scenario: Password missing a category is rejected
- **GIVEN** any of the passwords `clave1!` (no uppercase), `CLAVE1!` (no lowercase), `Clave!!` (no digit), `Clave12` (no symbol)
- **WHEN** it is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive ASCII digits, with no spaces, letters or symbols.

#### Scenario: Sixteen digits are accepted
- **GIVEN** the card number `1234567890123456`
- **WHEN** it is validated
- **THEN** it is accepted as valid

#### Scenario: Wrong length or non-digit characters are rejected
- **GIVEN** any of the card numbers `1234 5678 9012 3456`, `123456789012345`, `12345678901234567`, `123456789012345A`
- **WHEN** it is validated
- **THEN** it is rejected

### Requirement: Telephone stored without validation
The telephone number SHALL be accepted and stored exactly as supplied, without validation, encryption or transformation, and MUST NOT affect whether a user's data is valid.

#### Scenario: Any telephone value is kept unchanged
- **GIVEN** user data whose other fields are valid and whose telephone is `+34 600-000-000`
- **WHEN** the user data is validated
- **THEN** it is valid and the stored telephone is still `+34 600-000-000`

### Requirement: Whole-user validation
The system SHALL consider a user's data valid only when its DNI, email address, password and card number are each valid; a missing (null) value for any of these fields MUST be rejected. Validating MUST NOT modify the user.

#### Scenario: Fully valid user is accepted
- **GIVEN** a user with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!` and card number `1234567890123456`
- **WHEN** the user's data is validated
- **THEN** it is valid

#### Scenario: One invalid field makes the user invalid
- **GIVEN** a user whose data is valid except for the DNI `12345678A`
- **WHEN** the user's data is validated
- **THEN** it is invalid and all of the user's attributes are unchanged

#### Scenario: Missing field is rejected
- **GIVEN** a user whose email address is missing (null)
- **WHEN** the user's data is validated
- **THEN** it is invalid
