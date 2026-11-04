# Spec Delta

## Purpose

Defines the data-quality rules every user value must satisfy before it is stored or updated, implemented with standard Java string operations and without regular expressions.

## ADDED Requirements

### Requirement: Spanish DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one uppercase letter equal to `TRWAGMYFPDXBNJZSQVHLCKE` indexed by the eight-digit number modulo 23.

#### Scenario: Valid DNI
- **WHEN** `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** `12345678A` is validated
- **THEN** it is rejected even though its shape is correct

#### Scenario: Wrong shape
- **WHEN** `1234567Z`, `123456789Z` or `1234567AZ` is validated
- **THEN** it is rejected

### Requirement: Email format validation
An email SHALL be valid only when it contains exactly one `@`, a nonempty part before `@`, and a part after `@` that contains a period with a nonempty domain name before the last period and an extension of exactly three characters after it.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password strength validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a non-whitespace character that is neither a letter nor a digit).

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing a category
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 ASCII digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** each is rejected

### Requirement: Required text fields
First name, surnames and telephone number SHALL be nonblank; the telephone number SHALL be stored exactly as supplied (after trimming surrounding whitespace) without transformation or encryption.

#### Scenario: Blank name rejected
- **WHEN** a user is created with an empty first name
- **THEN** creation is rejected

#### Scenario: Phone stored as supplied
- **WHEN** a user is created with phone `+34 612 345 678`
- **THEN** the stored phone is `+34 612 345 678`
