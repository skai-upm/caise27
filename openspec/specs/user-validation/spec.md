# user-validation Specification

## Purpose

Defines the acceptance rules for user data so that only well-formed identities, contacts, credentials and cards are ever stored.

## Requirements

### Requirement: Spanish DNI validation
A DNI SHALL be accepted only when it is exactly eight digits followed by one uppercase letter equal to the letter of the standard Spanish algorithm (`TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23).

#### Scenario: Valid DNI
- **WHEN** DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Correct shape, wrong letter
- **WHEN** DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** a DNI with fewer than eight digits, more than eight digits, or no final letter is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be accepted only when it has the shape `text@text.xxx`: one `@` with non-empty text before it, a part after it containing a period, a non-empty domain before the last period and an extension of exactly three characters after it. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be accepted only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing category
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be accepted only when it consists of exactly 16 consecutive ASCII digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Required text fields
First name, surname(s) and telephone number SHALL be non-blank; the telephone number SHALL be stored exactly as supplied, without transformation or encryption.

#### Scenario: Phone stored as supplied
- **GIVEN** telephone `612345678`
- **WHEN** a user is created with it
- **THEN** the stored telephone is `612345678`

#### Scenario: Blank name rejected
- **WHEN** a creation request has an empty first name
- **THEN** creation is rejected
