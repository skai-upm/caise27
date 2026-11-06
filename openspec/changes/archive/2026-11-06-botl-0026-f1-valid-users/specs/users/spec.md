# Spec Delta

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter equal to the letter at position (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`.

#### Scenario: Correct and incorrect check letters
- **WHEN** `12345678Z` and `12345678A` are validated
- **THEN** `12345678Z` is accepted and `12345678A` is rejected

#### Scenario: Wrong shape
- **WHEN** `1234567Z`, `123456789Z`, `1234567AZ` or `12345678` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by a final `.` and an extension of exactly three characters, with no spaces. The check SHALL use String operations, not regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). The check SHALL use neither regular expressions nor external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing categories
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 digits, with no spaces, letters or symbols.

#### Scenario: Card numbers
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are validated
- **THEN** only `1234567890123456` is accepted

### Requirement: Whole-user validation for user operations
The system SHALL provide one validation of all the data of a user (first name, surnames, DNI, email, password, phone and card) that later user operations reuse. It SHALL reject the data with an invalid-input error naming the first invalid field. First name, surnames and phone SHALL only be required to be non-empty, and the phone SHALL be stored as given.

#### Scenario: Invalid field is named
- **WHEN** user data with DNI `12345678A` and otherwise valid values is validated
- **THEN** it is rejected with an error that mentions the DNI
