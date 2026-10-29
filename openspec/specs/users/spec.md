# users Specification

## Purpose

Defines the park's users as independent domain objects: the data each user keeps, its encapsulation, and the immutability of the DNI.

## Requirements

### Requirement: User data
Each user SHALL be an independent object with first name, surname(s), DNI, email address, password, telephone number and card number. The password SHALL be kept only in its protected form.

#### Scenario: User keeps all attributes
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** each value can be read back from the user, and the password is available only as a protected value that verifies `Clave1!`

### Requirement: Encapsulated attributes
User attributes SHALL be private and accessible only through the user's operations; the DNI SHALL NOT be changeable after creation.

#### Scenario: Fields are private
- **WHEN** the user class is inspected
- **THEN** every attribute is private and the DNI attribute is final with no operation that changes it

#### Scenario: Other attributes changeable through operations
- **WHEN** the telephone of a user is changed through its operation
- **THEN** the new value is returned and the DNI is unchanged

### Requirement: User data persisted
Every user attribute SHALL be stored and reconstructed by the users repository, the password only in protected form; stored users with fewer fields SHALL still be read.

#### Scenario: Restart keeps all user data
- **WHEN** a complete user is saved and the application restarts
- **THEN** all seven attributes are reconstructed and the original password still verifies

### Requirement: Separate user, application and storage classes
The user SHALL be represented by its own class that does not manage persistence; the class that starts and runs the application SHALL NOT contain file reading or writing logic; users SHALL be stored and retrieved by a separate class.

#### Scenario: Responsibilities separated
- **WHEN** the sources are inspected
- **THEN** the user class performs no I/O, the entry class does not read or write files, and a repository class stores and retrieves users

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
