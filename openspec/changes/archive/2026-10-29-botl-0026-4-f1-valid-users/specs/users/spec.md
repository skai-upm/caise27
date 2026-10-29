# Spec Delta

## ADDED Requirements

### Requirement: DNI validation
A DNI SHALL be valid only if it has eight digits followed by an upper-case letter that matches the number according to the standard Spanish algorithm (the letter at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct and wrong letter
- **WHEN** `12345678Z` and `12345678A` are checked
- **THEN** the first is valid and the second is invalid

#### Scenario: Malformed DNI
- **WHEN** `1234567Z`, `123456789Z`, `1234567AZ` or `12345678` is checked
- **THEN** it is invalid

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: content before a single `@`, a non-empty domain name after it, and an extension of exactly three characters after the last dot, checked with basic string operations and `split`, without regular expressions.

#### Scenario: Source examples
- **WHEN** `ana@correo.com`, `ana`, `ana@correo`, `@correo.com`, `ana@.com` and `ana@correo.es` are checked
- **THEN** only `ana@correo.com` is valid

### Requirement: Password rules
A password SHALL be valid only if it contains at least one upper-case letter, one lower-case letter, one digit and one symbol, checked without regular expressions or external libraries.

#### Scenario: Source examples
- **WHEN** `Clave1!`, `clave1!`, `CLAVE1!`, `Clave!!` and `Clave12` are checked
- **THEN** only `Clave1!` is valid

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Source examples
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are checked
- **THEN** only `1234567890123456` is valid

### Requirement: Reusable user data check
The model SHALL offer a single check of a user's DNI, email, password and card that rejects invalid data with an error naming every invalid field, so that user operations can reuse it; the telephone number SHALL only be stored, without validation.

#### Scenario: Several invalid fields reported
- **WHEN** the data DNI `12345678A`, email `ana@correo`, password `Clave1!` and card `123` is checked
- **THEN** an error names DNI, email and card as invalid
