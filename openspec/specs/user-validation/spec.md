# user-validation Specification

## Purpose
Determines whether the data defining a user meets the assignment's restrictions for DNI, email address, password and card number, so that user operations can reuse the same checks.

## Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight decimal digits followed by one letter, and that letter equals the standard Spanish check letter: the character at position (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`. Any other value MUST be rejected.

#### Scenario: Correct check letter accepted
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is valid

#### Scenario: Incorrect check letter rejected
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is invalid

#### Scenario: Wrong shape rejected
- **WHEN** the DNIs `1234567Z`, `123456789Z`, `12345678`, `1234567AZ` or an empty/absent value are validated
- **THEN** each is invalid

### Requirement: Email validation
An email address SHALL be valid only when it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain, a dot, and an extension of exactly three characters. The check MUST use String operations and `split`, not regular expressions.

#### Scenario: Well-formed email accepted
- **WHEN** the email `ana@correo.com` is validated
- **THEN** it is valid

#### Scenario: Malformed emails rejected
- **WHEN** each of `ana`, `ana@correo`, `@correo.com`, `ana@.com` is validated
- **THEN** each is invalid

#### Scenario: Two-character extension rejected
- **WHEN** the email `ana@correo.es` is validated
- **THEN** it is invalid

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). The check MUST NOT use regular expressions or external libraries.

#### Scenario: Password with all categories accepted
- **WHEN** the password `Clave1!` is validated
- **THEN** it is valid

#### Scenario: Password missing a category rejected
- **WHEN** each of `clave1!` (no uppercase), `CLAVE1!` (no lowercase), `Clave!!` (no digit) and `Clave12` (no symbol) is validated
- **THEN** each is invalid

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive decimal digits, with no spaces, letters or symbols.

#### Scenario: Sixteen digits accepted
- **WHEN** the card number `1234567890123456` is validated
- **THEN** it is valid

#### Scenario: Wrong length or characters rejected
- **WHEN** each of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` and `123456789012345A` is validated
- **THEN** each is invalid

### Requirement: Telephone stored without validation
The telephone number SHALL NOT be subject to format validation or transformation in this delivery; it is stored exactly as provided.

#### Scenario: Any telephone value does not affect validity
- **WHEN** a user whose DNI, email, password and card number are all valid has telephone `+34 600 12 34 56`
- **THEN** the user's data is valid and the telephone is kept unchanged

### Requirement: Whole-user validation
The system SHALL provide a single reusable check that reports a user's data as valid only when its DNI, email, password and card number are all valid, without modifying the user.

#### Scenario: Fully valid user
- **WHEN** a user with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!` and card `1234567890123456` is checked
- **THEN** the data is reported valid

#### Scenario: One invalid field makes the user invalid
- **WHEN** the same user data is checked but with card `123456789012345`
- **THEN** the data is reported invalid
- **AND** the user's attributes are unchanged
