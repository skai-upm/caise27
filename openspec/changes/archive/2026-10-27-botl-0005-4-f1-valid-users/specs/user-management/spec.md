# Spec Delta

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter, and that letter corresponds to the number according to the standard Spanish DNI algorithm (letter at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** the DNIs `1234567Z`, `123456789Z` or `12345678` are validated
- **THEN** each is rejected

### Requirement: Email validation
An email address SHALL be valid only when it has the form `text@text.xxx`: non-empty content before a single `@`, and after it a non-empty domain followed by `.` and an extension of exactly three characters. The check SHALL use String operations and `split`, not regular expressions.

#### Scenario: Valid email
- **WHEN** the email `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** the emails `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` are validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol, checked without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** the password `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** the passwords `clave1!` (no uppercase), `CLAVE1!` (no lowercase), `Clave!!` (no digit) or `Clave12` (no symbol) are validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** the card number `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** the card numbers `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` are validated
- **THEN** each is rejected

### Requirement: Telephone stored without validation
In Delivery 1 the telephone number SHALL only be stored as given, without validation, encryption or any other transformation.

#### Scenario: Telephone kept as given
- **WHEN** user data with telephone `612345678` is validated and stored
- **THEN** the telephone does not affect validity and is stored as `612345678`

### Requirement: Reusable user data validation
The system SHALL be able to validate a complete set of user data (DNI, email, password, card number) in one check that reports which fields are invalid, so that user operations can reuse it.

#### Scenario: All fields valid
- **WHEN** the data `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is validated
- **THEN** no errors are reported

#### Scenario: Several invalid fields
- **WHEN** user data with DNI `12345678A` and card `123` is validated
- **THEN** errors are reported for the DNI and the card number
