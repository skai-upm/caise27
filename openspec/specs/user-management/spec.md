# user-management Specification

## Purpose
Defines the park user: its attributes, how they are encapsulated, and which of them may change, as the basis for user validation, registration, deletion and modification.

## Requirements

### Requirement: User attributes
Each user SHALL have a first name, surname(s), DNI, e-mail address, password, telephone number and card number, held as private attributes accessible only through the user's operations. The password SHALL be kept only in its protected (BCrypt) form.

#### Scenario: Complete user
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, e-mail `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`
- **THEN** the user exposes those values (the password only as a protected value that verifies `Clave1!`)

#### Scenario: Encapsulation
- **WHEN** the user class is inspected
- **THEN** all its instance attributes are private

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after creation, while the other attributes MAY be modified through the user's operations.

#### Scenario: No way to change the DNI
- **WHEN** the operations of the user class are inspected
- **THEN** the DNI attribute is final and there is no operation that modifies it, whereas e-mail, names, telephone, card and protected password have modifiers

### Requirement: DNI validation
A user's DNI SHALL consist of eight digits followed by the upper-case letter that corresponds to the number under the standard Spanish DNI algorithm (letter at position number mod 23 of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid and invalid DNI
- **WHEN** the DNIs `12345678Z`, `12345678A`, `1234567Z`, `123456789` and `1234567AZ` are validated
- **THEN** only `12345678Z` is valid

### Requirement: E-mail validation
A user's e-mail SHALL have the form `text@text.xxx`: content before a single `@`, a domain after it, and an extension of exactly three characters after the last dot, checked with basic `String` operations and `split`, without regular expressions.

#### Scenario: Valid and invalid e-mails
- **WHEN** `ana@correo.com`, `ana`, `ana@correo`, `@correo.com`, `ana@.com` and `ana@correo.es` are validated
- **THEN** only `ana@correo.com` is valid

### Requirement: Password validation
A user's password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol, checked without regular expressions or external libraries.

#### Scenario: Valid and invalid passwords
- **WHEN** `Clave1!`, `clave1!`, `CLAVE1!`, `Clave!!` and `Clave12` are validated
- **THEN** only `Clave1!` is valid

### Requirement: Card number validation
A user's card number SHALL contain exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid and invalid cards
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are validated
- **THEN** only `1234567890123456` is valid

### Requirement: Reusable user data validation
The validation SHALL be available to user operations as a single check of all user data that rejects invalid data with an error naming the invalid field; the telephone number SHALL only be stored, without validation.

#### Scenario: Whole user validation
- **WHEN** user data with a valid DNI, e-mail, password and card is validated, and then the same data with the card `1234`
- **THEN** the first passes and the second is rejected with an error about the card number
