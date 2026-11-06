# Spec Delta

## ADDED Requirements

### Requirement: DNI validation
A user's DNI SHALL consist of exactly eight digits followed by one uppercase letter. The letter SHALL be the one assigned to the number by the standard Spanish algorithm: the number modulo 23 indexes the sequence `TRWAGMYFPDXBNJZSQVHLCKE`. Any other DNI SHALL be rejected.

#### Scenario: Valid DNI
- **WHEN** `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter or format
- **WHEN** `12345678A`, `1234567Z`, `123456789Z` or `1234567AZ` is validated
- **THEN** it is rejected

### Requirement: E-mail validation
A user's e-mail SHALL have the form `text@text.xxx`, checked with String operations and `split`, not regular expressions. It SHALL contain exactly one `@`, with content before it, a non-empty domain after it, and an extension of exactly three characters after the last dot, and no whitespace.

#### Scenario: Valid e-mail
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid e-mails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A user's password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (any character that is not a letter, digit or whitespace). It SHALL be checked without regular expressions or external libraries, and before the password is protected for storage.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing category
- **WHEN** `clave1!` (no uppercase), `CLAVE1!` (no lowercase), `Clave!!` (no digit) or `Clave12` (no symbol) is validated
- **THEN** each is rejected

### Requirement: Card number validation
A user's card number SHALL be exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** each is rejected

### Requirement: Reusable user data validation
The user data rules SHALL be available as one reusable validation of DNI, e-mail, password and card number. It SHALL reject invalid data with an error naming the first violated rule, so that every user operation applies the same checks. The telephone number SHALL only be stored, without validation.

#### Scenario: Combined validation
- **WHEN** data with a valid DNI, e-mail and password but card `123` is validated
- **THEN** it is rejected with an error about the card number

#### Scenario: Phone not validated
- **WHEN** valid data with phone `abc` is validated
- **THEN** it is accepted
