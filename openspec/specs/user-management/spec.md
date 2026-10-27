# user-management Specification

## Purpose

Defines the users of the amusement park application: the data each user holds, the invariants on that data, and how users are stored and retrieved separately from the user representation and the application entry point.

## Requirements

### Requirement: User data
Each user SHALL hold the following data: first name, surname(s), DNI, email address, password, telephone number and card number. The data SHALL only be accessible through the user's operations (encapsulated attributes).

#### Scenario: User keeps its data
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** the user returns exactly those values for each attribute

### Requirement: DNI is immutable
A user's DNI SHALL NOT be changeable after the user is created.

#### Scenario: No DNI modification
- **WHEN** a user has been created with DNI `12345678Z`
- **THEN** the user offers no operation to change the DNI and keeps returning `12345678Z`

### Requirement: Separate user storage
Users SHALL be stored and retrieved by a component separate from the user representation and from the application entry point; the user itself SHALL NOT manage storage.

#### Scenario: Store and retrieve a user
- **WHEN** a user with email `ana@correo.com` is stored
- **THEN** it can be retrieved by that email address

#### Scenario: Unknown user
- **WHEN** a user is looked up by an email address that was never stored
- **THEN** no user is returned

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

### Requirement: User creation command
The application SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when all checks pass, store a new user with that data and confirm it with `USER CREATED`. The command SHALL have exactly seven data elements after the command name.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is printed and a user with email `ana@correo.com` and DNI `12345678Z` exists

#### Scenario: Wrong number of elements
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error describing the expected syntax is printed and no user is created

### Requirement: Validation before storing
User data SHALL be validated with the DNI, email, password and card rules before a user is stored; if any rule fails, no user SHALL be created and the failing fields SHALL be reported.

#### Scenario: Invalid DNI rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the DNI is printed and no user is created

#### Scenario: Invalid password rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the password is printed and no user is created

### Requirement: Unique DNI and email
Two users SHALL NOT have the same DNI or the same email address; both conditions SHALL be checked before creating a user.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otra@correo.com`
- **THEN** an error about the duplicate DNI is printed and no user is created

#### Scenario: Duplicate email rejected
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** an error about the duplicate email is printed and no user is created
